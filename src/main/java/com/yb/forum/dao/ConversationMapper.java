package com.yb.forum.dao;

import com.yb.forum.model.DifyConversation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConversationMapper {
    int insert(DifyConversation row);

    int insertSelective(DifyConversation row);

    DifyConversation selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DifyConversation row);

    int updateByPrimaryKey(DifyConversation row);

    /**
     * 根据用户 ID 查询对话记录列表（按创建时间降序）
     * @param userId 用户 ID
     * @return 对话记录列表
     */
    List<DifyConversation> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户 ID 和 conversationId 查询对话记录列表
     * @param userId 用户 ID
     * @param conversationId Dify 对话 ID
     * @return 对话记录列表
     */
    List<DifyConversation> selectByUserIdAndConversationId(
        @Param("userId") Long userId, 
        @Param("conversationId") String conversationId
    );
}
