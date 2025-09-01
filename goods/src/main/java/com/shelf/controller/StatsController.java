package com.shelf.controller;

import com.shelf.dto.ApiResponse;
import com.shelf.service.ProductService;
import com.shelf.service.OutboundRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 统计数据控制器 - 优化版本，集中管理统计API
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatsController {

    private final ProductService productService;
    private final OutboundRecordService outboundRecordService;

    /**
     * 获取仪表盘统计数据 - 一次性获取所有统计
     */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardStats> getDashboardStats() {
        try {
            DashboardStats stats = new DashboardStats();
            
            // 并行获取所有统计数据（利用缓存）
            stats.setTotalProducts(productService.getProductCount());
            stats.setTotalStock(productService.getTotalStock());
            stats.setStockValue(productService.getStockValue());
            
            // 获取今日出库数据
            try {
                Integer todayOutbound = getTodayOutboundQuantity();
                stats.setTodayOutbound(todayOutbound);
            } catch (Exception e) {
                stats.setTodayOutbound(0);
            }
            
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品总数
     */
    @GetMapping("/products/count")
    public ApiResponse<Long> getProductCount() {
        try {
            Long count = productService.getProductCount();
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error("获取商品总数失败: " + e.getMessage());
        }
    }

    /**
     * 获取库存总量
     */
    @GetMapping("/stock/total")
    public ApiResponse<Integer> getTotalStock() {
        try {
            Integer total = productService.getTotalStock();
            return ApiResponse.success(total);
        } catch (Exception e) {
            return ApiResponse.error("获取库存总量失败: " + e.getMessage());
        }
    }

    /**
     * 获取库存价值
     */
    @GetMapping("/stock/value")
    public ApiResponse<BigDecimal> getStockValue() {
        try {
            BigDecimal value = productService.getStockValue();
            return ApiResponse.success(value);
        } catch (Exception e) {
            return ApiResponse.error("获取库存价值失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日出库数量
     */
    @GetMapping("/outbound/today")
    public ApiResponse<Integer> getTodayOutbound() {
        try {
            Integer count = getTodayOutboundQuantity();
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error("获取今日出库数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日出库数量的内部方法
     */
    private Integer getTodayOutboundQuantity() {
        try {
            LocalDate today = LocalDate.now();
            return outboundRecordService.getTotalOutboundQuantity(today, today);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 仪表盘统计数据DTO
     */
    public static class DashboardStats {
        private Long totalProducts;
        private Integer totalStock;
        private BigDecimal stockValue;
        private Integer todayOutbound;

        // Getters and Setters
        public Long getTotalProducts() {
            return totalProducts;
        }

        public void setTotalProducts(Long totalProducts) {
            this.totalProducts = totalProducts;
        }

        public Integer getTotalStock() {
            return totalStock;
        }

        public void setTotalStock(Integer totalStock) {
            this.totalStock = totalStock;
        }

        public BigDecimal getStockValue() {
            return stockValue;
        }

        public void setStockValue(BigDecimal stockValue) {
            this.stockValue = stockValue;
        }

        public Integer getTodayOutbound() {
            return todayOutbound;
        }

        public void setTodayOutbound(Integer todayOutbound) {
            this.todayOutbound = todayOutbound;
        }
    }
}