package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.exception.ApplicationException;
import com.yb.forum.model.User;
import com.yb.forum.services.IUserService;
import com.yb.forum.utils.JwtUtil;
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
import com.yb.forum.utils.MinioUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Resource
    private MinioUtil minioUtil;

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
    public AppResult login (@ApiParam("用户名") @RequestParam("username") String username,
                            @ApiParam("密码") @RequestParam("password") String password) {
        // 1. 调用 Service 中的登录方法，返回 User 对象
        User user = userService.login(username, password);
        if (user == null) {
            log.warn(ResultCode.FAILED_LOGIN.toString());
            return AppResult.failed(ResultCode.FAILED_LOGIN);
        }

        // 2. 生成 JWT 令牌
        String token = JwtUtil.generateToken(user);

        // 3. 将用户信息存入 Redis 缓存（用于快速查询）
        String cacheKey = USER_INFO_KEY + user.getId();
        redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);

        // ================= 新增逻辑：判断角色并分配跳转路径 =================
        String redirectUrl = "/index.html"; // 默认普通用户跳转到前台首页
        // 假设实体类中 1 代表管理员，0 代表普通用户
        if (user.getIsAdmin() != null && user.getIsAdmin() == 1) {
            redirectUrl = "/admin.html"; // 管理员跳转到后台管理页面
        }
        // ==============================================================

        // 4. 返回令牌、用户信息以及动态计算的跳转路径
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("user", user);
        responseData.put("redirectUrl", redirectUrl); // 将路径传给前端

        return AppResult.success(responseData);
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public AppResult<User> getUserInfo (HttpServletRequest request,
                                        @ApiParam("用户 Id") @RequestParam(value = "id", required = false) Long id) {
        User user = null;

        if (id == null) {
            // 从 JWT 令牌中获取用户 ID
            String token = request.getHeader("Authorization");
            log.info("获取用户信息 - Authorization 头: {}", token != null ? "存在" : "不存在");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                log.info("获取用户信息 - Token 值: {}", token.substring(0, Math.min(50, token.length())) + "...");
                try {
                    Long userId = JwtUtil.getUserIdFromToken(token);
                    id = userId;
                    log.info("获取用户信息 - 从 token 中解析出 userId: {}", userId);
                } catch (Exception e) {
                    log.error("获取用户信息 - JWT 解析失败", e);
                }
            }
        }
        
        // 如果仍然没有用户 ID，返回错误
        if (id == null) {
            log.warn("获取用户信息 - 没有 userId，返回未授权错误");
            return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED);
        }

        // 尝试从 Redis 获取缓存
        String cacheKey = USER_INFO_KEY + id;
        user = (User) redisTemplate.opsForValue().get(cacheKey);

        if (user != null) {
            log.info("从 Redis 缓存获取用户信息，id: {}", id);
            return AppResult.success(user);
        }

        // 缓存未命中，从数据库查询
        log.info("从数据库查询用户信息，id: {}", id);
        user = userService.selectById(id);

        if (user != null) {
            // 存入 Redis 缓存
            String cacheKey1 = USER_INFO_KEY + id;
            redisTemplate.opsForValue().set(cacheKey1, user, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        }

        if (user == null) {
            return AppResult.failed(ResultCode.FAILED_USER_NOT_EXISTS);
        }

        return AppResult.success(user);
    }





    @ApiOperation("上传并修改用户头像")
    @PostMapping("/updateAvatar")
    public AppResult updateAvatar(HttpServletRequest request,
                                  @ApiParam("头像文件") @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "请选择要上传的图片");
        }

        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                userId = JwtUtil.getUserIdFromToken(token.substring(7));
            } catch (Exception e) {
                log.error("JWT解析失败");
            }
        }
        if (userId == null) {
            return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED);
        }

        try {
            String newAvatarUrl = minioUtil.uploadAvatar(file);

            // ✅ 直接构造 User 对象走 updateByPrimaryKeySelective，
            //    不走 modifyInfo（modifyInfo 没有处理 avatarUrl 字段）
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setAvatarUrl(newAvatarUrl);
            updateUser.setUpdateTime(new Date());
            userService.updateAvatarById(updateUser); // 见下方新增方法

            // 更新 Redis 缓存
            String cacheKey = USER_INFO_KEY + userId;
            User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                cachedUser.setAvatarUrl(newAvatarUrl);
                redisTemplate.opsForValue().set(cacheKey, cachedUser, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }


            return AppResult.success(newAvatarUrl);
        } catch (Exception e) {
            log.error("头像上传失败: ", e);
            return AppResult.failed(ResultCode.ERROR_UPLOADED_IMAGE);
        }
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public AppResult logout (HttpServletRequest request) {
        // 从 JWT 令牌中获取用户 ID
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // JWT 解析失败
            }
        }
        
        // 清除 Redis 中的用户缓存
        if (userId != null) {
            String cacheKey = USER_INFO_KEY + userId;
            redisTemplate.delete(cacheKey);
            log.info("清除用户缓存：{}", cacheKey);
        }
        
        log.info("退出成功");
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
            return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "请输入要修改的内容");
        }

        // 从 JWT 令牌中获取用户 ID
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // JWT 解析失败
            }
        }
        
        // 如果没有用户 ID，返回错误
        if (userId == null) {
            return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED);
        }
        
        // 查询用户信息
        User user = userService.selectById(userId);
        if (user == null) {
            return AppResult.failed(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        
        // 校验用户是否被禁言
        if (user.getState() == 1) {
            return AppResult.failed(ResultCode.FAILED_USER_BANNED);
        }

        // 校验昵称格式
        if (!StringUtil.isEmpty(nickname)) {
            String nicknameError = ValidationUtil.validateNickname(nickname);
            if (nicknameError != null) {
                return AppResult.failed(ResultCode.FAILED_NICKNAME_INVALID, nicknameError);
            }
            // 校验昵称是否已存在
            User checkUser = userService.selectByNickname(nickname);
            if (checkUser != null && !checkUser.getId().equals(user.getId())) {
                return AppResult.failed(ResultCode.FAILED_USER_EXISTS, "昵称已存在");
            }
        }
        
        // 校验邮箱格式
        if (!StringUtil.isEmpty(email)) {
            if (!ValidationUtil.isValidEmail(email)) {
                return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "邮箱格式不正确");
            }
        }
        
        // 校验手机号格式
        if (!StringUtil.isEmpty(phoneNum)) {
            if (!ValidationUtil.isValidPhoneNum(phoneNum)) {
                return AppResult.failed(ResultCode.FAILED_PARAMS_VALIDATE, "手机号格式不正确");
            }
        }

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
        
        // 从 JWT 令牌中获取用户 ID
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = JwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // JWT 解析失败
            }
        }
        
        // 如果没有用户 ID，返回错误
        if (userId == null) {
            return AppResult.failed(ResultCode.FAILED_UNAUTHORIZED);
        }

        // 调用Service
        userService.modifyPassword(userId, newPassword, oldPassword);

        // 清除Redis缓存
        String cacheKey = USER_INFO_KEY + userId;
        redisTemplate.delete(cacheKey);
        log.info("清除用户缓存（修改密码）: {}", cacheKey);
        return AppResult.success("修改成功");
    }

    @ApiOperation("管理员-获取用户列表")
    @GetMapping("/admin/list")
    public AppResult<List<User>> getAdminUserList(@RequestParam(value = "username", required = false) String username) {
        List<User> users = userService.selectAll(username);
        return AppResult.success(users);
    }

    @ApiOperation("管理员-禁言/解禁用户")
    @PostMapping("/admin/ban")
    public AppResult banUser(@RequestParam("id") Long id, @RequestParam("state") Byte state) {
        userService.updateUserState(id, state);
        
        // 清除用户信息缓存
        String userCacheKey = USER_INFO_KEY + id;
        redisTemplate.delete(userCacheKey);
        
        // 清除文章列表缓存（因为文章列表中包含了作者的状态信息）
        java.util.Set<String> articleListKeys = redisTemplate.keys("article:list:*");
        if (articleListKeys != null && !articleListKeys.isEmpty()) {
            redisTemplate.delete(articleListKeys);
        }
        
        return AppResult.success();
    }

    @ApiOperation("管理员-删除用户")
    @PostMapping("/admin/delete")
    public AppResult deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return AppResult.success();
    }
}