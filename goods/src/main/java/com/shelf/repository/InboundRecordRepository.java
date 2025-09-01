package com.shelf.repository;

import com.shelf.entity.InboundRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 入库记录数据访问接口
 */
@Repository
public interface InboundRecordRepository extends JpaRepository<InboundRecord, Long> {

    /**
     * 根据日期范围查询入库记录（分页）
     */
    Page<InboundRecord> findByInDateBetweenOrderByInDateDesc(
            LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 根据商品ID查询入库记录
     */
    Page<InboundRecord> findByProductIdOrderByInDateDesc(Long productId, Pageable pageable);

    /**
     * 根据商品ID和日期范围查询入库记录
     */
    @Query("SELECT i FROM InboundRecord i WHERE i.productId = :productId " +
           "AND i.inDate BETWEEN :startDate AND :endDate")
    Page<InboundRecord> findByProductIdAndDateRange(@Param("productId") Long productId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   Pageable pageable);

    /**
     * 根据多个条件查询入库记录（PostgreSQL兼容 - 使用原生SQL避免类型推断问题）
     */
    @Query(value = "SELECT * FROM inbound_record i WHERE " +
           "(:productId IS NULL OR i.product_id = CAST(:productId AS BIGINT)) AND " +
           "(:startDate IS NULL OR i.in_date >= CAST(:startDate AS DATE)) AND " +
           "(:endDate IS NULL OR i.in_date <= CAST(:endDate AS DATE))", 
           nativeQuery = true)
    Page<InboundRecord> findByMultipleConditions(@Param("productId") Long productId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                Pageable pageable);

    /**
     * 根据多个条件查询入库记录（PostgreSQL兼容的原生SQL版本）
     */
    @Query(value = "SELECT * FROM inbound_record i WHERE " +
           "(:productId IS NULL OR i.product_id = :productId) AND " +
           "(:startDate IS NULL OR i.in_date >= CAST(:startDate AS DATE)) AND " +
           "(:endDate IS NULL OR i.in_date <= CAST(:endDate AS DATE)) " +
           "ORDER BY i.id DESC " +
           "LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<InboundRecord> findByMultipleConditionsNative(@Param("productId") Long productId,
                                                      @Param("startDate") String startDate,
                                                      @Param("endDate") String endDate,
                                                      @Param("limit") int limit,
                                                      @Param("offset") int offset);

    /**
     * 计算原生查询的总数（PostgreSQL兼容）
     */
    @Query(value = "SELECT COUNT(*) FROM inbound_record i WHERE " +
           "(:productId IS NULL OR i.product_id = :productId) AND " +
           "(:startDate IS NULL OR i.in_date >= CAST(:startDate AS DATE)) AND " +
           "(:endDate IS NULL OR i.in_date <= CAST(:endDate AS DATE))", 
           nativeQuery = true)
    Long countByMultipleConditionsNative(@Param("productId") Long productId,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    /**
     * 根据多个条件查询入库记录（包括商品名称搜索）- PostgreSQL兼容的原生SQL版本，优化JOIN性能
     */
    @Query(value = "SELECT i.*, p.name as product_name, p.spec as product_spec, " +
           "p.unit as product_unit, p.price as product_price, p.image_url as product_image " +
           "FROM inbound_record i " +
           "LEFT JOIN product p ON i.product_id = p.id WHERE " +
           "(:productId IS NULL OR i.product_id = CAST(:productId AS BIGINT)) AND " +
           "(:productName IS NULL OR :productName = '' OR LOWER(p.name) LIKE LOWER('%' || :productName || '%')) AND " +
           "(:startDate IS NULL OR i.in_date >= CAST(:startDate AS DATE)) AND " +
           "(:endDate IS NULL OR i.in_date <= CAST(:endDate AS DATE)) " +
           "ORDER BY i.created_at DESC", 
           nativeQuery = true)
    Page<InboundRecord> findByMultipleConditionsWithProductName(@Param("productId") Long productId,
                                                              @Param("productName") String productName,
                                                              @Param("startDate") String startDate,
                                                              @Param("endDate") String endDate,
                                                              Pageable pageable);

    /**
     * PostgreSQL兼容的商品名称搜索（原生SQL版本）
     */
    @Query(value = "SELECT i.* FROM inbound_record i " +
           "LEFT JOIN product p ON i.product_id = p.id WHERE " +
           "(:productId IS NULL OR i.product_id = :productId) AND " +
           "(:productName IS NULL OR :productName = '' OR LOWER(p.name) LIKE LOWER('%' || :productName || '%')) AND " +
           "(:startDate IS NULL OR i.in_date >= CAST(:startDate AS DATE)) AND " +
           "(:endDate IS NULL OR i.in_date <= CAST(:endDate AS DATE)) " +
           "ORDER BY i.id DESC " +
           "LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<InboundRecord> findByMultipleConditionsWithProductNameNative(@Param("productId") Long productId,
                                                                     @Param("productName") String productName,
                                                                     @Param("startDate") String startDate,
                                                                     @Param("endDate") String endDate,
                                                                     @Param("limit") int limit,
                                                                     @Param("offset") int offset);

    /**
     * PostgreSQL兼容的商品名称搜索计数（原生SQL版本）
     */
    @Query(value = "SELECT COUNT(*) FROM inbound_record i " +
           "LEFT JOIN product p ON i.product_id = p.id WHERE " +
           "(:productId IS NULL OR i.product_id = :productId) AND " +
           "(:productName IS NULL OR :productName = '' OR LOWER(p.name) LIKE LOWER('%' || :productName || '%')) AND " +
           "(:startDate IS NULL OR i.in_date >= CAST(:startDate AS DATE)) AND " +
           "(:endDate IS NULL OR i.in_date <= CAST(:endDate AS DATE))", 
           nativeQuery = true)
    Long countByMultipleConditionsWithProductNameNative(@Param("productId") Long productId,
                                                       @Param("productName") String productName,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate);

    /**
     * 根据商品名称查询商品ID列表
     */
    @Query("SELECT p.id FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<Long> findProductIdsByNameContaining(@Param("productName") String productName);

    /**
     * 根据商品ID列表和条件查询入库记录
     */
    @Query("SELECT i FROM InboundRecord i WHERE " +
           "i.productId IN :productIds AND " +
           "(:startDate IS NULL OR i.inDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.inDate <= :endDate) ORDER BY i.inDate DESC")
    Page<InboundRecord> findByProductIdsAndDateRange(@Param("productIds") List<Long> productIds,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    Pageable pageable);

    /**
     * 根据商品ID统计入库总量
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InboundRecord i WHERE i.productId = :productId")
    Integer getTotalInboundQuantityByProductId(@Param("productId") Long productId);

    /**
     * 根据商品ID和日期范围统计入库总量
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InboundRecord i WHERE i.productId = :productId " +
           "AND i.inDate BETWEEN :startDate AND :endDate")
    Integer getTotalInboundQuantityByProductIdAndDateRange(@Param("productId") Long productId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * 根据日期范围统计入库总量
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InboundRecord i WHERE i.inDate BETWEEN :startDate AND :endDate")
    Integer getTotalInboundQuantityByDateRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 获取入库趋势数据（最近N天）
     */
    @Query("SELECT i.inDate, SUM(i.quantity) FROM InboundRecord i " +
           "WHERE i.inDate >= :startDate GROUP BY i.inDate ORDER BY i.inDate")
    List<Object[]> getInboundTrendData(@Param("startDate") LocalDate startDate);

    /**
     * 统计指定商品截止到指定日期的累计入库数量
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InboundRecord i " +
           "WHERE i.productId = :productId AND i.inDate <= :date")
    Integer sumQuantityByProductIdBeforeDate(@Param("productId") Long productId, 
                                           @Param("date") LocalDate date);

    /**
     * 查询所有商品截止到指定日期的累计入库数量
     */
    @Query("SELECT i.productId, COALESCE(SUM(i.quantity), 0) FROM InboundRecord i " +
           "WHERE i.inDate <= :date GROUP BY i.productId")
    List<Object[]> sumQuantityGroupByProductBeforeDate(@Param("date") LocalDate date);

    /**
     * 统计指定商品的总入库数量（不限日期）
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InboundRecord i " +
           "WHERE i.productId = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    /**
     * 批量查询多个商品的入库数量
     */
    @Query("SELECT i.productId, COALESCE(SUM(i.quantity), 0) FROM InboundRecord i " +
           "WHERE i.productId IN :productIds GROUP BY i.productId")
    List<Object[]> sumQuantityByProductIds(@Param("productIds") List<Long> productIds);

    /**
     * 简单查询所有入库记录（无条件，用于调试）- 移除ORDER BY避免冲突
     */
    @Query("SELECT i FROM InboundRecord i")
    Page<InboundRecord> findAllOrderByIdDesc(Pageable pageable);

    /**
     * 根据日期范围查询（简化版）- 移除ORDER BY避免冲突
     */
    @Query("SELECT i FROM InboundRecord i WHERE i.inDate BETWEEN :startDate AND :endDate")
    Page<InboundRecord> findByDateRangeSimple(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             Pageable pageable);
}
