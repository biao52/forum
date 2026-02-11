package com.bitejiuyeke.forum.services;

import com.bitejiuyeke.forum.model.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IMessageService {

    /**
     * 发送站内信息
     * @param message 站内信对象
     */
    void create (Message message);

    /**
     * 根据Id查询站内信
     * @param id 站内信ID
     * @return Message
     */
    Message selectById (Long id);

    /**
     * 根据用户Id查询该用户未读数量
     * @param receiveUserId 用户Id
     * @return 未读数量
     */
    Integer selectUnreadCount(Long receiveUserId);

    /**
     * 根据接收者用户Id查询所有站内信
     * @param receiveUserId 接收者用户Id
     * @return List<Message>
     */
    List<Message> selectByReceiveUserId (Long receiveUserId);

    /**
     * 更新指定站内信的状态
     * @param id 站内信ID
     * @param state 目标状态
     */
    void updateStateById (Long id, Byte state);

    /**
     * 回复站内信
     * @param repliedId 要回复的站内信Id
     * @param message 回复的对象
     */
    @Transactional
    void reply (Long repliedId, Message message);

}
