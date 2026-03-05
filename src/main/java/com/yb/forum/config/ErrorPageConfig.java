package com.yb.forum.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * 错误页面配置
 * 
 * @Author yangbiao
 */
@Configuration
public class ErrorPageConfig implements ErrorPageRegistrar {

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        // 配置 404 错误页面
        ErrorPage error404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error/404");
        registry.addErrorPages(error404);
    }
}
