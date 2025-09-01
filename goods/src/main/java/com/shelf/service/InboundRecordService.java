package com.shelf.service;

import com.shelf.dto.InboundRecordDTO;
import com.shelf.entity.InboundRecord;
import com.shelf.entity.Product;
import com.shelf.repository.InboundRecordRepository;
import com.shelf.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 入库记录业务逻辑服务
 */
@Service
@RequiredArgsConstructor
public class InboundRecordService {

    private final InboundRecordRepository inboundRecordRepository;
    private final ProductRepository productRepository;

    /**
     * 分页查询入库记录 - PostgreSQL兼容版本
     */
    @Transactional(readOnly = true)
    public Page<InboundRecordDTO> getInboundRecords(Long productId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            // 转换日期为String，PostgreSQL需要明确的类型
            String startDateStr = startDate != null ? startDate.toString() : null;
            String endDateStr = endDate != null ? endDate.toString() : null;
            
            // 使用PostgreSQL兼容的查询
            Page<InboundRecord> records = inboundRecordRepository.findByMultipleConditions(
                    productId, startDateStr, endDateStr, pageable);
            return records.map(this::convertToDTO);
        } catch (Exception e) {
            System.err.println("PostgreSQL兼容查询失败，尝试原生SQL查询: " + e.getMessage());
            
            try {
                // 降级到原生SQL查询
                return getInboundRecordsWithNativeQuery(productId, startDate, endDate, pageable);
            } catch (Exception e2) {
                System.err.println("原生SQL查询失败，使用简单查询: " + e2.getMessage());
                
                try {
                    // 最后降级到简单查询
                    Page<InboundRecord> records = inboundRecordRepository.findAllOrderByIdDesc(pageable);
                    return records.map(this::convertToDTO);
                } catch (Exception e3) {
                    throw new RuntimeException("查询入库记录失败: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 使用原生SQL查询入库记录（PostgreSQL兼容版本）
     */
    @Transactional(readOnly = true)
    private Page<InboundRecordDTO> getInboundRecordsWithNativeQuery(Long productId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // 将LocalDate转换为String，PostgreSQL需要
        String startDateStr = startDate != null ? startDate.toString() : null;
        String endDateStr = endDate != null ? endDate.toString() : null;
        
        // 计算总数
        Long total = inboundRecordRepository.countByMultipleConditionsNative(productId, startDateStr, endDateStr);
        
        // 查询数据
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        List<InboundRecord> records = inboundRecordRepository.findByMultipleConditionsNative(
                productId, startDateStr, endDateStr, limit, offset);
        
        // 转换为DTO
        List<InboundRecordDTO> dtos = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, total);
    }

    /**
     * 分页查询入库记录（支持商品名称搜索）- PostgreSQL兼容版本
     */
    @Transactional(readOnly = true)
    public Page<InboundRecordDTO> getInboundRecords(Long productId, String productName, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            // 转换日期为String，PostgreSQL需要明确的类型
            String startDateStr = startDate != null ? startDate.toString() : null;
            String endDateStr = endDate != null ? endDate.toString() : null;
            
            // 使用PostgreSQL兼容的商品名称搜索查询
            Page<InboundRecord> records = inboundRecordRepository.findByMultipleConditionsWithProductName(
                    productId, productName, startDateStr, endDateStr, pageable);
            return records.map(this::convertToDTO);
        } catch (Exception e) {
            System.err.println("PostgreSQL商品名称查询失败，尝试备用查询: " + e.getMessage());
            
            try {
                // 降级到原生SQL查询方法
                return getInboundRecordsWithProductNameNative(productId, productName, startDate, endDate, pageable);
            } catch (Exception e2) {
                System.err.println("备用查询失败: " + e2.getMessage());
                throw new RuntimeException("查询入库记录失败: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 使用PostgreSQL兼容的原生SQL查询入库记录（支持商品名称搜索）
     */
    @Transactional(readOnly = true)
    private Page<InboundRecordDTO> getInboundRecordsWithProductNameNative(Long productId, String productName, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // 将LocalDate转换为String，PostgreSQL需要
        String startDateStr = startDate != null ? startDate.toString() : null;
        String endDateStr = endDate != null ? endDate.toString() : null;
        
        // 计算总数
        Long total = inboundRecordRepository.countByMultipleConditionsWithProductNameNative(
                productId, productName, startDateStr, endDateStr);
        
        // 查询数据
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        List<InboundRecord> records = inboundRecordRepository.findByMultipleConditionsWithProductNameNative(
                productId, productName, startDateStr, endDateStr, limit, offset);
        
        // 转换为DTO
        List<InboundRecordDTO> dtos = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, total);
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
    @Transactional
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
    @Transactional
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
