package com.yb.forum.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yb.forum.config.DifyConfig;
import com.yb.forum.model.DifyRequest;
import com.yb.forum.model.DifyResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Dify智能体服务类
 */
@Service
public class DifyService {

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
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + difyConfig.getApiKey());
            headers.set("Content-Type", "application/json");
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", new HashMap<>());
            requestBody.put("query", query);
            requestBody.put("response_mode", "streaming"); // 使用流式响应模式
            requestBody.put("user", userId);
            
            // 添加系统提示词（如果配置了）
            String systemPrompt = difyConfig.getSystemPrompt();
            System.out.println("=== 系统提示词配置 ===");
            System.out.println(systemPrompt);
            System.out.println("======================");
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                // 将系统提示词添加到查询前面，作为上下文
                String enhancedQuery = systemPrompt + "\n\n用户问题：" + query;
                requestBody.put("query", enhancedQuery);
            }
            
            // 打印请求信息
            System.out.println("=== Dify API 请求开始 ===");
            System.out.println("URL: " + url);
            System.out.println("API Key: " + difyConfig.getApiKey());
            System.out.println("请求体：" + requestBody);
            
            // 发送请求（使用流式响应）
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);
            
            // 记录响应
            System.out.println("响应状态码：" + responseEntity.getStatusCode());
            System.out.println("Dify API 响应原始数据：" + responseEntity.getBody());
            System.out.println("=== Dify API 请求结束 ===");
            
            // 解析流式响应
            DifyResponse response = new DifyResponse();
            if (responseEntity.getBody() != null) {
                try {
                    // 流式响应可能包含多个 data: 开头的行
                    String responseBody = responseEntity.getBody();
                    StringBuilder answerBuilder = new StringBuilder();
                    StringBuilder thinkingBuilder = new StringBuilder();
                    boolean hasThinking = false;
                    
                    // 按行分割
                    String[] lines = responseBody.split("\n");
                    for (String line : lines) {
                        line = line.trim();
                        // 查找以 "data:" 开头的行
                        if (line.startsWith("data:")) {
                            String jsonData = line.substring(5).trim();
                            // 跳过 [DONE] 标记
                            if ("[DONE]".equals(jsonData)) {
                                continue;
                            }
                            try {
                                // 解析 JSON
                                com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(jsonData);
                                
                                // 提取思考过程（如果有）
                                if (jsonNode.has("thought")) {
                                    String thought = jsonNode.get("thought").asText();
                                    if (!thought.isEmpty()) {
                                        thinkingBuilder.append(thought);
                                        hasThinking = true;
                                    }
                                }
                                
                                // 提取最终答案
                                if (jsonNode.has("answer")) {
                                    String answerPart = jsonNode.get("answer").asText();
                                    answerBuilder.append(answerPart);
                                } else if (jsonNode.has("error")) {
                                    String errorMessage = jsonNode.get("error").asText();
                                    response.setAnswer("Dify API 错误：" + errorMessage);
                                    response.setMessage(errorMessage);
                                    return response;
                                }
                            } catch (Exception e) {
                                System.out.println("解析 JSON 行失败：" + line);
                            }
                        }
                    }
                    
                    // 如果没有找到 answer 字段，尝试直接解析整个响应
                    if (answerBuilder.length() == 0) {
                        try {
                            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);
                            if (jsonNode.has("answer")) {
                                answerBuilder.append(jsonNode.get("answer").asText());
                            }
                        } catch (Exception e) {
                            System.out.println("直接解析响应失败");
                        }
                    }
                    
                    // 只返回最终答案，不返回思考过程
                    String finalAnswer = answerBuilder.toString();
                    if (finalAnswer.isEmpty() && hasThinking) {
                        // 如果没有答案但有思考过程，返回思考过程作为答案
                        finalAnswer = thinkingBuilder.toString();
                    }
                    response.setAnswer(finalAnswer);
                    
                } catch (Exception e) {
                    System.out.println("响应解析失败：" + e.getMessage());
                    response.setAnswer(responseEntity.getBody());
                }
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