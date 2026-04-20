package com.yb.forum.services;

import com.yb.forum.model.DashboardStatVO;
import com.yb.forum.model.UserGrowthVO;

public interface IAdminDashboardService {
    // 获取概览统计数据
    DashboardStatVO getStatistics();

    // 获取近7天用户增长数据
    UserGrowthVO getUserGrowthLast7Days();
}
