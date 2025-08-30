package com.shelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 货架商品管理系统主启动类
 */
@SpringBootApplication
public class ShelfApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShelfApplication.class, args);
        System.out.println("货架商品管理系统启动成功！");
        System.out.println("访问地址: http://localhost:8084");
        System.out.println("API文档: http://localhost:8084/api");
    }
} 