package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.config.AppConfig;
import com.yb.forum.exception.ApplicationException;
import com.yb.forum.model.User;
import com.yb.forum.services.IUserService;
import com.yb.forum.utils.MD5Util;
import com.yb.forum.utils.StringUtil;
import com.yb.forum.utils.UUIDUtil;
import com.yb.forum.utils.ValidationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * @Author yangbiao
 */
@Api(tags = "用户接口")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_INFO_KEY = "user:info:";
    private static final long CACHE_EXPIRE_TIME = 30; // 缓存过期时间（分钟）

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public AppResult register (@ApiParam("用户名") @RequestParam(value = "username", required = false) String username,
                               @ApiParam("昵称") @RequestParam(value = "nickname", required = false) String nickname,
                               @ApiParam("密码") @RequestParam(value = "password", required = false) String password,
                               @ApiParam("确认密码") @RequestParam(value = "passwordRepeat", required = false) String passwordRepeat) {
        // 1. 检查必要参数是否存在（null 检查）
        if (username == null) {
            return AppResult.failed(ResultCode.FAILED_USERNAME_INVALID, "用户名不能为空");
        }
        if (nickname == null) {
            return AppResult.failed(ResultCode.FAILED_NICKNAME_INVALID, "昵称不能为空");
        }
        if (password == null) {
            return AppResult.failed(ResultCode.FAILED_PASSWORD_INVALID, "密码不能为空");
        }
        if (passwordRepeat == null) {
            return AppResult.failed(ResultCode.FAILED_PASSWORD_INVALID, "确认密码不能为空");
        }

        // 2. 检查是否为空字符串（去除空格后检查）
        if (username.trim().isEmpty()) {
            return AppResult.failed(ResultCode.FAILED_USERNAME_INVALID, "用户名不能为空");
        }
        if (nickname.trim().isEmpty()) {
            return AppResult.failed(ResultCode.FAILED_NICKNAME_INVALID, "昵称不能为空");
        }
        if (password.trim().isEmpty()) {
            return AppResult.failed(ResultCode.FAILED_PASSWORD_INVALID, "密码不能为空");
        }
        if (passwordRepeat.trim().isEmpty()) {
            return AppResult.failed(ResultCode.FAILED_PASSWORD_INVALID, "确认密码不能为空");
        }

        // 3. 去除首尾空格
        username = username.trim();
        nickname = nickname.trim();

        // 4. 校验用户名格式
        String usernameError = ValidationUtil.validateUsername(username);
        if (usernameError != null) {
            return AppResult.failed(ResultCode.FAILED_USERNAME_INVALID, usernameError);
        }

        // 5. 校验昵称格式
        String nicknameError = ValidationUtil.validateNickname(nickname);
        if (nicknameError != null) {
            return AppResult.failed(ResultCode.FAILED_NICKNAME_INVALID, nicknameError);
        }

        // 6. 校验密码格式和强度
        String passwordError = ValidationUtil.validatePassword(password);
        if (passwordError != null) {
            // 区分是长度问题还是强度问题
            if (passwordError.contains("长度")) {
                return AppResult.failed(ResultCode.FAILED_PASSWORD_INVALID, passwordError);
            } else if (passwordError.contains("字母和数字")) {
                return AppResult.failed(ResultCode.FAILED_PASSWORD_INVALID, passwordError);
            } else if (passwordError.contains("强度")) {
                return AppResult.failed(ResultCode.FAILED_PASSWORD_TOO_WEAK, passwordError);
            } else {
                return AppResult.failed(ResultCode.FAILED_PASSWORD_INVALID, passwordError);
            }
        }

        // 7. 校验两次密码是否一致
        if (!password.equals(passwordRepeat)) {
            return AppResult.failed(ResultCode.FAILED_TWO_PWD_NOT_SAME);
        }

        // 8. 准备数据
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        // 处理密码
        String salt = UUIDUtil.UUID_32();
        String encryptPassword = MD5Util.md5Salt(password, salt);
        user.setPassword(encryptPassword);
        user.setSalt(salt);

        // 9. 调用 Service 层
        try {
            userService.createNormalUser(user);
        } catch (ApplicationException e) {
            // 如果是用户已存在的异常，返回对应的错误码
            if (e.getMessage().contains("用户已存在")) {
                return AppResult.failed(ResultCode.FAILED_USER_EXISTS);
            }
            // 其他异常向上抛出
            throw e;
        }

        return AppResult.success();
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public AppResult login (HttpServletRequest request,
                            @ApiParam("用户名") @RequestParam("username") String username,
                            @ApiParam("密码") @RequestParam("password") String password) {
        // 1. 调用Service中的登录方法，返回User对象
        User user = userService.login(username, password);
        if (user == null) {
            log.warn(ResultCode.FAILED_LOGIN.toString());
            return AppResult.failed(ResultCode.FAILED_LOGIN);
        }
        // 2. 如果登录成功把User对象设置到Session作用域中
        HttpSession session = request.getSession(true);
        session.setAttribute(AppConfig.USER_SESSION, user);

        // 3. 将用户信息存入Redis缓存
        String cacheKey = USER_INFO_KEY + user.getId();
        redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        return AppResult.success();
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public AppResult<User> getUserInfo (HttpServletRequest request,
                                        @ApiParam("用户Id") @RequestParam(value = "id", required = false) Long id) {
        User user = null;

        if (id == null) {
            // 从session中获取当前登录的用户信息
            HttpSession session = request.getSession(false);
            user = (User) session.getAttribute(AppConfig.USER_SESSION);
        } else {
            // 尝试从Redis获取缓存
            String cacheKey = USER_INFO_KEY + id;
            user = (User) redisTemplate.opsForValue().get(cacheKey);

            if (user != null) {
                log.info("从Redis缓存获取用户信息，id: {}", id);
                return AppResult.success(user);
            }

            // 缓存未命中，从数据库查询
            log.info("从数据库查询用户信息，id: {}", id);
            user = userService.selectById(id);

            if (user != null) {
                // 存入Redis缓存
                String cacheKey1 = USER_INFO_KEY + id;
                redisTemplate.opsForValue().set(cacheKey1, user, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
        }

        if (user == null) {
            return AppResult.failed(ResultCode.FAILED_USER_NOT_EXISTS);
        }

        return AppResult.success(user);
    }

    @ApiOperation("退出登录")
    @GetMapping("/logout")
    public AppResult logout (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(AppConfig.USER_SESSION);
            if (user != null) {
                // 清除Redis中的用户缓存
                String cacheKey = USER_INFO_KEY + user.getId();
                redisTemplate.delete(cacheKey);
                log.info("清除用户缓存: {}", cacheKey);
            }
            log.info("退出成功");
            session.invalidate();
        }
        return AppResult.success("退出成功");
    }

    @ApiOperation("修改个人信息")
    @PostMapping("/modifyInfo")
    public AppResult modifyInfo (HttpServletRequest request,
                                 @ApiParam("用户名") @RequestParam(value = "username",required = false) String username,
                                 @ApiParam("昵称") @RequestParam(value = "nickname",required = false) String nickname,
                                 @ApiParam("性别") @RequestParam(value = "gender",required = false) Byte gender,
                                 @ApiParam("邮箱") @RequestParam(value = "email",required = false) String email,
                                 @ApiParam("电话号") @RequestParam(value = "phoneNum",required = false) String phoneNum,
                                 @ApiParam("个人简介") @RequestParam(value = "remark",required = false) String remark) {
        // 对参数做非空校验
        if (StringUtil.isEmpty(username) && StringUtil.isEmpty(nickname)
                && StringUtil.isEmpty(email) && StringUtil.isEmpty(phoneNum)
                && StringUtil.isEmpty(remark) && gender == null) {
            return AppResult.failed("请输入要修改的内容");
        }

        // 从session中获取用户Id
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);

        // 封装对象
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUsername(username);
        updateUser.setNickname(nickname);
        updateUser.setGender(gender);
        updateUser.setEmail(email);
        updateUser.setPhoneNum(phoneNum);
        updateUser.setRemark(remark);

        // 调用Service中的方法
        userService.modifyInfo(updateUser);

        // 查询最新的用户信息
        user = userService.selectById(user.getId());

        // 把最新的用户信息设置到session中
        session.setAttribute(AppConfig.USER_SESSION, user);

        // 更新Redis缓存
        String cacheKey = USER_INFO_KEY + user.getId();
        redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.info("更新用户缓存: {}", cacheKey);

        return AppResult.success(user);
    }

    @ApiOperation("修改密码")
    @PostMapping("/modifyPwd")
    public AppResult modifyPassword (HttpServletRequest request,
                                     @ApiParam("原密码") @RequestParam("oldPassword") String oldPassword,
                                     @ApiParam("新密码") @RequestParam("newPassword") String newPassword,
                                     @ApiParam("确认密码") @RequestParam("passwordRepeat") String passwordRepeat) {
        // 校验新密码与确认密码是否相同
        if (!newPassword.equals(passwordRepeat)) {
            return AppResult.failed(ResultCode.FAILED_TWO_PWD_NOT_SAME);
        }
        // 获取当前登录的用户信息
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute(AppConfig.USER_SESSION);

        // 调用Service
        userService.modifyPassword(user.getId(), newPassword, oldPassword);

        // 清除Redis缓存
        String cacheKey = USER_INFO_KEY + user.getId();
        redisTemplate.delete(cacheKey);
        log.info("清除用户缓存（修改密码）: {}", cacheKey);

        // 销毁session
        if (session != null) {
            session.invalidate();
        }

        return AppResult.success();
    }
}