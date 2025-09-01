package com.shelf.controller;

import com.shelf.dto.ApiResponse;
import com.shelf.dto.OutboundRecordDTO;
import com.shelf.dto.PageResponse;
import com.shelf.service.OutboundRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 出库记录控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OutboundRecordController {

    private final OutboundRecordService outboundRecordService;

    /**
     * 将Java字段名映射为数据库列名
     */
    private String mapFieldToColumn(String fieldName) {
        switch (fieldName) {
            case "createdAt":
                return "created_at";
            case "outDate":
                return "out_date";
            case "productId":
                return "product_id";
            case "paymentStatus":
                return "payment_status";
            default:
                return fieldName;
        }
    }

    /**
     * 分页查询出库记录
     */
    @GetMapping("/outbounds")
    public ApiResponse<PageResponse<OutboundRecordDTO>> getOutboundRecords(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "outDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        try {
            // 字段名映射：将Java字段名转换为数据库列名
            String dbColumnName = mapFieldToColumn(sort);
            
            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, dbColumnName));
            
            // 如果有商品名称搜索，使用支持商品名称的查询方法
            Page<OutboundRecordDTO> records;
            if (productName != null && !productName.trim().isEmpty()) {
                records = outboundRecordService.getOutboundRecords(
                        productId, productName, name, paymentStatus, startDate, endDate, pageable);
            } else {
                records = outboundRecordService.getOutboundRecords(
                        productId, name, paymentStatus, startDate, endDate, pageable);
            }
            
            return ApiResponse.success(PageResponse.of(records));
        } catch (Exception e) {
            return ApiResponse.error("查询出库记录失败: " + e.getMessage());
        }
    }

    /**
     * 创建出库记录
     */
    @PostMapping("/outbound")
    public ApiResponse<OutboundRecordDTO> createOutboundRecord(@RequestBody OutboundRecordDTO dto) {
        try {
            // 参数验证
            if (dto.getProductId() == null) {
                return ApiResponse.badRequest("商品ID不能为空");
            }
            if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                return ApiResponse.badRequest("出库数量必须大于0");
            }
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                return ApiResponse.badRequest("姓名不能为空");
            }
            if (dto.getPaymentStatus() == null || (dto.getPaymentStatus() != 0 && dto.getPaymentStatus() != 1)) {
                return ApiResponse.badRequest("付款状态必须为0(未付款)或1(已付款)");
            }
            if (dto.getOutDate() == null) {
                dto.setOutDate(LocalDate.now());
            }

            OutboundRecordDTO record = outboundRecordService.createOutboundRecord(dto);
            return ApiResponse.success("出库记录创建成功", record);
        } catch (Exception e) {
            return ApiResponse.error("创建出库记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取出库记录
     */
    @GetMapping("/outbound/{id}")
    public ApiResponse<OutboundRecordDTO> getOutboundRecordById(@PathVariable Long id) {
        try {
            OutboundRecordDTO record = outboundRecordService.getOutboundRecordById(id);
            return ApiResponse.success(record);
        } catch (Exception e) {
            return ApiResponse.error("获取出库记录失败: " + e.getMessage());
        }
    }

    /**
     * 更新出库记录
     */
    @PutMapping("/outbound/{id}")
    public ApiResponse<OutboundRecordDTO> updateOutboundRecord(
            @PathVariable Long id,
            @RequestBody OutboundRecordDTO outboundRecordDTO) {
        try {
            // 设置ID
            outboundRecordDTO.setId(id);
            
            // 参数验证
            if (outboundRecordDTO.getProductId() == null) {
                return ApiResponse.badRequest("商品ID不能为空");
            }
            if (outboundRecordDTO.getQuantity() == null || outboundRecordDTO.getQuantity() <= 0) {
                return ApiResponse.badRequest("出库数量必须大于0");
            }
            if (outboundRecordDTO.getName() == null || outboundRecordDTO.getName().trim().isEmpty()) {
                return ApiResponse.badRequest("姓名不能为空");
            }
            if (outboundRecordDTO.getPaymentStatus() == null || (outboundRecordDTO.getPaymentStatus() != 0 && outboundRecordDTO.getPaymentStatus() != 1)) {
                return ApiResponse.badRequest("付款状态必须为0(未付款)或1(已付款)");
            }
            if (outboundRecordDTO.getOutDate() == null) {
                return ApiResponse.badRequest("出库日期不能为空");
            }
            
            OutboundRecordDTO updatedRecord = outboundRecordService.updateOutboundRecord(outboundRecordDTO);
            return ApiResponse.success("出库记录更新成功", updatedRecord);
        } catch (Exception e) {
            return ApiResponse.error("更新出库记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除出库记录
     */
    @DeleteMapping("/outbound/{id}")
    public ApiResponse<String> deleteOutboundRecord(@PathVariable Long id) {
        try {
            outboundRecordService.deleteOutboundRecord(id);
            return ApiResponse.success("出库记录删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除出库记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取出库趋势数据
     */
    @GetMapping("/outbounds/trend")
    public ApiResponse<List<Object[]>> getOutboundTrendData(
            @RequestParam(defaultValue = "30") Integer days) {
        try {
            List<Object[]> trendData = outboundRecordService.getOutboundTrendData(days);
            return ApiResponse.success(trendData);
        } catch (Exception e) {
            return ApiResponse.error("获取出库趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 统计指定日期范围内的出库总量
     */
    @GetMapping("/outbounds/total")
    public ApiResponse<Integer> getTotalOutboundQuantity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Integer total = outboundRecordService.getTotalOutboundQuantity(startDate, endDate);
            return ApiResponse.success(total);
        } catch (Exception e) {
            return ApiResponse.error("统计出库总量失败: " + e.getMessage());
        }
    }

    /**
     * 导出出库记录
     */
    @GetMapping("/outbounds/export")
    public ApiResponse<List<OutboundRecordDTO>> exportOutboundRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<OutboundRecordDTO> records = outboundRecordService.getOutboundRecordsForExport(startDate, endDate);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error("导出出库记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日出库统计
     */
    @GetMapping("/outbound/today")
    public ApiResponse<Integer> getTodayOutbound() {
        try {
            LocalDate today = LocalDate.now();
            Integer count = outboundRecordService.getTotalOutboundQuantity(today, today);
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error("获取今日出库统计失败: " + e.getMessage());
        }
    }
} 