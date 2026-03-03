package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.config.AppConfig;
import com.yb.forum.model.Article;
import com.yb.forum.model.ArticleReply;
import com.yb.forum.model.User;
import com.yb.forum.services.IArticleReplyService;
import com.yb.forum.services.IArticleService;
import com.yb.forum.services.IUserService;
import com.yb.forum.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author yangbiao
 */
@Api(tags = "回复接口")
@Slf4j
@RestController
@RequestMapping("/reply")
public class ArticleReplyController {

    @Resource
    private IArticleService articleService;
    @Resource
    private IUserService userService;
    @Resource
    private IArticleReplyService articleReplyService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REPLY_LIST_KEY = "reply:list:";
    private static final long CACHE_EXPIRE_TIME = 30; // 缓存过期时间（分钟）

    @ApiOperation("回复帖子")
    @PostMapping("/create")
    public AppResult create (HttpServletRequest request,
                             @ApiParam("帖子Id") @RequestParam("articleId") @NonNull Long articleId,
                             @ApiParam("帖子内容") @RequestParam("content") @NonNull String content) {
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
        
        // 判断用户是否已禁言
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        // 获取要回复的帖子对象
        Article article = articleService.selectById(articleId);
        // 是否存在，或已删除
        if (article == null || article.getDeleteState() == 1) {
            return AppResult.failed(ResultCode.FAILED_ARTICLE_NOT_EXISTS);
        }
        // 是否封帖
        if (article.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_ARTICLE_BANNED);
        }

        // 构建回复对象
        ArticleReply articleReply = new ArticleReply();
        articleReply.setArticleId(articleId);
        articleReply.setPostUserId(user.getId());
        articleReply.setContent(content);
        // 写入回复
        articleReplyService.create(articleReply);

        // 清除该文章的回复列表缓存
        String cacheKey = REPLY_LIST_KEY + articleId;
        redisTemplate.delete(cacheKey);
        log.info("清除回复列表缓存: {}", cacheKey);

        return AppResult.success();
    }

    @ApiOperation("获取回复列表")
    @GetMapping("/getReplies")
    public AppResult<List<ArticleReply>> getRepliesByArticleId (
            @ApiParam("帖子Id") @RequestParam("articleId") @NonNull Long articleId) {

        // 构造缓存key
        String cacheKey = REPLY_LIST_KEY + articleId;

        // 尝试从Redis获取缓存
        List<ArticleReply> articleReplies = (List<ArticleReply>) redisTemplate.opsForValue().get(cacheKey);

        if (articleReplies != null) {
            log.info("从Redis缓存获取回复列表，articleId: {}", articleId);
            return AppResult.success(articleReplies);
        }

        // 校验帖子是否存在
        Article article = articleService.selectById(articleId);
        if (article == null || article.getDeleteState() == 1) {
            return AppResult.failed(ResultCode.FAILED_ARTICLE_NOT_EXISTS);
        }

        // 缓存未命中，从数据库查询
        log.info("从数据库查询回复列表，articleId: {}", articleId);
        articleReplies = articleReplyService.selectByArticleId(articleId);

        if (articleReplies == null) {
            articleReplies = new java.util.ArrayList<>();
        }

        // 存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, articleReplies, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return AppResult.success(articleReplies);
    }
}