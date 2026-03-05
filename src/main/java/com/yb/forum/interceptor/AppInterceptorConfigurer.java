package com.yb.forum.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author yangbiao
 */
// 表示一个配置类
@Configuration
public class AppInterceptorConfigurer implements WebMvcConfigurer {

    // 注入自定义的登录拦截器
    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加登录拦截器 - 只拦截 API 接口，不拦截静态 HTML 页面
        registry.addInterceptor(loginInterceptor)       // 添加用户登录拦截器
                .addPathPatterns("/user/**")            // 拦截用户相关 API
                .addPathPatterns("/article/**")         // 拦截文章相关 API
                .addPathPatterns("/board/**")           // 拦截版块相关 API
                .addPathPatterns("/reply/**")           // 拦截回复相关 API
                .addPathPatterns("/message/**")         // 拦截站内信相关 API
                .excludePathPatterns("/user/login")     // 排除登录 api 接口
                .excludePathPatterns("/user/register")  // 排除注册 api 接口
                .excludePathPatterns("/swagger*/**")    // 排除 swagger 下所有
                .excludePathPatterns("/v3*/**")         // 排除 v3 下所有，与 swagger 相关
                .excludePathPatterns("/dist/**")        // 排除所有静态文件
                .excludePathPatterns("/image/**")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/test/**")
                .excludePathPatterns("/**.ico");
    }
}
