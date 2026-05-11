package com.yb.forum.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yb.forum.config.DifyConfig;
import com.yb.forum.model.DifyResponse;
import com.yb.forum.services.IDifyService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Dify智能体服务类
 */
@Service
public class IDifyServiceImpl implements IDifyService {

    @Resource
    private DifyConfig difyConfig;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 调用Dify智能体API
     * @param query 用户查询内容
     * @param userId 用户ID
     * @return Dify响应结果
     */
    public DifyResponse callDify(String query, String userId) {
        try {
            // 构建请求 URL
            String url = difyConfig.getBaseUrl() + "/v1/chat-messages";

            // 打印实际使用的配置
            System.out.println("=== Dify 配置检查 ===");
            System.out.println("baseUrl: " + difyConfig.getBaseUrl());
            System.out.println("实际请求 URL: " + url);
            System.out.println("API Key: " + difyConfig.getApiKey());
            System.out.println("======================");

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + difyConfig.getApiKey());
            headers.set("Content-Type", "application/json");

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", new HashMap<>());
            requestBody.put("query", query);
            requestBody.put("response_mode", "streaming"); // Agent Chat App 只支持 streaming 模式
            requestBody.put("user", userId);

            // 打印请求信息
            System.out.println("=== Dify API 请求开始 ===");
            System.out.println("URL: " + url);
            System.out.println("请求体：" + requestBody);

            // 发送请求
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            // 记录响应
            System.out.println("响应状态码：" + responseEntity.getStatusCode());
            System.out.println("响应头：" + responseEntity.getHeaders());
            System.out.println("响应体长度：" + (responseEntity.getBody() != null ? responseEntity.getBody().length() : 0));
            System.out.println("响应体内容：" + responseEntity.getBody());
            System.out.println("=== Dify API 请求结束 ===");

            // 解析响应
            DifyResponse response = new DifyResponse();
            if (responseEntity.getBody() != null) {
                try {
                    String responseBody = responseEntity.getBody();
                    System.out.println("=== 原始响应体 ===");
                    System.out.println(responseBody);
                    System.out.println("==================");
                    
                    // 流式响应包含多个 data: 开头的行
                    StringBuilder answerBuilder = new StringBuilder();
                    String conversationId = null;
                    String messageId = null;
                    
                    // 按行分割
                    String[] lines = responseBody.split("\n");
                    for (String line : lines) {
                        line = line.trim();
                        if (line.startsWith("data:")) {
                            String jsonData = line.substring(5).trim();
                            if ("[DONE]".equals(jsonData)) {
                                continue;
                            }
                            try {
                                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(jsonData);
                                System.out.println("=== 解析数据块 ===");
                                System.out.println(jsonNode);
                                
                                // 提取答案
                                if (jsonNode.has("answer")) {
                                    String answerPart = jsonNode.get("answer").asText();
                                    answerBuilder.append(answerPart);
                                    System.out.println("累积答案: " + answerBuilder.toString());
                                }
                                
                                // 提取 conversation_id
                                if (jsonNode.has("conversation_id")) {
                                    conversationId = jsonNode.get("conversation_id").asText();
                                }
                                
                                // 提取 message_id
                                if (jsonNode.has("message_id")) {
                                    messageId = jsonNode.get("message_id").asText();
                                }
                                
                                // 检查错误
                                if (jsonNode.has("error")) {
                                    String errorMessage = jsonNode.get("error").asText();
                                    System.out.println("Dify 返回错误: " + errorMessage);
                                    response.setAnswer("Dify API 错误：" + errorMessage);
                                    response.setMessage(errorMessage);
                                    return response;
                                }
                            } catch (Exception e) {
                                System.out.println("解析 JSON 行失败: " + line);
                            }
                        }
                    }
                    
                    // 设置最终答案
                    String finalAnswer = answerBuilder.toString();
                    if (finalAnswer.isEmpty()) {
                        System.out.println("警告: 未从流式响应中提取到答案");
                        response.setAnswer("未收到有效回答，请查看控制台日志");
                    } else {
                        System.out.println("=== 最终答案 ===");
                        System.out.println(finalAnswer);
                        response.setAnswer(finalAnswer);
                    }
                    
                    // 设置 conversation_id 和 message_id
                    if (conversationId != null) {
                        response.setConversation_id(conversationId);
                    }
                    if (messageId != null) {
                        response.setMessage_id(messageId);
                    }

                } catch (Exception e) {
                    System.out.println("=== 响应解析失败 ===");
                    System.out.println("异常类型: " + e.getClass().getName());
                    System.out.println("异常信息: " + e.getMessage());
                    e.printStackTrace();
                    response.setAnswer("响应解析失败：" + e.getMessage());
                }
            } else {
                System.out.println("响应体为空");
                response.setAnswer("Dify 返回空响应");
            }

            return response;
        } catch (Exception e) {
            // 处理异常
            System.out.println("=== Dify API 调用异常 ===");
            System.out.println("异常类型：" + e.getClass().getName());
            System.out.println("异常信息：" + e.getMessage());
            e.printStackTrace();
            System.out.println("=== Dify API 调用异常结束 ===");

            DifyResponse errorResponse = new DifyResponse();
            errorResponse.setAnswer("抱歉，智能助手暂时无法为您服务，请稍后再试。错误详情：" + e.getMessage());
            errorResponse.setMessage("调用 Dify API 失败：" + e.getMessage());
            return errorResponse;
        }
    }
}