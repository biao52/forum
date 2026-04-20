package com.yb.forum.model;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleReply {
    // 编号
    private Long id;

    // 帖子Id, 关联Article
    private Long articleId;

    // 回复的用户编号
    private Long postUserId;

    // 被回复的评论 ID（两级平铺模式）
    private Long replyId;

    // 被回复的用户 ID（两级平铺模式）
    private Long replyUserId;

    // 回复的正文
    private String content;

    // 忽略，需求中点赞功能
    private Integer likeCount;

    // 状态 0正常  1 禁用
    private Byte state;

    // 状态 0正常  1 删除
    private Byte deleteState;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

    // 关联对象 - 回复的发布者
    private User user;
    
    // 关联对象 - 被回复的用户
    private User replyUser;
}