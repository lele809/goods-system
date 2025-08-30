package com.shelf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    
    private Long id;
    private String name;
    private String spec;
    private String unit;
    private Integer initialStock;
    private BigDecimal price;
    private BigDecimal amount; // 金额/元
    private Integer remainingQuantity; // 剩余数量
    private String imageUrl; // 商品图片URL
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 计算字段
    private Integer currentStock; // 当前库存（初始库存-出库总量）
    private Integer totalOutbound; // 总出库量
    private BigDecimal totalValue; // 库存总价值
}