package com.yb.forum.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Dify API响应模型
 */
@Data
public class DifyResponse {
    /**
     * 智能体返回的回答
     */
    private String answer;
    
    /**
     * 响应状态消息
     */
    private String message;
    
    /**
     * 会话ID，用于上下文管理
     */
    private String conversation_id;
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 智能体思考过程（如果有）
     */
    private String thinking;
    
    /**
     * 参考资料（如果有）
     */
    private List<Map<String, Object>> references;
}