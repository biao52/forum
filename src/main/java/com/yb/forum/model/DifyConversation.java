package com.yb.forum.model;

import lombok.Data;

import java.util.Date;

@Data
public class DifyConversation {
    private Long id;

    private Long userId;

    private String conversationId;

    private String query;

    private String answer;

    private String messageId;

    private Date createTime;

    private Date updateTime;
}
