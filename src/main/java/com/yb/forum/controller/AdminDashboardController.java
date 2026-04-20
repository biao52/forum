package com.yb.forum.controller;

import com.yb.forum.common.AppResult;
import com.yb.forum.model.DashboardStatVO;
import com.yb.forum.model.UserGrowthVO;
import com.yb.forum.services.IAdminDashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@Api(tags = "后台数据看板接口")
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Resource
    private IAdminDashboardService adminDashboardService;

    @ApiOperation("获取基础统计数据")
    @GetMapping("/statistics")
    public AppResult<DashboardStatVO> getStatistics() {
        // 注：生产环境中，这里应该有鉴权逻辑，判断当前登录用户是否为 isAdmin = 1
        DashboardStatVO statVO = adminDashboardService.getStatistics();
        return AppResult.success(statVO);
    }

    @ApiOperation("获取近7天用户增长曲线数据")
    @GetMapping("/userGrowth")
    public AppResult<UserGrowthVO> getUserGrowth() {
        UserGrowthVO growthVO = adminDashboardService.getUserGrowthLast7Days();
        return AppResult.success(growthVO);
    }
}
