package com.bitejiuyeke.forum.services.impl;

import com.bitejiuyeke.forum.common.AppResult;
import com.bitejiuyeke.forum.common.ResultCode;
import com.bitejiuyeke.forum.dao.MessageMapper;
import com.bitejiuyeke.forum.exception.ApplicationException;
import com.bitejiuyeke.forum.model.Message;
import com.bitejiuyeke.forum.model.User;
import com.bitejiuyeke.forum.services.IMessageService;
import com.bitejiuyeke.forum.services.IUserService;
import com.bitejiuyeke.forum.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author 比特就业课
 */

@Slf4j
@Service
public class MessageServiceImpl implements IMessageService {

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private IUserService userService;

    @Override
    public void create(Message message) {
        // 非空校验
        if (message == null || message.getPostUserId() == null || message.getReceiveUserId() == null
           || StringUtil.isEmpty(message.getContent())) {
            // 打印日志
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        // 校验接收者是否存在
        User user = userService.selectById(message.getReceiveUserId());
        if (user == null || user.getDeleteState() == 1) {
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }

        // 设置默认值
        message.setState((byte) 0); // 表示未读状态
        message.setDeleteState((byte) 0);
        // 设置创建与更新时间
        Date date = new Date();
        message.setCreateTime(date);
        message.setUpdateTime(date);

        // 调用DAO
        int row = messageMapper.insertSelective(message);
        if (row != 1) {
            log.warn(ResultCode.FAILED_CREATE.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_CREATE));
        }
    }

    @Override
    public Message selectById(Long id) {
        // 非空校验
        if (id == null || id <= 0) {
            // 打印日志
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        // 调用DAO
        Message message = messageMapper.selectByPrimaryKey(id);
        // 返回结果
        return message;
    }

    @Override
    public Integer selectUnreadCount(Long receiveUserId) {
        // 非空校验
        if (receiveUserId == null || receiveUserId <= 0) {
            // 打印日志
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        // 直接调用DAO
        Integer count = messageMapper.selectUnreadCount(receiveUserId);
        // 正常的查询是不可能出现null，如果为null则抛出异常
        if (count == null) {
            log.warn(ResultCode.ERROR_SERVICES.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.ERROR_SERVICES));
        }
        // 返回结果
        return count;
    }

    @Override
    public List<Message> selectByReceiveUserId(Long receiveUserId) {
        // 非空校验
        if (receiveUserId == null || receiveUserId <= 0) {
            // 打印日志
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        // 调用DAO
        List<Message> messages = messageMapper.selectByReceiveUserId(receiveUserId);
        // 返回结果
        return messages;
    }

    @Override
    public void updateStateById(Long id, Byte state) {
        // 非空校验, state : 0 未读 1 已读 2 已回复
        if (id == null || id <= 0 || state < 0 || state > 2) {
            // 打印日志
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        // 构造更新对象
        Message updateMessage = new Message();
        updateMessage.setId(id);
        updateMessage.setState(state);
        Date date = new Date();
        updateMessage.setUpdateTime(date);
        // 调用DAO
        int row = messageMapper.updateByPrimaryKeySelective(updateMessage);
        if (row != 1) {
            log.warn(ResultCode.ERROR_SERVICES.toString());
            throw new ApplicationException(AppResult.failed(ResultCode.ERROR_SERVICES));
        }
    }

    @Override
    public void reply(Long repliedId, Message message) {
        // 非空校验
        if (repliedId == null || repliedId <= 0) {
            // 打印日志
            log.warn(ResultCode.FAILED_PARAMS_VALIDATE.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE));
        }
        // 校验repliedId对应的站内信状态
        Message existsMessage = messageMapper.selectByPrimaryKey(repliedId);
        if (existsMessage == null || existsMessage.getDeleteState() == 1) {
            // 打印日志
            log.warn(ResultCode.FAILED_MESSAGE_NOT_EXISTS.toString());
            // 抛出异常
            throw new ApplicationException(AppResult.failed(ResultCode.FAILED_MESSAGE_NOT_EXISTS));
        }

        // 更新状态为已回复
        updateStateById(repliedId, (byte) 2);
        // 回复的内容写入数据库
        create(message);
    }

}
