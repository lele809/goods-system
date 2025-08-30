package com.shelf.service;

import com.shelf.dto.InboundRecordDTO;
import com.shelf.entity.InboundRecord;
import com.shelf.entity.Product;
import com.shelf.repository.InboundRecordRepository;
import com.shelf.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


/**
 * 入库记录业务逻辑服务
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InboundRecordService {

    private final InboundRecordRepository inboundRecordRepository;
    private final ProductRepository productRepository;

    /**
     * 分页查询入库记录
     */
    @Transactional(readOnly = true)
    public Page<InboundRecordDTO> getInboundRecords(Long productId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            Page<InboundRecord> records = inboundRecordRepository.findByMultipleConditions(
                    productId, startDate, endDate, pageable);
            
            return records.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("查询入库记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分页查询入库记录（支持商品名称搜索）
     */
    @Transactional(readOnly = true)
    public Page<InboundRecordDTO> getInboundRecords(Long productId, String productName, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            Page<InboundRecord> records = inboundRecordRepository.findByMultipleConditionsWithProductName(
                    productId, productName, startDate, endDate, pageable);
            
            return records.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("查询入库记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取入库记录
     */
    @Transactional(readOnly = true)
    public InboundRecordDTO getInboundRecordById(Long id) {
        InboundRecord record = inboundRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入库记录不存在，ID: " + id));
        return convertToDTO(record);
    }

    /**
     * 创建入库记录
     */
    public InboundRecordDTO createInboundRecord(InboundRecordDTO dto) {
        // 验证商品是否存在
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + dto.getProductId()));

        // 创建入库记录
        InboundRecord record = new InboundRecord();
        record.setProductId(dto.getProductId());
        record.setQuantity(dto.getQuantity());
        record.setInDate(dto.getInDate());
        record.setImageUrl(product.getImageUrl());

        // 保存入库记录
        InboundRecord savedRecord = inboundRecordRepository.save(record);

        // 更新商品库存（增加库存）
        product.setRemainingQuantity(product.getRemainingQuantity() + dto.getQuantity());
        productRepository.save(product);

        return convertToDTO(savedRecord);
    }

    /**
     * 更新入库记录
     */
    public InboundRecordDTO updateInboundRecord(InboundRecordDTO dto) {
        // 获取原入库记录
        InboundRecord existingRecord = inboundRecordRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("入库记录不存在，ID: " + dto.getId()));

        // 验证商品是否存在
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + dto.getProductId()));

        // 计算库存变化量
        int quantityDiff = dto.getQuantity() - existingRecord.getQuantity();

        // 更新入库记录
        existingRecord.setProductId(dto.getProductId());
        existingRecord.setQuantity(dto.getQuantity());
        existingRecord.setInDate(dto.getInDate());
        existingRecord.setImageUrl(product.getImageUrl());

        // 保存更新后的入库记录
        InboundRecord updatedRecord = inboundRecordRepository.save(existingRecord);

        // 更新商品库存
        product.setRemainingQuantity(product.getRemainingQuantity() + quantityDiff);
        productRepository.save(product);

        return convertToDTO(updatedRecord);
    }

    /**
     * 删除入库记录
     */
    public void deleteInboundRecord(Long id) {
        InboundRecord record = inboundRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入库记录不存在，ID: " + id));

        // 获取关联的商品
        Product product = productRepository.findById(record.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + record.getProductId()));

        // 更新商品库存（减少库存）
        product.setRemainingQuantity(product.getRemainingQuantity() - record.getQuantity());
        productRepository.save(product);

        // 删除入库记录
        inboundRecordRepository.delete(record);
    }

    /**
     * 获取入库趋势数据
     */
    @Transactional(readOnly = true)
    public List<Object[]> getInboundTrendData(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        return inboundRecordRepository.getInboundTrendData(startDate);
    }

    /**
     * 统计入库总量
     */
    @Transactional(readOnly = true)
    public Integer getTotalInboundQuantity(LocalDate startDate, LocalDate endDate) {
        return inboundRecordRepository.getTotalInboundQuantityByDateRange(startDate, endDate);
    }

    /**
     * 转换为DTO
     */
    private InboundRecordDTO convertToDTO(InboundRecord record) {
        try {
            InboundRecordDTO dto = new InboundRecordDTO();
            dto.setId(record.getId());
            dto.setProductId(record.getProductId());
            dto.setQuantity(record.getQuantity());
            dto.setInDate(record.getInDate());
            dto.setImageUrl(record.getImageUrl());
            dto.setCreatedAt(record.getCreatedAt());

            // 获取商品信息
            if (record.getProduct() != null) {
                dto.setProductName(record.getProduct().getName());
                dto.setProductSpec(record.getProduct().getSpec());
                dto.setProductUnit(record.getProduct().getUnit());
                dto.setProductPrice(record.getProduct().getPrice());
                dto.setTotalAmount(record.getProduct().getPrice().multiply(BigDecimal.valueOf(record.getQuantity())));
            } else {
                // 如果关联的商品信息未加载，手动查询
                productRepository.findById(record.getProductId()).ifPresent(product -> {
                    dto.setProductName(product.getName());
                    dto.setProductSpec(product.getSpec());
                    dto.setProductUnit(product.getUnit());
                    dto.setProductPrice(product.getPrice());
                    dto.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(record.getQuantity())));
                });
            }

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("转换入库记录DTO失败: " + e.getMessage(), e);
        }
    }
}