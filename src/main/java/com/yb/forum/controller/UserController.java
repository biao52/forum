package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.config.AppConfig;
import com.yb.forum.model.User;
import com.yb.forum.services.IUserService;
import com.yb.forum.utils.MD5Util;
import com.yb.forum.utils.StringUtil;
import com.yb.forum.utils.UUIDUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @Author 比特就业课
 */

// 对Controller进行API接口的描述
@Api(tags = "用户接口")
// 日志注解
@Slf4j
// 这是一个返回数据的Controller
@RestController
// 路由映射，一级路径
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    /**
     * 用户注册
     * @param username 用户名
     * @param nickname 用户昵称
     * @param password 密码
     * @param passwordRepeat 重复密码
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public AppResult register (@ApiParam("用户名") @RequestParam("username") @NonNull String username,
                               @ApiParam("昵称") @RequestParam("nickname") @NonNull String nickname,
                               @ApiParam("密码") @RequestParam("password") @NonNull String password,
                               @ApiParam("确认密码") @RequestParam("passwordRepeat") @NonNull String passwordRepeat) {
//        if (StringUtil.isEmpty(username) || StringUtil.isEmpty(nickname)
//        || StringUtil.isEmpty(password) || StringUtil.isEmpty(passwordRepeat)) {
//            // 返回错误信息
//            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE);
//        }

        // 校验密码与重复密码是否相同
        if (!password.equals(passwordRepeat)) {
            log.warn(ResultCode.FAILED_TWO_PWD_NOT_SAME.toString());
            // 返回错误信息
            return AppResult.failed(ResultCode.FAILED_TWO_PWD_NOT_SAME);
        }
        // 准备数据
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        // 处理密码
        // 1. 生成盐
        String salt = UUIDUtil.UUID_32();
        // 2. 生成密码的密文
        String encryptPassword = MD5Util.md5Salt(password, salt);
        // 3. 设置密码和盐
        user.setPassword(encryptPassword);
        user.setSalt(salt);

        // 调用Service层
        userService.createNormalUser(user);
        // 返回成功
        return AppResult.success();
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public AppResult login (HttpServletRequest request,
                            @ApiParam("用户名") @RequestParam("username") @NonNull String username,
                            @ApiParam("密码") @RequestParam("password") @NonNull String password) {
        // 1. 调用Service中的登录方法，返回User对象
        User user = userService.login(username, password);
        if (user == null) {
            // 打印日志
            log.warn(ResultCode.FAILED_LOGIN.toString());
            // 返回结果
            return AppResult.failed(ResultCode.FAILED_LOGIN);
        }
        // 2. 如果登录成功把User对象设置到Session作用域中
        HttpSession session = request.getSession(true);
        session.setAttribute(AppConfig.USER_SESSION, user);
        // 3. 返回结果
        return AppResult.success();
    }


    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public AppResult<User> getUserInfo (HttpServletRequest request,
                                        @ApiParam("用户Id") @RequestParam(value = "id", required = false) Long id) {
        // 定义返回的User对象
        User user = null;
        // 根据Id的值判断User对象的获取方式
        if (id == null) {
            // 1. 如果id为空，从session中获取当前登录的用户信息
            HttpSession session = request.getSession(false);
            // 从session中获取当前登录的用户信息
            user = (User) session.getAttribute(AppConfig.USER_SESSION);
        } else {
            // 2. 如果id不为空，从数据库中按Id查询出用户信息
            user = userService.selectById(id);
        }
        // 判断用户对象是否为空
        if (user == null) {
            return AppResult.failed(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        // 返回正常的结果
        return AppResult.success(user);
    }


    @ApiOperation("退出登录")
    @GetMapping("/logout")
    public AppResult logout (HttpServletRequest request) {
        // 获取session对象
        HttpSession session = request.getSession(false);
        // 判断session是否有效
        if (session != null) {
            // 打印日志
            log.info("退出成功");
            // 表示用户在登录状态，直接销毁session
            session.invalidate();
        }

        return AppResult.success("退出成功");
    }

    /**
     * 修改个人信息
     * @param username 用户名
     * @param nickname 昵称
     * @param gender 性别
     * @param email 邮箱
     * @param phoneNum 电话号
     * @param remark 个人简介
     * @return AppResult
     */
    @ApiOperation("修改个人信息")
    @PostMapping("/modifyInfo")
    public AppResult modifyInfo (HttpServletRequest request,
                                 @ApiParam("用户名") @RequestParam(value = "username",required = false) String username,
                                 @ApiParam("昵称") @RequestParam(value = "nickname",required = false) String nickname,
                                 @ApiParam("性别") @RequestParam(value = "gender",required = false) Byte gender,
                                 @ApiParam("邮箱") @RequestParam(value = "email",required = false) String email,
                                 @ApiParam("电话号") @RequestParam(value = "phoneNum",required = false) String phoneNum,
                                 @ApiParam("个人简介") @RequestParam(value = "remark",required = false) String remark) {
        // 1. 接收参数
        // 2. 对参数做非空校验（全部都为空，则返回错误描述）
        if (StringUtil.isEmpty(username) && StringUtil.isEmpty(nickname)
           && StringUtil.isEmpty(email) && StringUtil.isEmpty(phoneNum)
           && StringUtil.isEmpty(remark) && gender == null) {
            // 返回错误信息
            return AppResult.failed("请输入要修改的内容");
        }

        // 从session中获取用户Id
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);

        // 3. 封装对象
        User updateUser = new User();
        updateUser.setId(user.getId()); // 用户Id
        updateUser.setUsername(username); // 用户名
        updateUser.setNickname(nickname); // 昵称
        updateUser.setGender(gender); // 性别
        updateUser.setEmail(email); // 邮箱
        updateUser.setPhoneNum(phoneNum); // 电话
        updateUser.setRemark(remark); // 个人简介

        // 4. 调用Service中的方法
        userService.modifyInfo(updateUser);
        // 5. 查询最新的用户信息
        user = userService.selectById(user.getId());
        // 6. 把最新的用户信息设置到session中
        session.setAttribute(AppConfig.USER_SESSION, user);
        // 7. 返回结果
        return AppResult.success(user);
    }

    @ApiOperation("修改密码")
    @PostMapping("/modifyPwd")
    public AppResult modifyPassword (HttpServletRequest request,
                                     @ApiParam("原密码") @RequestParam("oldPassword") @NonNull String oldPassword,
                                     @ApiParam("新密码") @RequestParam("newPassword") @NonNull String newPassword,
                                     @ApiParam("确认密码") @RequestParam("passwordRepeat") @NonNull String passwordRepeat) {
        // 1. 校验新密码与确认密码是否相同
        if (!newPassword.equals(passwordRepeat)) {
            // 返回错误描述
            return AppResult.failed(ResultCode.FAILED_TWO_PWD_NOT_SAME);
        }
        // 2. 获取当前登录的用户信息
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);
        // 3. 调用Service
        userService.modifyPassword(user.getId(), newPassword, oldPassword);
        // 4. 销毁session
        if (session != null) {
            session.invalidate();
        }
        // 5. 返回结果
        return AppResult.success();
    }


}
