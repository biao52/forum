package com.bitejiuyeke.forum.controller;

import com.bitejiuyeke.forum.common.AppResult;
import com.bitejiuyeke.forum.common.ResultCode;
import com.bitejiuyeke.forum.config.AppConfig;
import com.bitejiuyeke.forum.model.Article;
import com.bitejiuyeke.forum.model.ArticleReply;
import com.bitejiuyeke.forum.model.User;
import com.bitejiuyeke.forum.services.IArticleReplyService;
import com.bitejiuyeke.forum.services.IArticleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @Author 比特就业课
 */
@Api(tags = "回复接口")
@Slf4j
@RestController
@RequestMapping("/reply")
public class ArticleReplyController {

    @Resource
    private IArticleService articleService;
    @Resource
    private IArticleReplyService articleReplyService;

    @ApiOperation("回复帖子")
    @PostMapping("/create")
    public AppResult create (HttpServletRequest request,
                             @ApiParam("帖子Id") @RequestParam("articleId") @NonNull Long articleId,
                             @ApiParam("帖子内容") @RequestParam("content") @NonNull String content) {
        // 获取用户
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        // 判断用户是否已禁言
        if (user.getState() == 1) {
            // 表示已禁言
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        // 获取要回复的帖子对象
        Article article = articleService.selectById(articleId);
        // 是否存在，或已删除
        if (article == null || article.getDeleteState() == 1) {
            // 表示已删除或不存在
            return AppResult.failed(ResultCode.FAILED_ARTICLE_NOT_EXISTS);
        }
        // 是否封帖
        if (article.getState() == 1) {
            // 表示已封贴
            return AppResult.failed(ResultCode.FAILED_ARTICLE_BANNED);
        }

        // 构建回复对象
        ArticleReply articleReply = new ArticleReply();
        articleReply.setArticleId(articleId); // 要回复的帖Id
        articleReply.setPostUserId(user.getId()); // 回复的发送者
        articleReply.setContent(content); // 回复的内容
        // 写入回复
        articleReplyService.create(articleReply);
        // 返回结果
        return AppResult.success();
    }

    @ApiOperation("获取回复列表")
    @GetMapping("/getReplies")
    public AppResult<List<ArticleReply>> getRepliesByArticleId (@ApiParam("帖子Id") @RequestParam("articleId") @NonNull Long articleId) {
        // 校验帖子是否存在
        Article article = articleService.selectById(articleId);
        if (article == null || article.getDeleteState() == 1) {
            // 返回错误提示
            return AppResult.failed(ResultCode.FAILED_ARTICLE_NOT_EXISTS);
        }
        // 调用Service
        List<ArticleReply> articleReplies = articleReplyService.selectByArticleId(articleId);
        // 返回结果
        return AppResult.success(articleReplies);

    }
}
