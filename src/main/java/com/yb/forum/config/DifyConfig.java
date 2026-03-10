package com.yb.forum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Dify智能体配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "dify")
public class DifyConfig {
    /**
     * Dify API 密钥
     */
    private String apiKey;
    
    /**
     * Dify API 基础URL
     */
    private String baseUrl = "http://localhost";
    
    /**
     * 智能体应用ID
     */
    private String appId;
    
    /**
     * 超时时间（毫秒）
     */
    private int timeout = 30000;
    
    /**
     * 系统提示词
     */
    private String systemPrompt = "你是论坛智能助手，专门为论坛用户提供帮助。你的职责包括：\r\n" + //
                "    1. 回答用户关于论坛使用的问题\r\n" + //
                "    2. 提供技术支持和指导\r\n" + //
                "    3. 解答用户的疑问\r\n" + //
                "    4. 保持友好、专业、耐心的态度\r\n" + //
                "    5. 回答简洁清晰，避免冗长\r\n" + //
                "    6. 如果遇到无法回答的问题，礼貌地告知用户\r\n" + //
                "    请注意：\r\n" + //
                "    - 使用中文回答\r\n" + //
                "    - 语气亲切自然\r\n" + //
                "    - 提供准确、有用的信息";
}