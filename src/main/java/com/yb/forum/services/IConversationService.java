package com.yb.forum.services;

import com.yb.forum.model.DifyConversation;

import java.util.List;
import java.util.Map;

public interface IConversationService {

    /**
     * 保存对话记录
     * @param conversation 对话记录对象
     */
    void save(DifyConversation conversation);

    /**
     * 根据 ID 查询对话记录
     * @param id 对话记录 ID
     * @return 对话记录
     */
    DifyConversation selectById(Long id);

    /**
     * 根据用户 ID 查询对话记录列表
     * @param userId 用户 ID
     * @return 对话记录列表
     */
    List<DifyConversation> selectByUserId(Long userId);

    /**
     * 根据用户 ID 和对话 ID 查询对话记录列表
     * @param userId 用户 ID
     * @param conversationId Dify 对话 ID
     * @return 对话记录列表
     */
    List<DifyConversation> selectByUserIdAndConversationId(Long userId, String conversationId);

    /**
     * 获取用户的会话列表（按 conversationId 分组，最多返回最近的 10 个会话）
     * @param userId 用户 ID
     * @param limit 限制数量
     * @return 会话列表，每个会话包含 conversationId、第一条和最后一条消息
     */
    List<Map<String, Object>> getConversationSessions(Long userId, int limit);
}
