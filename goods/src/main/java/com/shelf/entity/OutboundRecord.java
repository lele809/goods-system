package com.shelf.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 出库记录实体类
 */
@Entity
@Table(name = "outbound_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "out_date", nullable = false)
    private LocalDate outDate;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus = 0; // 0-未付款，1-已付款

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 关联商品实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
} 