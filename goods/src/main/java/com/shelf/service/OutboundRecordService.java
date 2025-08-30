package com.shelf.service;

import com.shelf.dto.OutboundRecordDTO;
import com.shelf.entity.OutboundRecord;
import com.shelf.entity.Product;
import com.shelf.repository.OutboundRecordRepository;
import com.shelf.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 出库记录业务逻辑服务
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OutboundRecordService {

    private final OutboundRecordRepository outboundRecordRepository;
    private final ProductRepository productRepository;

    /**
     * 分页查询出库记录
     */
    @Transactional(readOnly = true)
    public Page<OutboundRecordDTO> getOutboundRecords(Long productId, String name, Integer paymentStatus,
                                                     LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // 统一使用多条件查询方法
        Page<OutboundRecord> records = outboundRecordRepository.findByMultipleConditions(
                productId, 
                name != null && !name.trim().isEmpty() ? name.trim() : null,
                paymentStatus,
                startDate, 
                endDate, 
                pageable);
        
        return records.map(this::convertToDTO);
    }

    /**
     * 分页查询出库记录（支持商品名称搜索）
     */
    @Transactional(readOnly = true)
    public Page<OutboundRecordDTO> getOutboundRecords(Long productId, String productName, String name, Integer paymentStatus,
                                                     LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // 使用支持商品名称搜索的多条件查询方法
        Page<OutboundRecord> records = outboundRecordRepository.findByMultipleConditionsWithProductName(
                productId,
                productName != null && !productName.trim().isEmpty() ? productName.trim() : null,
                name != null && !name.trim().isEmpty() ? name.trim() : null,
                paymentStatus,
                startDate, 
                endDate, 
                pageable);
        
        return records.map(this::convertToDTO);
    }

    /**
     * 创建出库记录
     */
    public OutboundRecordDTO createOutboundRecord(OutboundRecordDTO dto) {
        // 验证商品是否存在
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + dto.getProductId()));
        
        // 验证库存是否充足（使用剩余数量字段）
        if (product.getRemainingQuantity() < dto.getQuantity()) {
            throw new RuntimeException("库存不足，当前剩余数量: " + product.getRemainingQuantity() + "，出库数量: " + dto.getQuantity());
        }
        
        // 验证姓名不能为空
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        
        // 创建出库记录
        OutboundRecord record = new OutboundRecord();
        record.setProductId(dto.getProductId());
        record.setOutDate(dto.getOutDate() != null ? dto.getOutDate() : LocalDate.now());
        record.setQuantity(dto.getQuantity());
        record.setName(dto.getName().trim());
        record.setPaymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : 0);
        record.setImageUrl(product.getImageUrl()); // 设置商品图片
        
        record = outboundRecordRepository.save(record);
        
        // 更新商品剩余数量
        product.setRemainingQuantity(product.getRemainingQuantity() - dto.getQuantity());
        productRepository.save(product);
        
        return convertToDTO(record);
    }

    /**
     * 根据ID获取出库记录
     */
    @Transactional(readOnly = true)
    public OutboundRecordDTO getOutboundRecordById(Long id) {
        OutboundRecord record = outboundRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("出库记录不存在，ID: " + id));
        return convertToDTO(record);
    }

    /**
     * 更新出库记录
     */
    public OutboundRecordDTO updateOutboundRecord(OutboundRecordDTO outboundRecordDTO) {
        // 获取原出库记录
        OutboundRecord oldRecord = outboundRecordRepository.findById(outboundRecordDTO.getId())
                .orElseThrow(() -> new RuntimeException("出库记录不存在，ID: " + outboundRecordDTO.getId()));
        
        // 获取对应的商品
        Product product = productRepository.findById(oldRecord.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + oldRecord.getProductId()));
        
        // 先恢复原来的库存数量
        product.setRemainingQuantity(product.getRemainingQuantity() + oldRecord.getQuantity());
        
        // 检查是否更换了商品
        if (!oldRecord.getProductId().equals(outboundRecordDTO.getProductId())) {
            // 保存原商品的库存恢复
            productRepository.save(product);
            
            // 获取新商品
            product = productRepository.findById(outboundRecordDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + outboundRecordDTO.getProductId()));
        }
        
        // 检查库存是否足够
        if (product.getRemainingQuantity() < outboundRecordDTO.getQuantity()) {
            throw new RuntimeException("库存不足，当前剩余数量: " + product.getRemainingQuantity());
        }
        
        // 验证姓名不能为空
        if (outboundRecordDTO.getName() == null || outboundRecordDTO.getName().trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        
        // 更新商品剩余数量
        product.setRemainingQuantity(product.getRemainingQuantity() - outboundRecordDTO.getQuantity());
        productRepository.save(product);
        
        // 更新出库记录
        oldRecord.setProductId(outboundRecordDTO.getProductId());
        oldRecord.setQuantity(outboundRecordDTO.getQuantity());
        oldRecord.setOutDate(outboundRecordDTO.getOutDate());
        oldRecord.setName(outboundRecordDTO.getName().trim());
        oldRecord.setPaymentStatus(outboundRecordDTO.getPaymentStatus() != null ? outboundRecordDTO.getPaymentStatus() : 0);
        oldRecord.setImageUrl(product.getImageUrl()); // 更新商品图片
        
        OutboundRecord savedRecord = outboundRecordRepository.save(oldRecord);
        return convertToDTO(savedRecord);
    }

    /**
     * 删除出库记录
     */
    public void deleteOutboundRecord(Long id) {
        // 获取出库记录
        OutboundRecord record = outboundRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("出库记录不存在，ID: " + id));
        
        // 获取对应的商品
        Product product = productRepository.findById(record.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + record.getProductId()));
        
        // 恢复商品剩余数量
        product.setRemainingQuantity(product.getRemainingQuantity() + record.getQuantity());
        productRepository.save(product);
        
        // 删除出库记录
        outboundRecordRepository.deleteById(id);
    }

    /**
     * 获取出库趋势数据
     */
    @Transactional(readOnly = true)
    public List<Object[]> getOutboundTrendData(Integer days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return outboundRecordRepository.findOutboundTrendData(startDate);
    }

    /**
     * 统计指定日期范围内的出库总量
     */
    @Transactional(readOnly = true)
    public Integer getTotalOutboundQuantity(LocalDate startDate, LocalDate endDate) {
        return outboundRecordRepository.sumQuantityByDateRange(startDate, endDate);
    }

    /**
     * 获取指定日期范围内的所有出库记录（用于导出）
     */
    @Transactional(readOnly = true)
    public List<OutboundRecordDTO> getOutboundRecordsForExport(LocalDate startDate, LocalDate endDate) {
        List<OutboundRecord> records = outboundRecordRepository.findByOutDateBetween(startDate, endDate);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换Entity到DTO
     */
    private OutboundRecordDTO convertToDTO(OutboundRecord record) {
        OutboundRecordDTO dto = new OutboundRecordDTO();
        dto.setId(record.getId());
        dto.setProductId(record.getProductId());
        dto.setOutDate(record.getOutDate());
        dto.setQuantity(record.getQuantity());
        dto.setName(record.getName());
        dto.setPaymentStatus(record.getPaymentStatus());
        dto.setCreatedAt(record.getCreatedAt());
        
        // 获取商品信息
        Product product = productRepository.findById(record.getProductId()).orElse(null);
        if (product != null) {
            dto.setProductName(product.getName());
            dto.setProductSpec(product.getSpec());
            dto.setProductUnit(product.getUnit());
            dto.setProductPrice(product.getPrice());
            dto.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(record.getQuantity())));
        }
        
        // 使用记录中的图片URL，如果没有则使用商品的图片URL
        dto.setImageUrl(record.getImageUrl() != null ? record.getImageUrl() : 
                       (product != null ? product.getImageUrl() : null));
        
        return dto;
    }
}