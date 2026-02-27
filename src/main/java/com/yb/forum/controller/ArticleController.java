package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.config.AppConfig;
import com.yb.forum.model.Article;
import com.yb.forum.model.Board;
import com.yb.forum.model.User;
import com.yb.forum.services.IArticleService;
import com.yb.forum.services.IBoardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author 比特就业课
 */
@Api(tags = "文章接口")
@Slf4j
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private IArticleService articleService;
    @Resource
    private IBoardService boardService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
                             @ApiParam("版块Id") @RequestParam("boardId") @NonNull Long boardId,
                             @ApiParam("文章标题") @RequestParam("title") @NonNull String title,
                             @ApiParam("文章内容") @RequestParam("content") @NonNull String content) {
        // 校验用户是否禁言
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        // 版块的校验
        Board board = boardService.selectById(boardId.longValue());
        if (board == null || board.getDeleteState() == 1 || board.getState() == 1) {
            log.warn(ResultCode.FAILED_BOARD_BANNED.toString());
            return AppResult.failed(ResultCode.FAILED_BOARD_BANNED);
        }
        // 封装文章对象
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setBoardId(boardId);
        article.setUserId(user.getId());
        // 调用Service
        articleService.create(article);

        // 清除相关缓存
        clearArticleListCache(boardId);
        clearArticleListCache(null); // 清除所有文章列表缓存

        return AppResult.success();
    }

    @ApiOperation("获取帖子列表")
    @GetMapping("/getAllByBoardId")
    public AppResult<List<Article>> getAllByBoardId (
            @ApiParam("版块Id") @RequestParam(value = "boardId", required = false) Long boardId) {

        // 构造缓存key
        String cacheKey = boardId == null ? ARTICLE_LIST_KEY + "all" : ARTICLE_LIST_KEY + boardId;

        // 尝试从Redis获取缓存
        List<Article> articles = (List<Article>) redisTemplate.opsForValue().get(cacheKey);

        if (articles != null) {
            log.info("从Redis缓存获取文章列表，boardId: {}", boardId);
            return AppResult.success(articles);
        }

        // 缓存未命中，从数据库查询
        log.info("从数据库查询文章列表，boardId: {}", boardId);
        if (boardId == null) {
            articles = articleService.selectAll();
        } else {
            articles = articleService.selectAllByBoardId(boardId);
        }

        if (articles == null) {
            articles = new ArrayList<>();
        }

        // 存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, articles, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return AppResult.success(articles);
    }

    @ApiOperation("根据帖子Id获取详情")
    @GetMapping("/details")
    public AppResult<Article> getDetails (HttpServletRequest request,
                                          @ApiParam("帖子Id") @RequestParam("id") @NonNull Long id) {

        // 构造缓存key
        String cacheKey = ARTICLE_DETAIL_KEY + id;

        // 尝试从Redis获取缓存
        Article article = (Article) redisTemplate.opsForValue().get(cacheKey);

        if (article != null) {
            log.info("从Redis缓存获取文章详情，id: {}", id);
            // 从session中获取当前登录的用户
            HttpSession session = request.getSession(false);
            User user = (User) session.getAttribute(AppConfig.USER_SESSION);
            // 判断当前用户是否为作者
            if (user != null && user.getId() == article.getUserId()) {
                article.setOwn(true);
            }
            return AppResult.success(article);
        }

        // 缓存未命中，从数据库查询
        log.info("从数据库查询文章详情，id: {}", id);
        // 从session中获取当前登录的用户
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);

        article = articleService.selectDetailById(id);
        if (article == null) {
            return AppResult.failed(ResultCode.FAILED_ARTICLE_NOT_EXISTS);
        }
        // 判断当前用户是否为作者
        if (user.getId() == article.getUserId()) {
            article.setOwn(true);
        }

        // 存入Redis缓存（不缓存own字段，因为不同用户看到的不同）
        Article cacheArticle = new Article();
        cacheArticle.setId(article.getId());
        cacheArticle.setTitle(article.getTitle());
        cacheArticle.setContent(article.getContent());
        cacheArticle.setBoardId(article.getBoardId());
        cacheArticle.setUserId(article.getUserId());
        cacheArticle.setLikeCount(article.getLikeCount());
        cacheArticle.setCreateTime(article.getCreateTime());
        cacheArticle.setUpdateTime(article.getUpdateTime());
        cacheArticle.setState(article.getState());
        cacheArticle.setDeleteState(article.getDeleteState());

        redisTemplate.opsForValue().set(cacheKey, cacheArticle, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return AppResult.success(article);
    }

    @ApiOperation("修改帖子")
    @PostMapping("/modify")
    public AppResult modify (HttpServletRequest request,
                             @ApiParam("帖子Id") @RequestParam("id") @NonNull Long id,
                             @ApiParam("帖子标题") @RequestParam("title") @NonNull String title,
                             @ApiParam("帖子正文") @RequestParam("content") @NonNull String content) {
        // 获取当前登录的用户
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
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
        // 校验用户的状态
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        // 判断用户是否被禁言
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        // 调用Service
        articleService.thumbsUpById(id);

        // 清除文章详情缓存（点赞数变化）
        clearArticleDetailCache(id);

        return AppResult.success();
    }

    @ApiOperation("删除帖子")
    @PostMapping("/delete")
    public AppResult deleteById (HttpServletRequest request,
                                 @ApiParam("帖子Id") @RequestParam("id") @NonNull Long id) {
        // 校验用户状态
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        // 查询帖子详情
        Article article = articleService.selectById(id);
        // 校验帖子状态
        if (article == null || article.getDeleteState() == 1) {
            return AppResult.failed(ResultCode.FAILED_ARTICLE_NOT_EXISTS);
        }
        // 校验当前登录的用户是不是作者
        if (user.getId() != article.getUserId()) {
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
        // 如果UserId为空，那么从session中获取当前登录的用户Id
        if (userId == null) {
            HttpSession session = request.getSession(false);
            User user = (User) session.getAttribute(AppConfig.USER_SESSION);
            userId = user.getId();
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