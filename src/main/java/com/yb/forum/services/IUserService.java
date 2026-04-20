package com.yb.forum.services;
import com.yb.forum.model.User;

import java.util.List;

/**
 * 用户接口
 */
public interface IUserService {

    /**
     * 创建一个普通用户
     *
     * @param user 用户信息
     */
    void createNormalUser (User user);

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUserName (String username);
    
    /**
     * 根据昵称查询用户信息
     * @param nickname 昵称
     * @return 用户信息
     */
    User selectByNickname (String nickname);

    /**
     * 处理用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    User login (String username, String password);

    /**
     * 根据Id查询用户信息
     * @param id 用户Id
     * @return User对象
     */
    User selectById (Long id);

    /**
     * 用户发帖数 +1
     * @param id 用户Id
     */
    void addOneArticleCountById (Long id);

    /**
     * 用户发帖数 -1
     * @param id 版块Id
     */
    void subOneArticleCountById (Long id);


    /**
     * 修改个人信息
     * @param user 要更新的对象
     */
    void modifyInfo (User user);

    /**
     * 修改密码
     * @param id 用户Id
     * @param newPassword 新密码
     * @param oldPassword 老密码
     */
    void modifyPassword (Long id, String newPassword, String oldPassword);


    List<User> selectAll(String username);


    void updateUserState(Long id, Byte state);

    void deleteUser(Long id);














}
