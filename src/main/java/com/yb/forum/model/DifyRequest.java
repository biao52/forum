package com.yb.forum.model;

import lombok.Data;

import java.util.Map;

/**
 * Dify API请求模型
 */
@Data
public class DifyRequest {
    /**
     * 用户输入的参数
     */
    private Map<String, Object> inputs;
    
    /**
     * 用户的查询内容
     */
    private String query;
    
    /**
     * 响应模式：blocking（同步）或streaming（流式）
     */
    private String response_mode = "blocking";
    
    /**
     * 用户ID，用于上下文管理
     */
    private String user;
    
    /**
     * 会话ID，用于上下文管理
     */
    private String conversation_id;
}