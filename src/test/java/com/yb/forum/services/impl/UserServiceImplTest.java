package com.yb.forum.services.impl;

import com.yb.forum.model.User;
import com.yb.forum.services.IUserService;
import com.yb.forum.utils.MD5Util;
import com.yb.forum.utils.UUIDUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@SpringBootTest
class UserServiceImplTest {

    @Resource
    private IUserService userService;

    @Test
    @Transactional
    void createNormalUser() {
        // 构造User对象
        User user = new User();
        user.setUsername("bitboy1");
        user.setNickname("bitboy");

        // 定义一个原始的密码
        String password = "123456";
        // 生成盐
        String salt = UUIDUtil.UUID_32();
        // 生成密码的密文
        String ciphertext = MD5Util.md5Salt(password, salt);
        // 设置加密后的密码
        user.setPassword(ciphertext);
        // 设置盐
        user.setSalt(salt);
        // 调用Service层的方法
        userService.createNormalUser(user);
        // 打印结果
        System.out.println(user);
    }

    @Test
    void selectByUserName() {
        User user = userService.selectByUserName("bitboy");
        System.out.println(user);
    }

    @Test
    void login() {
        User user = userService.login("bitboy", "123456");
        System.out.println(user);
    }

    @Test
    void selectById() {
        User user = userService.selectById(1l);
        System.out.println(user);
    }

    @Test
    @Transactional
    void addOneArticleCountById() {
        userService.addOneArticleCountById(1L);
        System.out.println("更新成功");
    }

    @Test
    @Transactional
    void subOneArticleCountById() {
        userService.subOneArticleCountById(6l);
        System.out.println("更新成功");
    }

    @Test
    @Transactional
    void modifyInfo() {
        User user = new User();
        user.setId(3l); // 用户Id
        user.setUsername("testUser"); // 登录名
        user.setNickname("testUser1"); // 昵称
        user.setGender(null); // 性别
        user.setEmail("qqq@qq.com");// 邮箱
        user.setPhoneNum("15366668888"); // 电话
        user.setRemark("测试"); // 个人简介
        // 调用Service
        userService.modifyInfo(user);

    }

    @Test
    @Transactional
    void modifyPassword() {
        userService.modifyPassword(1l, "123456", "111111");
        System.out.println("更新成功");
    }
}