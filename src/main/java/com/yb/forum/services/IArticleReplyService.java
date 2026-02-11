package com.yb.forum.services;

import com.yb.forum.model.ArticleReply;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author 比特就业课
 */

public interface IArticleReplyService {

    /**
     * 新增帖子回复
     * @param articleReply
     */
    @Transactional
    void create (ArticleReply articleReply);

    /**
     * 根据帖子Id查询所有的回复
     * @param articleId
     * @return
     */
    List<ArticleReply> selectByArticleId (Long articleId);
}
