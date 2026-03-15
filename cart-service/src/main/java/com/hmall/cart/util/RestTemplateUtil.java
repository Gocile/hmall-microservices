package com.hmall.cart.util;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @Author Gocile
 * @Version 1.0
 * @Description 工具类，负责注入RestTemplate到Spring容器
 * @Date 2026-03-15 14:53
 */
public class RestTemplateUtil {
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
