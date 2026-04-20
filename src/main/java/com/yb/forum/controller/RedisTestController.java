package com.yb.forum.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

// 添加一个测试接口验证 Redis 是否连通

@RestController
@RequestMapping("/test")
public class RedisTestController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/redis/ping")
    public String pingRedis() {
        try {
            redisTemplate.opsForValue().set("test:key", "hello", 10, TimeUnit.SECONDS);
            Object value = redisTemplate.opsForValue().get("test:key");
            return "Redis OK: " + value;
        } catch (Exception e) {
            return "Redis Error: " + e.getMessage();
        }
    }
}
