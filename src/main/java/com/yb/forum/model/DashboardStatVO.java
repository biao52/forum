package com.yb.forum.model;
import lombok.Data;
@Data
public class DashboardStatVO {
    private Integer articleCount;      // 文章总数
    private Integer userCount;         // 用户总数
    private Integer boardCount;        // 版块总数
    private Integer todayArticleCount; // 今日新增文章数
}
