package com.bitejiuyeke.forum.services;


import com.bitejiuyeke.forum.model.User;

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


}
