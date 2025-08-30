package com.shelf.service;

import com.shelf.dto.ProductDTO;
import com.shelf.dto.StockExportDTO;
import com.shelf.entity.Product;
import com.shelf.repository.InboundRecordRepository;
import com.shelf.repository.OutboundRecordRepository;
import com.shelf.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品业务逻辑服务
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final InboundRecordRepository inboundRecordRepository;
    private final OutboundRecordRepository outboundRecordRepository;

    /**
     * 分页查询商品列表
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProducts(String name, String spec, Pageable pageable) {
        Page<Product> products = productRepository.findByNameAndSpecContaining(name, spec, pageable);
        return products.map(this::convertToDTO);
    }

    /**
     * 获取所有商品（用于导出）
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据ID获取商品
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + id));
        return convertToDTO(product);
    }

    /**
     * 保存商品（新增或更新）
     */
    public ProductDTO saveProduct(ProductDTO productDTO) {
        Product product;
        
        if (productDTO.getId() != null) {
            // 更新
            product = productRepository.findById(productDTO.getId())
                    .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + productDTO.getId()));
            updateProductFromDTO(product, productDTO);
        } else {
            // 新增
            product = new Product();
            updateProductFromDTO(product, productDTO);
        }
        
        product = productRepository.save(product);
        return convertToDTO(product);
    }

    /**
     * 删除商品
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("商品不存在，ID: " + id);
        }
        
        // 检查是否有出库记录
        Integer outboundCount = outboundRecordRepository.sumQuantityByProductId(id);
        if (outboundCount > 0) {
            throw new RuntimeException("该商品存在出库记录，无法删除");
        }
        
        productRepository.deleteById(id);
    }

    /**
     * 查询库存不足的商品
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 检查商品名称和规格组合是否重复
     */
    @Transactional(readOnly = true)
    public boolean isNameAndSpecDuplicate(String name, String spec, Long excludeId) {
        List<Product> products = productRepository.findByNameAndSpec(name, spec);
        return products.stream().anyMatch(p -> !p.getId().equals(excludeId));
    }

    /**
     * 获取指定日期的历史库存
     */
    @Transactional(readOnly = true)
    public List<StockExportDTO> getHistoryStock(LocalDate date) {
        // 获取所有商品
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();
        
        // 获取截止到指定日期的累计入库数量（按商品分组）
        List<Object[]> inboundData = inboundRecordRepository.sumQuantityGroupByProductBeforeDate(date);
        Map<Long, Integer> inboundMap = new HashMap<>();
        for (Object[] data : inboundData) {
            Long productId = (Long) data[0];
            Integer quantity = ((Number) data[1]).intValue();
            inboundMap.put(productId, quantity);
        }
        
        // 获取截止到指定日期的累计出库数量（按商品分组）
        List<Object[]> outboundData = outboundRecordRepository.sumQuantityGroupByProductBeforeDate(date);
        Map<Long, Integer> outboundMap = new HashMap<>();
        for (Object[] data : outboundData) {
            Long productId = (Long) data[0];
            Integer quantity = ((Number) data[1]).intValue();
            outboundMap.put(productId, quantity);
        }
        
        // 计算每个商品在指定日期的库存：初始库存 + 累计入库 - 累计出库
        return products.stream()
                .map(product -> {
                    Integer totalInbound = inboundMap.getOrDefault(product.getId(), 0);
                    Integer totalOutbound = outboundMap.getOrDefault(product.getId(), 0);
                    
                    // 创建StockExportDTO，构造函数中会自动计算正确的库存
                    return new StockExportDTO(
                            product.getId(),
                            product.getName(),
                            product.getSpec(),
                            product.getUnit(),
                            product.getImageUrl(),
                            product.getInitialStock(),
                            product.getPrice(),
                            totalInbound,
                            totalOutbound,
                            date
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取指定商品在指定日期的历史库存
     */
    @Transactional(readOnly = true)
    public StockExportDTO getProductHistoryStock(Long productId, LocalDate date) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + productId));
        
        // 获取累计入库和出库数量
        Integer totalInbound = inboundRecordRepository.sumQuantityByProductIdBeforeDate(productId, date);
        Integer totalOutbound = outboundRecordRepository.sumQuantityByProductIdBeforeDate(productId, date);
        
        // 创建DTO，构造函数中会自动计算正确的库存
        return new StockExportDTO(
                product.getId(),
                product.getName(),
                product.getSpec(),
                product.getUnit(),
                product.getImageUrl(),
                product.getInitialStock(),
                product.getPrice(),
                totalInbound,
                totalOutbound,
                date
        );
    }

    /**
     * 获取库存查询的有效日期范围
     */
    @Transactional(readOnly = true)
    public Map<String, String> getStockDateRange() {
        Map<String, String> result = new HashMap<>();
        
        // 获取最早的商品创建日期
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();
        LocalDate minDate = LocalDate.now();
        if (!products.isEmpty()) {
            minDate = products.stream()
                    .map(p -> p.getCreatedAt().toLocalDate())
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
        }
        
        // 最大日期为今天
        LocalDate maxDate = LocalDate.now();
        
        // 添加调试日志
        System.out.println("库存查询日期范围 - 最小日期: " + minDate + ", 最大日期: " + maxDate);
        
        result.put("minDate", minDate.toString());
        result.put("maxDate", maxDate.toString());
        
        return result;
    }

    /**
     * 获取商品总数
     */
    @Transactional(readOnly = true)
    public Long getProductCount() {
        return productRepository.count();
    }

    /**
     * 获取库存总量
     */
    @Transactional(readOnly = true)
    public Integer getTotalStock() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .mapToInt(Product::getRemainingQuantity)
                .sum();
    }

    /**
     * 获取库存价值
     */
    @Transactional(readOnly = true)
    public BigDecimal getStockValue() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getRemainingQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 转换Entity到DTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSpec(product.getSpec());
        dto.setUnit(product.getUnit());
        dto.setInitialStock(product.getInitialStock());
        dto.setPrice(product.getPrice());
        dto.setAmount(product.getAmount());
        dto.setImageUrl(product.getImageUrl()); // 添加图片URL字段
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // 计算总入库量、总出库量和当前库存
        Integer totalInbound = inboundRecordRepository.sumQuantityByProductId(product.getId());
        Integer totalOutbound = outboundRecordRepository.sumQuantityByProductId(product.getId());
        
        // 使用和库存管理页面相同的计算公式：初始库存 + 累计入库 - 累计出库
        Integer realTimeStock = product.getInitialStock() + 
                (totalInbound != null ? totalInbound : 0) - 
                (totalOutbound != null ? totalOutbound : 0);
        
        dto.setTotalOutbound(totalOutbound);
        dto.setCurrentStock(realTimeStock); // 使用实时计算的库存
        dto.setRemainingQuantity(realTimeStock); // 统一使用实时计算的库存，确保与currentStock一致
        dto.setTotalValue(realTimeStock > 0 ? 
                product.getPrice().multiply(BigDecimal.valueOf(realTimeStock)) : 
                BigDecimal.ZERO);
        
        return dto;
    }

    /**
     * 从DTO更新Entity
     */
    private void updateProductFromDTO(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setSpec(dto.getSpec());
        product.setUnit(dto.getUnit());
        product.setInitialStock(dto.getInitialStock());
        product.setPrice(dto.getPrice());
        product.setAmount(dto.getAmount());
        product.setRemainingQuantity(dto.getRemainingQuantity());
        product.setImageUrl(dto.getImageUrl()); // 添加图片URL字段
    }
}