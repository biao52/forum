package com.yb.forum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 错误页面控制器
 * 
 * @Author yangbiao
 */

@Slf4j
@Controller
public class ErrorPageController {

    /**
     * 处理 404 错误
     * @return 404 页面
     */
    @GetMapping("/error/404")
    public String error404() {
        return "forward:/404.html";
    }
}
