package com.bitejiuyeke.forum.services.impl;

import com.bitejiuyeke.forum.model.Message;
import com.bitejiuyeke.forum.services.IMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageServiceImplTest {

    @Resource
    private IMessageService messageService;
    @Resource
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    void create() {
        Message message = new Message();
        message.setPostUserId(2l);
        message.setReceiveUserId(1l);
        message.setContent("单元测试");
        messageService.create(message);
        System.out.println("发送成功");

    }

    @Test
    void selectUnreadCount() {
        Integer count = messageService.selectUnreadCount(1l);
        System.out.println("未读数量为： " + count);
        count = messageService.selectUnreadCount(2l);
        System.out.println("未读数量为： " + count);
        count = messageService.selectUnreadCount(20l);
        System.out.println("未读数量为： " + count);
    }

    @Test
    void selectByReceiveUserId() throws JsonProcessingException {
        List<Message> messages = messageService.selectByReceiveUserId(1l);
        System.out.println(objectMapper.writeValueAsString(messages));

        messages = messageService.selectByReceiveUserId(2l);
        System.out.println(objectMapper.writeValueAsString(messages));

        messages = messageService.selectByReceiveUserId(20l);
        System.out.println(objectMapper.writeValueAsString(messages));
    }

    @Test
    @Transactional
    void updateStateById() {
        messageService.updateStateById(1l, (byte) 1);
        System.out.println("更新成功");
    }

    @Test
    void selectById() throws JsonProcessingException {
        Message message = messageService.selectById(1l);
        System.out.println(objectMapper.writeValueAsString(message));
    }

    @Test
    void reply() {
        // 构建对象
        Message message = new Message();
        message.setPostUserId(1l);
        message.setReceiveUserId(2l);
        message.setContent("单元测试回复");
        // 调用service
        messageService.reply(2l, message);
        System.out.println("回复成功");
    }
}