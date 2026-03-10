package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.common.ResultCode;
import com.yb.forum.model.DifyResponse;
import com.yb.forum.services.IDifyService;
import com.yb.forum.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Dify智能体控制器
 */
@Api(tags = "智能助手接口")
@Slf4j
@RestController
@RequestMapping("/dify")
public class DifyController {

    @Resource
    private IDifyService iDifyService;
    
    /**
     * 调用智能助手
     * @param request HTTP请求
     * @param query 用户查询内容
     * @return 智能助手回答
     */
    @ApiOperation("调用智能助手")
    @PostMapping("/chat")
    public AppResult chat(HttpServletRequest request, 
                         @ApiParam("查询内容") @RequestParam("query") String query) {
        try {
            // 从请求头获取token并解析用户ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            // 调用Dify服务
            DifyResponse response = iDifyService.callDify(query, userId.toString());
            
            // 返回结果
            return AppResult.success(ResultCode.SUCCESS.getMessage(), response.getAnswer());
        } catch (Exception e) {
            log.error("调用智能助手失败: {}", e.getMessage(), e);
            return AppResult.failed(ResultCode.FAILED, "调用智能助手失败: " + e.getMessage());
        }
    }
}