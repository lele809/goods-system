package com.shelf.controller;

import com.shelf.dto.ApiResponse;
import com.shelf.dto.StockExportDTO;
import com.shelf.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 库存控制器
 */
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockController {

    private final ProductService productService;

    /**
     * 获取可查询的日期范围
     */
    @GetMapping("/date-range")
    public ApiResponse<Map<String, String>> getDateRange() {
        try {
            Map<String, String> dateRange = productService.getStockDateRange();
            return ApiResponse.success(dateRange);
        } catch (Exception e) {
            return ApiResponse.error("获取日期范围失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定日期的历史库存
     */
    @GetMapping("/history")
    public ApiResponse<List<StockExportDTO>> getHistoryStock(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<StockExportDTO> stockList = productService.getHistoryStock(date);
            return ApiResponse.success(stockList);
        } catch (Exception e) {
            return ApiResponse.error("查询历史库存失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定商品在指定日期的历史库存
     */
    @GetMapping("/history/{productId}")
    public ApiResponse<StockExportDTO> getProductHistoryStock(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            StockExportDTO stock = productService.getProductHistoryStock(productId, date);
            return ApiResponse.success(stock);
        } catch (Exception e) {
            return ApiResponse.error("查询商品历史库存失败: " + e.getMessage());
        }
    }

    /**
     * 导出指定日期的库存Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStock(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<StockExportDTO> stockList = productService.getHistoryStock(date);
            byte[] csvData = generateExcel(stockList, date);
            
            String filename = "库存导出_" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误信息
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 生成Excel文件
     */
    private byte[] generateExcel(List<StockExportDTO> stockList, LocalDate date) throws IOException {
        // 这里使用简单的CSV格式，您可以换成Apache POI来生成真正的Excel
        StringBuilder csvContent = new StringBuilder();
        
        // 添加BOM以支持中文
        csvContent.append("\uFEFF");
        
        // 表头
        csvContent.append("商品名称,规格,单位,初始库存,累计出库,库存数量,单价,库存总价值,统计日期\n");
        
        // 数据行
        for (StockExportDTO stock : stockList) {
            csvContent.append(String.format("%s,%s,%s,%d,%d,%d,%.2f,%.2f,%s\n",
                    stock.getProductName() != null ? stock.getProductName() : "",
                    stock.getSpec() != null ? stock.getSpec() : "",
                    stock.getUnit() != null ? stock.getUnit() : "",
                    stock.getInitialStock(),
                    stock.getTotalOutbound(),
                    stock.getStockQuantity(),
                    stock.getUnitPrice(),
                    stock.getTotalValue(),
                    stock.getStockDate()
            ));
        }
        
        return csvContent.toString().getBytes("UTF-8");
    }
} 