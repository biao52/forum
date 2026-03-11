package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.model.DifyConversation;
import com.yb.forum.model.DifyResponse;
import com.yb.forum.services.IConversationService;
import com.yb.forum.services.IDifyService;
import com.yb.forum.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Dify智能体控制器
 */
@Api(tags = "智能助手接口")
@Slf4j
@RestController
@RequestMapping("/dify")
public class DifyController {

    @Resource
    private IDifyService iDifyService;
    @Resource
    private IConversationService conversationService;
    
    /**
     * 调用智能助手
     * @param request HTTP 请求
     * @param query 用户查询内容
     * @return 智能助手回答
     */
    @ApiOperation("调用智能助手")
    @PostMapping("/chat")
    public AppResult chat(HttpServletRequest request, 
                         @ApiParam("查询内容") @RequestParam("query") String query) {
        try {
            // 从请求头获取 token 并解析用户 ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            // 调用 Dify 服务
            DifyResponse response = iDifyService.callDify(query, userId.toString());
            
            // 保存对话记录到数据库
            try {
                DifyConversation conversation = new DifyConversation();
                conversation.setUserId(userId);
                conversation.setQuery(query);
                conversation.setAnswer(response.getAnswer());
                conversation.setConversationId(response.getConversationId());
                conversation.setMessageId(response.getMessageId());
                conversationService.save(conversation);
                log.info("保存对话记录成功，userId: {}, query: {}", userId, query);
            } catch (Exception e) {
                log.warn("保存对话记录失败：{}", e.getMessage());
                // 保存失败不影响返回结果
            }
            
            // 返回结果
            return AppResult.success(ResultCode.SUCCESS.getMessage(), response.getAnswer());
        } catch (Exception e) {
            log.error("调用智能助手失败：{}", e.getMessage(), e);
            return AppResult.failed(ResultCode.FAILED, "调用智能助手失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户的对话历史记录
     * @param request HTTP 请求
     * @param conversationId 可选的对话 ID，用于获取特定会话的历史
     * @return 对话历史列表
     */
    @ApiOperation("查询对话历史记录")
    @GetMapping("/history")
    public AppResult history(HttpServletRequest request,
                            @ApiParam("对话 ID（可选）") @RequestParam(value = "conversationId", required = false) String conversationId) {
        try {
            // 从请求头获取 token 并解析用户 ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            // 查询对话历史
            List<DifyConversation> history;
            if (conversationId != null && !conversationId.isEmpty()) {
                // 查询特定会话的历史
                history = conversationService.selectByUserIdAndConversationId(userId, conversationId);
            } else {
                // 查询所有历史
                history = conversationService.selectByUserId(userId);
            }
            
            // 返回结果
            return AppResult.success(history);
        } catch (Exception e) {
            log.error("查询对话历史失败：{}", e.getMessage(), e);
            return AppResult.failed(ResultCode.FAILED, "查询对话历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户的会话列表
     * @param request HTTP 请求
     * @return 会话列表
     */
    @ApiOperation("获取会话列表")
    @GetMapping("/sessions")
    public AppResult sessions(HttpServletRequest request) {
        try {
            // 从请求头获取 token 并解析用户 ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            // 查询会话列表（最多 10 个）
            List<Map<String, Object>> sessions = conversationService.getConversationSessions(userId, 10);
            
            // 返回结果
            return AppResult.success(sessions);
        } catch (Exception e) {
            log.error("获取会话列表失败：{}", e.getMessage(), e);
            return AppResult.failed(ResultCode.FAILED, "获取会话列表失败：" + e.getMessage());
        }
    }
}