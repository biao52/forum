package com.yb.forum.interceptor;

import com.yb.forum.config.AppConfig;
import com.yb.forum.utils.JwtUtil;
import io.jsonwebtoken.Claims;
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
     * 前置处理 (对请求的预处理)
     * @return true : 继续流程 <br/> false : 流程中断
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求的 URL
        String requestURI = request.getRequestURI();
        
        // 如果是登录、注册等接口，直接放行（这些接口已经在配置中排除，但为了安全再次检查）
        if (requestURI.contains("/user/login") || 
            requestURI.contains("/user/register") ||
            requestURI.contains("/sign-in.html") ||
            requestURI.contains("/sign-up.html")) {
            return true;
        }
        
        // 优先从请求头中获取 JWT 令牌
        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 验证 JWT 令牌是否有效
            if (JwtUtil.isTokenValid(token)) {
                // 令牌有效，将用户信息存入请求属性中
                Claims claims = JwtUtil.parseToken(token);
                Long userId = claims.get("userId", Long.class);
                request.setAttribute("userId", userId);
                request.setAttribute("token", token);
                return true;
            }
        }
        
        // 校验不通过，判断是 AJAX 请求还是页面请求
        String xRequestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // AJAX 请求，返回 401 状态码
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1001,\"message\":\"请先登录\",\"data\":null}");
        } else {
            // 页面请求，重定向到登录页
            if (!defaultURL.startsWith("/")) {
                defaultURL = "/" + defaultURL;
            }
            response.sendRedirect(defaultURL);
        }
        // 中断流程
        return false;
    }
}
