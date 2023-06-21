package com.bitejiuyeke.forum.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 比特就业课
 */
//  表示返回的结果是数据
@RestController
// 定义一级映射路径
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/hello")
    public String hello () {
        return "hello, Spring boot...";
    }
}
