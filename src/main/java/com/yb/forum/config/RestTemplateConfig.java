package com.yb.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * RestTemplate配置类
 */
@Configuration
public class RestTemplateConfig {

    @Resource
    private DifyConfig difyConfig;

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }
    
    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间（毫秒）
        factory.setConnectTimeout(difyConfig.getTimeout());
        // 设置读取超时时间（毫秒）
        factory.setReadTimeout(difyConfig.getTimeout());
        return factory;
    }
}