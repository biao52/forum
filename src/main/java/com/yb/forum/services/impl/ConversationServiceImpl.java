package com.yb.forum.services.impl;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.dao.ConversationMapper;
import com.yb.forum.exception.ApplicationException;
import com.yb.forum.model.DifyConversation;
import com.yb.forum.services.IConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ConversationServiceImpl implements IConversationService {

    @Resource
    private ConversationMapper conversationMapper;

    @Override
    public void save(DifyConversation conversation) {
        // 非空校验
        if (conversation == null || conversation.getUserId() == null) {
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }

        // 设置默认值
        Date date = new Date();
        conversation.setCreateTime(date);
        conversation.setUpdateTime(date);

        // 调用 DAO
        int row = conversationMapper.insertSelective(conversation);
        if (row != 1) {
            log.warn(ResultCode.FAILED_CREATE.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_CREATE));
        }
        log.info("保存 Dify 对话记录成功，userId: {}, conversationId: {}", 
            conversation.getUserId(), conversation.getConversationId());
    }

    @Override
    public DifyConversation selectById(Long id) {
        if (id == null || id <= 0) {
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        return conversationMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<DifyConversation> selectByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        return conversationMapper.selectByUserId(userId);
    }

    @Override
    public List<DifyConversation> selectByUserIdAndConversationId(Long userId, String conversationId) {
        if (userId == null || userId <= 0) {
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        return conversationMapper.selectByUserIdAndConversationId(userId, conversationId);
    }

    @Override
    public List<Map<String, Object>> getConversationSessions(Long userId, int limit) {
        if (userId == null || userId <= 0) {
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        
        // 查询该用户的所有对话记录（按创建时间降序）
        List<DifyConversation> allConversations = conversationMapper.selectByUserId(userId);
        
        // 按 conversationId 分组
        java.util.Map<String, List<DifyConversation>> groupedByConversation = allConversations.stream()
            .filter(c -> c.getConversationId() != null && !c.getConversationId().isEmpty())
            .collect(java.util.stream.Collectors.groupingBy(
                DifyConversation::getConversationId,
                java.util.LinkedHashMap::new, // 保持插入顺序
                java.util.stream.Collectors.toList()
            ));
        
        // 构建会话列表（每个会话取第一条和最后一条消息）
        List<Map<String, Object>> sessions = new java.util.ArrayList<>();
        int count = 0;
        
        for (java.util.Map.Entry<String, List<DifyConversation>> entry : groupedByConversation.entrySet()) {
            if (count >= limit) {
                break;
            }
            
            List<DifyConversation> conversationList = entry.getValue();
            if (conversationList.isEmpty()) {
                continue;
            }
            
            // 第一个消息（最早的消息）
            DifyConversation firstMessage = conversationList.get(0);
            // 最后一个消息（最新的消息）
            DifyConversation lastMessage = conversationList.get(conversationList.size() - 1);
            
            // 生成会话名称（取用户提问的前五个字）
            String sessionName = generateSessionName(firstMessage.getQuery());
            
            java.util.Map<String, Object> session = new java.util.HashMap<>();
            session.put("conversationId", entry.getKey());
            session.put("sessionName", sessionName); // 会话名称
            session.put("firstQuery", firstMessage.getQuery());
            session.put("lastAnswer", lastMessage.getAnswer());
            session.put("messageCount", conversationList.size());
            session.put("createTime", firstMessage.getCreateTime());
            session.put("updateTime", lastMessage.getUpdateTime());
            
            sessions.add(session);
            count++;
        }
        
        log.info("获取用户 {} 的会话列表，共 {} 个会话", userId, sessions.size());
        return sessions;
    }
    
    /**
     * 生成会话名称（取用户提问的前五个字，超过则加省略号）
     * @param query 用户提问
     * @return 会话名称
     */
    private String generateSessionName(String query) {
        if (query == null || query.isEmpty()) {
            return "新会话";
        }
        
        // 去除前后空格
        query = query.trim();
        
        // 如果超过 5 个字，取前 5 个字并加省略号
        if (query.length() > 5) {
            return query.substring(0, 5) + "...";
        }
        
        return query;
    }
}
