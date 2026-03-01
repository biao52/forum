package com.yb.forum.interceptor;

import com.yb.forum.config.AppConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 *
 * @Author yangbiao
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Value("${bit-forum.login.url}")
    private String defaultURL;
    /**
     * 前置处理(对请求的预处理)
     * @return true : 继续流程 <br/> false : 流程中断
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取session 对象
        HttpSession session = request.getSession(false);
        // 判断session是否有效
        if (session != null && session.getAttribute(AppConfig.USER_SESSION) != null) {
            // 用户为已登录状，校验通过
            return true;
        }
        // 校验URL是否正确
        if (!defaultURL.startsWith("/")) {
            defaultURL = "/" + defaultURL;
        }
        // 校验不通过, 跳转到登录页面
        response.sendRedirect(defaultURL);
        // 中断流程
        return false;

    }
}
