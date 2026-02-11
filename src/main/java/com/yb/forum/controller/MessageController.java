package com.bitejiuyeke.forum.controller;

import com.bitejiuyeke.forum.common.AppResult;
import com.bitejiuyeke.forum.common.ResultCode;
import com.bitejiuyeke.forum.config.AppConfig;
import com.bitejiuyeke.forum.model.Message;
import com.bitejiuyeke.forum.model.User;
import com.bitejiuyeke.forum.services.IMessageService;
import com.bitejiuyeke.forum.services.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @Author 比特就业课
 */
@Slf4j
@Api(tags = "站内信接口")
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private IMessageService messageService;
    @Resource
    private IUserService userService;

    @ApiOperation("发送站内信")
    @PostMapping("/send")
    public AppResult send (HttpServletRequest request,
                           @ApiParam("接收者Id") @RequestParam("receiveUserId") @NonNull Long receiveUserId,
                           @ApiParam("内容") @RequestParam("content") @NonNull String content) {
        // 获取当前登录的用户信息
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        // 1. 当前登录用户的状态，如果是禁言状态不能发站内信
        if (user.getState() == 1) {
            // 返回用户状态异常
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }
        // 2. 不能给自己发站内信
        if (user.getId() == receiveUserId) {
            return AppResult.failed("不能给自己发送站内信");
        }
        // 3. 校验接收者是否存在
        User receiveUser = userService.selectById(receiveUserId);
        if (receiveUser == null || receiveUser.getDeleteState() == 1) {
            // 返回接收者状态不正常
            return AppResult.failed("接收者状态异常");
        }
        // 4. 封装对象
        Message message = new Message();
        message.setPostUserId(user.getId()); // 发送者Id
        message.setReceiveUserId(receiveUserId); // 接收者Id
        message.setContent(content); // 站内信的内容
        // 5. 调用Service
        messageService.create(message);
        // 6. 返回结果
        return AppResult.success("发送成功");
    }

    @ApiOperation("获取未读数")
    @GetMapping("/getUnreadCount")
    public AppResult<Integer> getUnreadCount (HttpServletRequest request) {
        // 1. 获取当前登录的用户
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        // 2. 调用Service
        Integer count = messageService.selectUnreadCount(user.getId());// 当前登录用户的Id就是接收者Id
        // 3. 返回结果
        return AppResult.success(count);
    }

    /**
     * 查询用户的所有站内信
     *
     * @return 站内信集合
     */
    @ApiOperation("查询用户的所有站内信")
    @GetMapping("/getAll")
    public AppResult<List<Message>> getAll (HttpServletRequest request) {
        // 1. 获取当前登录的用户
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        // 2. 调用Service
        List<Message> messages = messageService.selectByReceiveUserId(user.getId());
        // 3. 返回结果
        return AppResult.success(messages);
    }

    @ApiOperation("更新为已读")
    @PostMapping("/markRead")
    public AppResult markRead (HttpServletRequest request,
                               @ApiParam("站内信Id") @RequestParam("id") @NonNull Long id) {
        // 1. 根据Id查询站内信
        Message message = messageService.selectById(id);
        // 2. 站内信是否存在
        if (message == null || message.getDeleteState() == 1) {
            // 返回错误信息
            return AppResult.failed(ResultCode.FAILED_MESSAGE_NOT_EXISTS);
        }
        // 3. 站内信是不是自己的
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        if (user.getId() != message.getReceiveUserId()) {
            // 返回错误信息
            return AppResult.failed(ResultCode.FAILED_FORBIDDEN);
        }

        // 调用Service, 把状态更新成已读
        messageService.updateStateById(id, (byte) 1);
        // 返回结果
        return AppResult.success();
    }

    /**
     * 回复站内信
     * @param repliedId 要回复的站内信Id
     * @param content 站内信的内容
     * @return AppResult
     */
    @ApiOperation("回复站内信")
    @PostMapping("/reply")
    public AppResult reply (HttpServletRequest request,
                            @ApiParam("要回复的站内信Id") @RequestParam("repliedId") @NonNull Long repliedId,
                            @ApiParam("站内信的内容") @RequestParam("content") @NonNull String content) {
        // 校验当前登录用户的状态
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        if (user.getState() == 1) {
            // 返回错误描述
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }

        // 校验要回复的站内信状态
        Message existsMessage = messageService.selectById(repliedId);
        if (existsMessage == null || existsMessage.getDeleteState() == 1) {
            // 返回错误描述
            return AppResult.failed(ResultCode.FAILED_MESSAGE_NOT_EXISTS);
        }
        // 不能给自己回复
        if (user.getId() == existsMessage.getPostUserId()) {
            // 返回错误描述
            return AppResult.failed("不能回复自己的站内信");
        }
        // 构造对象
        Message message = new Message();
        message.setPostUserId(user.getId()); // 发送者
        message.setReceiveUserId(existsMessage.getPostUserId()); // 接收者
        message.setContent(content); // 内容
        // 调用Service
        messageService.reply(repliedId, message);
        // 返回结果
        return AppResult.success();
    }

}
