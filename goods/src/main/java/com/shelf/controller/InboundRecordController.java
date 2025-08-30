package com.shelf.controller;

import com.shelf.dto.ApiResponse;
import com.shelf.dto.InboundRecordDTO;
import com.shelf.dto.PageResponse;
import com.shelf.service.InboundRecordService;
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
 * 入库记录控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InboundRecordController {

    private final InboundRecordService inboundRecordService;

    /**
     * 分页查询入库记录
     */
    @GetMapping("/inbounds")
    public ApiResponse<PageResponse<InboundRecordDTO>> getInboundRecords(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "inDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        try {
            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            // 如果有商品名称搜索，使用支持商品名称的查询方法
            Page<InboundRecordDTO> records;
            if (productName != null && !productName.trim().isEmpty()) {
                records = inboundRecordService.getInboundRecords(
                        productId, productName, startDate, endDate, pageable);
            } else {
                records = inboundRecordService.getInboundRecords(
                        productId, startDate, endDate, pageable);
            }
            
            return ApiResponse.success(PageResponse.of(records));
        } catch (Exception e) {
            return ApiResponse.error("查询入库记录失败: " + e.getMessage());
        }
    }

    /**
     * 创建入库记录
     */
    @PostMapping("/inbound")
    public ApiResponse<InboundRecordDTO> createInboundRecord(@RequestBody InboundRecordDTO dto) {
        try {
            // 参数验证
            if (dto.getProductId() == null) {
                return ApiResponse.badRequest("商品ID不能为空");
            }
            if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                return ApiResponse.badRequest("入库数量必须大于0");
            }
            if (dto.getInDate() == null) {
                dto.setInDate(LocalDate.now());
            }

            InboundRecordDTO record = inboundRecordService.createInboundRecord(dto);
            return ApiResponse.success("入库记录创建成功", record);
        } catch (Exception e) {
            return ApiResponse.error("创建入库记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取入库记录
     */
    @GetMapping("/inbound/{id}")
    public ApiResponse<InboundRecordDTO> getInboundRecordById(@PathVariable Long id) {
        try {
            InboundRecordDTO record = inboundRecordService.getInboundRecordById(id);
            return ApiResponse.success(record);
        } catch (Exception e) {
            return ApiResponse.error("获取入库记录失败: " + e.getMessage());
        }
    }

    /**
     * 更新入库记录
     */
    @PutMapping("/inbound/{id}")
    public ApiResponse<InboundRecordDTO> updateInboundRecord(
            @PathVariable Long id,
            @RequestBody InboundRecordDTO inboundRecordDTO) {
        try {
            // 设置ID
            inboundRecordDTO.setId(id);
            
            // 参数验证
            if (inboundRecordDTO.getProductId() == null) {
                return ApiResponse.badRequest("商品ID不能为空");
            }
            if (inboundRecordDTO.getQuantity() == null || inboundRecordDTO.getQuantity() <= 0) {
                return ApiResponse.badRequest("入库数量必须大于0");
            }
            if (inboundRecordDTO.getInDate() == null) {
                return ApiResponse.badRequest("入库日期不能为空");
            }
            
            InboundRecordDTO updatedRecord = inboundRecordService.updateInboundRecord(inboundRecordDTO);
            return ApiResponse.success("入库记录更新成功", updatedRecord);
        } catch (Exception e) {
            return ApiResponse.error("更新入库记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除入库记录
     */
    @DeleteMapping("/inbound/{id}")
    public ApiResponse<String> deleteInboundRecord(@PathVariable Long id) {
        try {
            inboundRecordService.deleteInboundRecord(id);
            return ApiResponse.success("入库记录删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除入库记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取入库趋势数据
     */
    @GetMapping("/inbounds/trend")
    public ApiResponse<List<Object[]>> getInboundTrendData(
            @RequestParam(defaultValue = "30") int days) {
        try {
            List<Object[]> trendData = inboundRecordService.getInboundTrendData(days);
            return ApiResponse.success(trendData);
        } catch (Exception e) {
            return ApiResponse.error("获取入库趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 统计入库总量
     */
    @GetMapping("/inbounds/total")
    public ApiResponse<Integer> getTotalInboundQuantity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Integer total = inboundRecordService.getTotalInboundQuantity(startDate, endDate);
            return ApiResponse.success(total);
        } catch (Exception e) {
            return ApiResponse.error("统计入库总量失败: " + e.getMessage());
        }
    }

    /**
     * 导出入库记录
     */
    @GetMapping("/inbounds/export")
    public ApiResponse<String> exportInboundRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // 这里可以实现CSV导出逻辑，暂时返回成功消息
            return ApiResponse.success("入库记录导出功能待实现");
        } catch (Exception e) {
            return ApiResponse.error("导出入库记录失败: " + e.getMessage());
        }
    }
}