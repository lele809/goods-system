package com.shelf.controller;

import com.shelf.dto.ApiResponse;
import com.shelf.dto.PageResponse;
import com.shelf.dto.ProductDTO;
import com.shelf.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询商品列表
     */
    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductDTO>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String spec,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        try {
            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<ProductDTO> products = productService.getProducts(name, spec, pageable);
            return ApiResponse.success(PageResponse.of(products));
        } catch (Exception e) {
            return ApiResponse.error("查询商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有商品（不分页，用于导出）
     */
    @GetMapping("/products/all")
    public ApiResponse<List<ProductDTO>> getAllProducts() {
        try {
            List<ProductDTO> products = productService.getAllProducts();
            return ApiResponse.success(products);
        } catch (Exception e) {
            return ApiResponse.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品
     */
    @GetMapping("/product/{id}")
    public ApiResponse<ProductDTO> getProductById(@PathVariable Long id) {
        try {
            ProductDTO product = productService.getProductById(id);
            return ApiResponse.success(product);
        } catch (Exception e) {
            return ApiResponse.error("获取商品信息失败: " + e.getMessage());
        }
    }

    /**
     * 添加或更新商品
     */
    @PostMapping("/product")
    public ApiResponse<ProductDTO> saveProduct(@RequestBody ProductDTO productDTO) {
        try {
            // 参数验证
            if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
                return ApiResponse.badRequest("商品名称不能为空");
            }
            if (productDTO.getUnit() == null || productDTO.getUnit().trim().isEmpty()) {
                return ApiResponse.badRequest("单位不能为空");
            }
            if (productDTO.getInitialStock() == null || productDTO.getInitialStock() < 0) {
                return ApiResponse.badRequest("初始库存不能为空且不能小于0");
            }
            if (productDTO.getPrice() == null || productDTO.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                return ApiResponse.badRequest("价格不能为空且不能小于0");
            }
            if (productDTO.getAmount() == null || productDTO.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
                return ApiResponse.badRequest("金额不能为空且不能小于0");
            }
            if (productDTO.getRemainingQuantity() == null || productDTO.getRemainingQuantity() < 0) {
                return ApiResponse.badRequest("剩余数量不能为空且不能小于0");
            }

            // 检查名称和规格组合重复
            boolean isDuplicate = productService.isNameAndSpecDuplicate(
                    productDTO.getName(), 
                    productDTO.getSpec(), 
                    productDTO.getId());
            if (isDuplicate) {
                return ApiResponse.badRequest("相同名称和规格的商品已存在");
            }

            ProductDTO savedProduct = productService.saveProduct(productDTO);
            String message = productDTO.getId() == null ? "商品添加成功" : "商品更新成功";
            return ApiResponse.success(message, savedProduct);
        } catch (Exception e) {
            return ApiResponse.error("保存商品失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/product/{id}")
    public ApiResponse<String> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ApiResponse.success("商品删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除商品失败: " + e.getMessage());
        }
    }

    /**
     * 获取库存不足的商品
     */
    @GetMapping("/products/low-stock")
    public ApiResponse<List<ProductDTO>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        try {
            List<ProductDTO> products = productService.getLowStockProducts(threshold);
            return ApiResponse.success(products);
        } catch (Exception e) {
            return ApiResponse.error("查询库存不足商品失败: " + e.getMessage());
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
    @GetMapping("/products/stock/total")
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
    @GetMapping("/products/stock/value")
    public ApiResponse<java.math.BigDecimal> getStockValue() {
        try {
            java.math.BigDecimal value = productService.getStockValue();
            return ApiResponse.success(value);
        } catch (Exception e) {
            return ApiResponse.error("获取库存价值失败: " + e.getMessage());
        }
    }
}