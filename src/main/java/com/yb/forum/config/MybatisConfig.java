package com.yb.forum.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 配置Mybatis的扫描路径
 *
 * @Author yangbiao
 */
// 加入 Spring
@Configuration
// 具体的配置
@MapperScan("com.yb.forum.dao")
public class MybatisConfig {
}
