package com.yb.forum.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 配置Mybatis的扫描路径
 *
 * @Author 比特就业课
 */
// 加入Spring
@Configuration
// 具体的配置
@MapperScan("com.bitejiuyeke.forum.dao")
public class MybatisConfig {
}
