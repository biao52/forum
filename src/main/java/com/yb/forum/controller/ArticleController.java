package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.config.AppConfig;
import com.yb.forum.exception.ApplicationException;
import com.yb.forum.model.Article;
import com.yb.forum.model.Board;
import com.yb.forum.model.User;
import com.yb.forum.services.IArticleService;
import com.yb.forum.services.IBoardService;
import com.yb.forum.services.IUserService;
import com.yb.forum.utils.JwtUtil;
import com.yb.forum.utils.StringUtil;
import com.yb.forum.utils.XssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
      import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import com.yb.forum.utils.SensitiveFilterUtil;

import java.util.concurrent.TimeUnit;

/**
 * @Author yangbiao
 */

@Slf4j
@Api(tags = "帖子接口")
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private IArticleService articleService;
    @Resource
    private IUserService userService;
    @Resource
    private IBoardService boardService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private SensitiveFilterUtil sensitiveFilterUtil;

    // Redis Key 前缀
    private static final String ARTICLE_LIST_KEY = "article:list:";
    private static final String ARTICLE_DETAIL_KEY = "article:detail:";
    private static final String ARTICLE_USER_KEY = "article:user:";
    private static final long CACHE_EXPIRE_TIME = 30; // 缓存过期时间（分钟）

    /**
     * 发布新帖子
     */
    @ApiOperation("发布新帖")
    @PostMapping("/create")
    public AppResult create (HttpServletRequest request,
                             @ApiParam("版块Id") @RequestParam("boardId") Long boardId,
                             @ApiParam("文章标题") @RequestParam("title") String title,
                             @ApiParam("文章内容") @RequestParam("content") String content) {
        // 1. 检查必要参数
        if (boardId == null) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "版块ID不能为空");
        }
        if (StringUtil.isEmpty(title)) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "标题不能为空");
        }
        if (StringUtil.isEmpty(content)) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "内容不能为空");
        }
        
        // 2. 检查参数长度
        if (title.trim().length() < 2) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "标题长度至少为2位");
        }
        if (title.trim().length() > 100) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "标题长度不能超过100位");
        }
        if (content.trim().length() < 10) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "内容长度至少为10位");
        }
        if (content.length() > 50000) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "内容长度不能超过50000位");
        }
        
        // 3. 从 JWT 令牌中获取用户 ID
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // JWT 解析失败
            }
        }
        
        // 如果没有用户 ID，返回错误
        if (userId == null) {
            return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED, "请先登录");
        }
        
        // 查询用户信息
        User user = userService.selectById(userId);
        if (user == null) {
            return AppResult.failed(ResultCode.FAILED_USER_NOT_EXISTS, "用户不存在");
        }
        
        // 4. 校验用户是否禁言
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        
        // 5. 版块的校验
        Board board = boardService.selectById(boardId.longValue());
        if (board == null) {
            return AppResult.failed(ResultCode.FAILED_BOARD_NOT_EXISTS, "版块不存在");
        }
        if (board.getDeleteState() == 1) {
            return AppResult.failed(ResultCode.FAILED_BOARD_BANNED, "版块已被删除");
        }
        if (board.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_BOARD_BANNED, "版块已被禁用");
        }
        
        // 6. 检查发布频率（防刷屏）
        String publishKey = "user:publish:" + user.getId();
        String lastPublishTime = (String) redisTemplate.opsForValue().get(publishKey);
        if (lastPublishTime != null) {
            long lastTime = Long.parseLong(lastPublishTime);
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime < 60000) { // 60秒内只能发布一次
                return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "发布频率过高，请稍后再试");
            }
        }
        
        // 7. 过滤和清理输入
        String cleanTitle = XssUtil.clean(title.trim());
        String cleanContent = XssUtil.clean(content.trim());
        
        // 8. 敏感词过滤
        if (sensitiveFilterUtil.containsSensitiveWord(cleanTitle) || sensitiveFilterUtil.containsSensitiveWord(cleanContent)) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "内容包含敏感词，请修改后重新发布");
        }
        
        // 9. 封装文章对象
        Article article = new Article();
        article.setTitle(cleanTitle);
        article.setContent(cleanContent);
        article.setBoardId(boardId);
        article.setUserId(user.getId());
        
        try {
            // 9. 调用Service
            articleService.create(article);
            
            // 10. 设置发布频率限制
            redisTemplate.opsForValue().set(publishKey, String.valueOf(System.currentTimeMillis()), 1, TimeUnit.MINUTES);
            
            // 11. 清除相关缓存
            clearArticleListCache(boardId);
            clearArticleListCache(null); // 清除所有文章列表缓存
            
            // 12. 返回文章ID
            return AppResult.success(article.getId());
        } catch (ApplicationException e) {
            log.error("发布文章失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("发布文章异常: {}", e.getMessage(), e);
            return AppResult.failed(ResultCode.ERROR_SERVICES, "发布文章失败，请稍后再试");
        }
    }

    @ApiOperation("获取帖子列表")
    @GetMapping("/getAllByBoardId")
    public AppResult<List<Article>> getAllByBoardId(
            @ApiParam("版块Id") @RequestParam(value = "boardId", required = false) Long boardId,
            @ApiParam("搜索关键字") @RequestParam(value = "keyword", required = false) String keyword) {

        // 🔑 构造缓存 Key（区分普通查询和关键字搜索）
        String cacheKey;
        if (StringUtil.isEmpty(keyword)) {
            // 普通查询：按 boardId 缓存
            cacheKey = boardId == null ? ARTICLE_LIST_KEY + "all" : ARTICLE_LIST_KEY + "board:" + boardId;
        } else {
            // 关键字搜索：单独缓存，避免污染普通列表
            // 对 keyword 做处理：去除空格、转小写、替换特殊字符，保证 key 合法
            String safeKeyword = keyword.trim().toLowerCase().replaceAll("[^a-z0-9\u4e00-\u9fa5]", "_");
            cacheKey = boardId == null
                    ? ARTICLE_LIST_KEY + "search:keyword:" + safeKeyword
                    : ARTICLE_LIST_KEY + "search:board:" + boardId + ":keyword:" + safeKeyword;
        }

        // 🔍 尝试从 Redis 获取缓存
        List<Article> articles = (List<Article>) redisTemplate.opsForValue().get(cacheKey);

        if (articles != null) {
            log.info("✅ 从 Redis 缓存获取文章列表，cacheKey: {}", cacheKey);
            return AppResult.success(articles);
        }

        // 🗄️ 缓存未命中，从数据库查询
        log.info("🔎 从数据库查询文章列表，boardId: {}, keyword: {}", boardId, keyword);

        if (StringUtil.isEmpty(keyword)) {
            // 普通查询
            if (boardId == null) {
                articles = articleService.selectAll();
            } else {
                articles = articleService.selectAllByBoardId(boardId, null);
            }
        } else {
            // 🔍 关键字搜索（标题 OR 内容）
            if (boardId == null) {
                articles = articleService.selectByKeyword(keyword);
            } else {
                articles = articleService.selectByBoardIdAndKeyword(boardId, keyword);
            }
        }

        if (articles == null) {
            articles = new ArrayList<>();
        }

        // 💾 存入 Redis 缓存（搜索结果的缓存时间可以短一些）
        long expireTime = StringUtil.isEmpty(keyword) ? CACHE_EXPIRE_TIME : 10; // 搜索缓存10分钟
        redisTemplate.opsForValue().set(cacheKey, articles, expireTime, TimeUnit.MINUTES);
        log.info("💾 缓存已更新，cacheKey: {}, expire: {}min", cacheKey, expireTime);

        return AppResult.success(articles);
    }

    @ApiOperation("根据帖子Id获取详情")
    @GetMapping("/details")
    public AppResult<Article> getDetails(
            HttpServletRequest request,
            @ApiParam("帖子Id") @RequestParam("id") @NonNull Long id) {

        // 🔍 构造缓存key
        String cacheKey = ARTICLE_DETAIL_KEY + id;

        // 🔍 尝试从Redis获取缓存
        Article article = (Article) redisTemplate.opsForValue().get(cacheKey);

        // 从 JWT 令牌中获取用户 ID
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // JWT 解析失败
            }
        }

        if (article != null) {
            log.info("✅ 从 Redis 缓存获取文章详情，id: {}", id);
            // 判断当前用户是否为作者
            if (userId != null && userId == article.getUserId()) {
                article.setOwn(true);
            }
            
            // 增加浏览量（异步，不影响返回速度）
            new Thread(() -> {
                try {
                    articleService.addOneVisitCountById(id);
                    // 清除缓存
                    clearArticleDetailCache(id);
                } catch (Exception e) {
                    log.error("增加浏览量失败: {}", e.getMessage());
                }
            }).start();
            
            return AppResult.success(article);
        }

        // 🗄️ 缓存未命中，从数据库查询
        log.info("🔎 从数据库查询文章详情，id: {}", id);

        // 调用 Service，获取帖子详情（包含 user 和 board 关联信息）
        article = articleService.selectDetailById(id);
        if (article == null) {
            return AppResult.failed(ResultCode.FAILED_ARTICLE_NOT_EXISTS);
        }
        // 判断当前用户是否为作者
        if (userId != null && userId == article.getUserId()) {
            article.setOwn(true);
        }

        // 增加浏览量
        articleService.addOneVisitCountById(id);

        // 💾 存入Redis缓存（缓存完整对象，包括 user 信息）
        // ⚠️ 注意：不要设置 own 字段到缓存，因为不同用户看到的不同
        Article cacheArticle = new Article();
        // 复制所有字段（包括 user）
        cacheArticle.setId(article.getId());
        cacheArticle.setBoardId(article.getBoardId());
        cacheArticle.setUserId(article.getUserId());
        cacheArticle.setTitle(article.getTitle());
        cacheArticle.setContent(article.getContent());
        cacheArticle.setVisitCount(article.getVisitCount());
        cacheArticle.setReplyCount(article.getReplyCount());
        cacheArticle.setLikeCount(article.getLikeCount());
        cacheArticle.setState(article.getState());
        cacheArticle.setDeleteState(article.getDeleteState());
        cacheArticle.setCreateTime(article.getCreateTime());
        cacheArticle.setUpdateTime(article.getUpdateTime());
        // ✅ 重要：复制 user 对象
        cacheArticle.setUser(article.getUser());
        // ✅ 如果有 board 对象，也要复制
        // cacheArticle.setBoard(article.getBoard());

        redisTemplate.opsForValue().set(cacheKey, cacheArticle, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.info("💾 文章详情已缓存，id: {}", id);

        return AppResult.success(article);
    }

    @ApiOperation("修改帖子")
    @PostMapping("/modify")
    public AppResult modify (HttpServletRequest request,
                             @ApiParam("帖子Id") @RequestParam("id") @NonNull Long id,
                             @ApiParam("帖子标题") @RequestParam("title") @NonNull String title,
                             @ApiParam("帖子正文") @RequestParam("content") @NonNull String content) {
        // 从 JWT 令牌中获取用户 ID
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // JWT 解析失败
            }
        }
        
        // 如果没有用户 ID，返回错误
        if (userId == null) {
            return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED, "请先登录");
        }
        
        // 查询用户信息
        User user = userService.selectById(userId);
        if (user == null) {
            return AppResult.failed(ResultCode.FAILED_USER_NOT_EXISTS, "用户不存在");
        }
        // 校验用户状态
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        // 查询帖子详情
        Article article = articleService.selectById(id);
        // 校验帖子是否有效
        if (article == null) {
            return AppResult.failed(ResultCode.FAILED_ARTICLE_NOT_EXISTS);
        }
        // 判断用户是不是作者
        if (user.getId() != article.getUserId()) {
            return AppResult.failed(ResultCode.FAILED_FORBIDDEN);
        }
        // 判断帖子的状态 - 已归档
        if (article.getState() == 1 || article.getDeleteState() == 1) {
            return AppResult.failed(ResultCode.FAILED_ARTICLE_BANNED);
        }

        // 调用Service
        articleService.modify(id, title, content);

        // 清除相关缓存
        clearArticleDetailCache(id);
        clearArticleListCache(article.getBoardId());
        clearArticleListCache(null);

        log.info("帖子更新成功. Article id = " + id + "User id = " + user.getId() + ".");
        return AppResult.success();
    }

    @ApiOperation("点赞")
    @PostMapping("/thumbsUp")
    public AppResult thumbsUp (HttpServletRequest request,
                               @ApiParam("帖子Id") @RequestParam("id") @NonNull Long id) {
        // 从 JWT 令牌中获取用户 ID
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // JWT 解析失败
            }
        }
        
        // 如果没有用户 ID，返回错误
        if (userId == null) {
            return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED, "请先登录");
        }
        
        // 查询用户信息
        User user = userService.selectById(userId);
        if (user == null) {
            return AppResult.failed(ResultCode.FAILED_USER_NOT_EXISTS, "用户不存在");
        }
        
        // 判断用户是否被禁言
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        
        // 检查是否已经点赞
        String likeKey = "article:like:" + id;
        Boolean hasLiked = redisTemplate.opsForSet().isMember(likeKey, userId);
        if (Boolean.TRUE.equals(hasLiked)) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "您已经点过赞了");
        }
        
        // 调用Service
        articleService.thumbsUpById(id);
        
        // 记录点赞信息到Redis
        redisTemplate.opsForSet().add(likeKey, userId);
        // 设置过期时间（30天）
        redisTemplate.expire(likeKey, 30, TimeUnit.DAYS);

        // 清除文章详情缓存（点赞数变化）
        clearArticleDetailCache(id);

        return AppResult.success();
    }

    @ApiOperation("取消点赞")
    @PostMapping("/cancelThumbsUp")
    public AppResult cancelThumbsUp (HttpServletRequest request,
                                    @ApiParam("帖子Id") @RequestParam("id") @NonNull Long id) {
        // 从 JWT 令牌中获取用户 ID
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // JWT 解析失败
            }
        }
        
        // 如果没有用户 ID，返回错误
        if (userId == null) {
            return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED, "请先登录");
        }
        
        // 查询用户信息
        User user = userService.selectById(userId);
        if (user == null) {
            return AppResult.failed(ResultCode.FAILED_USER_NOT_EXISTS, "用户不存在");
        }
        
        // 判断用户是否被禁言
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        
        // 检查是否已经点赞
        String likeKey = "article:like:" + id;
        Boolean hasLiked = redisTemplate.opsForSet().isMember(likeKey, userId);
        if (Boolean.FALSE.equals(hasLiked)) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "您还没有点过赞");
        }
        
        // 调用Service
        articleService.cancelThumbsUpById(id);
        
        // 从Redis中移除点赞记录
        redisTemplate.opsForSet().remove(likeKey, userId);

        // 清除文章详情缓存（点赞数变化）
        clearArticleDetailCache(id);

        return AppResult.success();
    }

    @ApiOperation("删除帖子")
    @PostMapping("/delete")
    public AppResult deleteById (HttpServletRequest request,
                                 @ApiParam("帖子Id") @RequestParam("id") @NonNull Long id) {
        // 从 JWT 令牌中获取用户 ID
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // JWT 解析失败
            }
        }
        
        // 如果没有用户 ID，返回错误
        if (userId == null) {
            return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED, "请先登录");
        }
        
        // 查询用户信息
        User user = userService.selectById(userId);
        if (user == null) {
            return AppResult.failed(ResultCode.FAILED_USER_NOT_EXISTS, "用户不存在");
        }
        
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        // 查询帖子详情
        Article article = articleService.selectById(id);
        // 校验帖子状态
        if (article == null || article.getDeleteState() == 1) {
            return AppResult.failed(ResultCode.FAILED_ARTICLE_NOT_EXISTS);
        }
        // 校验当前登录的用户是不是作者或管理员
        boolean isOwner = user.getId().equals(article.getUserId());
        boolean isAdmin = user.getIsAdmin() != null && user.getIsAdmin() == 1;
        if (!isOwner && !isAdmin) {
            return AppResult.failed(ResultCode.FAILED_FORBIDDEN);
        }
        // 调用Service
        articleService.deleteById(id);

        // 清除相关缓存
        clearArticleDetailCache(id);
        clearArticleListCache(article.getBoardId());
        clearArticleListCache(null);
        clearArticleUserCache(article.getUserId());

        return AppResult.success();
    }

    @ApiOperation("获取用户的帖子列表")
    @GetMapping("/getAllByUserId")
    public AppResult<List<Article>> getAllByUserId (HttpServletRequest request,
                                                    @ApiParam("用户Id") @RequestParam(value = "userId", required = false) Long userId) {
        // 如果 UserId 为空，那么从 JWT 令牌中获取当前登录的用户 Id
        if (userId == null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    userId = JwtUtil.getUserIdFromToken(token);
                } catch (Exception e) {
                    // JWT 解析失败
                }
            }
            
            // 如果没有用户 ID，返回错误
            if (userId == null) {
                return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED, "请先登录");
            }
        }

        // 构造缓存key
        String cacheKey = ARTICLE_USER_KEY + userId;

        // 尝试从Redis获取缓存
        List<Article> articles = (List<Article>) redisTemplate.opsForValue().get(cacheKey);

        if (articles != null) {
            log.info("从Redis缓存获取用户文章列表，userId: {}", userId);
            return AppResult.success(articles);
        }

        // 缓存未命中，从数据库查询
        log.info("从数据库查询用户文章列表，userId: {}", userId);
        articles = articleService.selectByUserId(userId);

        if (articles == null) {
            articles = new ArrayList<>();
        }

        // 存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, articles, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return AppResult.success(articles);
    }

    /**
     * 清除文章列表缓存
     */
    private void clearArticleListCache(Long boardId) {
        String key = boardId == null ? ARTICLE_LIST_KEY + "all" : ARTICLE_LIST_KEY + boardId;
        redisTemplate.delete(key);
        log.info("清除文章列表缓存: {}", key);
    }

    /**
     * 清除文章详情缓存
     */
    private void clearArticleDetailCache(Long articleId) {
        String key = ARTICLE_DETAIL_KEY + articleId;
        redisTemplate.delete(key);
        log.info("清除文章详情缓存: {}", key);
    }

    /**
     * 清除用户文章列表缓存
     */
    private void clearArticleUserCache(Long userId) {
        String key = ARTICLE_USER_KEY + userId;
        redisTemplate.delete(key);
        log.info("清除用户文章列表缓存: {}", key);
    }
}