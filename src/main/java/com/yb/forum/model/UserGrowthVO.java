package com.yb.forum.model;


import lombok.Data;
import java.util.List;

@Data
public class UserGrowthVO {
    private List<String> dates;  // 日期列表，如 ["04-11", "04-12", ...]
    private List<Integer> counts;// 对应日期的注册数量
}
