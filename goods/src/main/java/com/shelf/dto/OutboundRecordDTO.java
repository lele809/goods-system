package com.shelf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 出库记录数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundRecordDTO {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSpec;
    private String productUnit;
    private BigDecimal productPrice;
    private String imageUrl; // 商品图片URL
    private LocalDate outDate;
    private Integer quantity;
    private String name; // 姓名
    private Integer paymentStatus; // 付款状态：0-未付款，1-已付款
    private LocalDateTime createdAt;
    
    // 计算字段
    private BigDecimal totalAmount; // 出库总金额
} 