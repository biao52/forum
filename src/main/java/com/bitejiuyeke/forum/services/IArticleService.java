package com.bitejiuyeke.forum.services;

import com.bitejiuyeke.forum.model.Article;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IArticleService {


    /**
     * 发布帖子
     * @param article 要发布的帖子
     */
    @Transactional // 当前方法中的执行过程会被事务管理起来
    void create (Article article);

    /**
     * 查询所有帖子列表
     * @return
     */
    List<Article> selectAll ();

    /**
     * 根据版块Id查询所有帖子列表
     * @param boardId 版块Id
     * @return
     */
    List<Article> selectAllByBoardId (Long boardId);

    /**
     * 根据用户Id查询帖子列表
     * @param userId 用户Id
     * @return 帖子列表
     */
    List<Article> selectByUserId (Long userId);

    /**
     * 根据帖子Id查询详情
     * @param id 帖子Id
     * @return 帖子详情
     */
    Article selectDetailById (Long id);

    /**
     * 根据帖子Id查询记录
     * @param id 帖子Id
     * @return
     */
    Article selectById (Long id);

    /**
     * 编辑帖子
     * @param id 帖子Id
     * @param title 帖子标题
     * @param content 帖子正文
     */
    void modify (Long id, String title, String content);

    /**
     * 点赞帖子
     * @param id 帖子Id
     */
    void thumbsUpById (Long id);

    /**
     * 根据Id删除帖子
     * @param id 帖子Id
     */
    @Transactional // 事务管理
    void deleteById (Long id);

    /**
     * 文章回复数量 +1
     * @param id 版块Id
     */
    void addOneReplyCountById (Long id);



}
