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
           "AND i.inDate BETWEEN :startDate AND :endDate ORDER BY i.inDate DESC")
    Page<InboundRecord> findByProductIdAndDateRange(@Param("productId") Long productId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   Pageable pageable);

    /**
     * 根据多个条件查询入库记录（支持可选参数）
     */
    @Query("SELECT i FROM InboundRecord i WHERE " +
           "(:productId IS NULL OR i.productId = :productId) AND " +
           "(:startDate IS NULL OR i.inDate >= CAST(:startDate AS date)) AND " +
           "(:endDate IS NULL OR i.inDate <= CAST(:endDate AS date))")
    Page<InboundRecord> findByMultipleConditions(@Param("productId") Long productId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                Pageable pageable);

    /**
     * 根据多个条件查询入库记录（包括商品名称搜索）
     */
    @Query("SELECT i FROM InboundRecord i LEFT JOIN Product p ON i.productId = p.id WHERE " +
           "(:productId IS NULL OR i.productId = :productId) AND " +
           "(:productName IS NULL OR :productName = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
           "(:startDate IS NULL OR i.inDate >= CAST(:startDate AS date)) AND " +
           "(:endDate IS NULL OR i.inDate <= CAST(:endDate AS date))")
    Page<InboundRecord> findByMultipleConditionsWithProductName(@Param("productId") Long productId,
                                                              @Param("productName") String productName,
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
}
