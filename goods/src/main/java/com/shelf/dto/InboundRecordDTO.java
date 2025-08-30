package com.shelf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入库记录数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboundRecordDTO {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSpec;
    private String productUnit;
    private BigDecimal productPrice;
    private String imageUrl; // 商品图片URL
    private LocalDate inDate;
    private Integer quantity;
    private LocalDateTime createdAt;
    
    // 计算字段
    private BigDecimal totalAmount; // 入库总金额
}