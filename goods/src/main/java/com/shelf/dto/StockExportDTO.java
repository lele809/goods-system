package com.shelf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 库存导出数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockExportDTO {
    
    private Long productId;
    private String productName;
    private String spec;
    private String unit;
    private String imageUrl;        // 商品图片URL
    private Integer stockQuantity;  // 指定日期的库存数量
    private BigDecimal unitPrice;
    private BigDecimal totalValue;  // 库存总价值
    private LocalDate stockDate;    // 库存统计日期
    private Integer initialStock;   // 初始库存
    private Integer totalInbound;   // 截止到统计日期的累计入库量
    private Integer totalOutbound;  // 截止到统计日期的累计出库量
    
    /**
     * 构造方法 - 用于计算库存
     */
    public StockExportDTO(Long productId, String productName, String spec, String unit, 
                         String imageUrl, Integer initialStock, BigDecimal unitPrice, 
                         Integer totalInbound, Integer totalOutbound, LocalDate stockDate) {
        this.productId = productId;
        this.productName = productName;
        this.spec = spec;
        this.unit = unit;
        this.imageUrl = imageUrl;
        this.initialStock = initialStock;
        this.unitPrice = unitPrice;
        this.totalInbound = totalInbound != null ? totalInbound : 0;
        this.totalOutbound = totalOutbound != null ? totalOutbound : 0;
        // 正确的库存计算公式：初始库存 + 累计入库 - 累计出库
        this.stockQuantity = initialStock + this.totalInbound - this.totalOutbound;
        this.totalValue = unitPrice.multiply(BigDecimal.valueOf(this.stockQuantity));
        this.stockDate = stockDate;
    }
} 