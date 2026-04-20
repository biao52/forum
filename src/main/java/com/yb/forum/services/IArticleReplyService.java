package com.yb.forum.services;

import com.yb.forum.model.ArticleReply;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author yangbiao
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
    
    /**
     * 根据回复Id删除回复
     * @param id 回复Id
     */
    @Transactional
    void deleteById(Long id);
    
    /**
     * 根据回复Id查询回复
     * @param id 回复Id
     * @return 回复对象
     */
    ArticleReply selectById(Long id);
}
