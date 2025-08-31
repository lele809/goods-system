package com.shelf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS跨域配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 明确允许的域名（生产环境安全配置）
        config.addAllowedOrigin("https://goods-system-frontend.onrender.com");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        
        // 允许发送Cookie
        config.setAllowCredentials(true);
        
        // 暴露哪些头部信息
        config.setExposedHeaders(Arrays.asList(
                "Authorization", 
                "Content-Type", 
                "X-Requested-With", 
                "accept", 
                "Origin", 
                "Access-Control-Request-Method", 
                "Access-Control-Request-Headers"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 