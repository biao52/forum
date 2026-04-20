package com.yb.forum.services.impl;

import com.yb.forum.dao.ArticleMapper;
import com.yb.forum.dao.BoardMapper;
import com.yb.forum.dao.UserMapper;
import com.yb.forum.model.DashboardStatVO;
import com.yb.forum.model.UserGrowthVO;
import com.yb.forum.services.IAdminDashboardService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
@Service
public class AdminDashboardServiceImpl implements IAdminDashboardService {
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private BoardMapper boardMapper;

    @Override
    public DashboardStatVO getStatistics() {
        DashboardStatVO statVO = new DashboardStatVO();
        statVO.setArticleCount(articleMapper.selectTotalCount());
        statVO.setUserCount(userMapper.selectTotalCount());
        statVO.setBoardCount(boardMapper.selectTotalCount());
        statVO.setTodayArticleCount(articleMapper.selectTodayCount());
        return statVO;
    }

    @Override
    public UserGrowthVO getUserGrowthLast7Days() {
        UserGrowthVO growthVO = new UserGrowthVO();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        // 1. 初始化近7天的日期 Map (保证顺序，且默认值为 0)
        Map<String, Integer> dateMap = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");

        // 倒推 6 天到今天，一共 7 天
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            dateMap.put(sdf.format(cal.getTime()), 0);
        }

        // 2. 从数据库获取有数据的日期
        List<Map<String, Object>> dbData = userMapper.selectUserGrowthLast7Days();
        if (dbData != null) {
            for (Map<String, Object> row : dbData) {
                String dateStr = row.get("dateStr").toString();
                Number count = (Number) row.get("count");
                // 如果数据库查询的日期在我们的 Map 中，覆盖掉默认的 0
                if (dateMap.containsKey(dateStr)) {
                    dateMap.put(dateStr, count.intValue());
                }
            }
        }

        // 3. 封装到 VO 中
        dates.addAll(dateMap.keySet());
        counts.addAll(dateMap.values());

        growthVO.setDates(dates);
        growthVO.setCounts(counts);
        return growthVO;
    }
}
