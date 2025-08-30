package com.shelf.repository;

import com.shelf.entity.OutboundRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 出库记录数据访问接口
 */
@Repository
public interface OutboundRecordRepository extends JpaRepository<OutboundRecord, Long> {

    /**
     * 根据日期范围查询出库记录（分页）
     */
    Page<OutboundRecord> findByOutDateBetweenOrderByOutDateDesc(
            LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 根据商品ID查询出库记录
     */
    Page<OutboundRecord> findByProductIdOrderByOutDateDesc(Long productId, Pageable pageable);

    /**
     * 根据商品ID和日期范围查询出库记录
     */
    @Query("SELECT o FROM OutboundRecord o WHERE o.productId = :productId " +
           "AND o.outDate BETWEEN :startDate AND :endDate ORDER BY o.outDate DESC")
    Page<OutboundRecord> findByProductIdAndDateRange(@Param("productId") Long productId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    Pageable pageable);

    /**
     * 根据多个条件查询出库记录（支持可选参数）
     */
    @Query("SELECT o FROM OutboundRecord o WHERE " +
           "(:productId IS NULL OR o.productId = :productId) AND " +
           "(:name IS NULL OR :name = '' OR LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
           "(:startDate IS NULL OR o.outDate >= CAST(:startDate AS date)) AND " +
           "(:endDate IS NULL OR o.outDate <= CAST(:endDate AS date)) " +
           "ORDER BY o.outDate DESC")
    Page<OutboundRecord> findByMultipleConditions(@Param("productId") Long productId,
                                                 @Param("name") String name,
                                                 @Param("paymentStatus") Integer paymentStatus,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 Pageable pageable);

    /**
     * 根据多个条件查询出库记录（包括商品名称搜索）
     */
    @Query("SELECT o FROM OutboundRecord o LEFT JOIN Product p ON o.productId = p.id WHERE " +
           "(:productId IS NULL OR o.productId = :productId) AND " +
           "(:productName IS NULL OR :productName = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
           "(:name IS NULL OR :name = '' OR LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
           "(:startDate IS NULL OR o.outDate >= CAST(:startDate AS date)) AND " +
           "(:endDate IS NULL OR o.outDate <= CAST(:endDate AS date)) " +
           "ORDER BY o.outDate DESC")
    Page<OutboundRecord> findByMultipleConditionsWithProductName(@Param("productId") Long productId,
                                                                @Param("productName") String productName,
                                                                @Param("name") String name,
                                                                @Param("paymentStatus") Integer paymentStatus,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate,
                                                                Pageable pageable);

    /**
     * 查询指定日期范围内的所有出库记录（用于统计）
     */
    List<OutboundRecord> findByOutDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 统计指定商品的总出库数量
     */
    @Query("SELECT COALESCE(SUM(o.quantity), 0) FROM OutboundRecord o WHERE o.productId = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    /**
     * 统计指定日期范围内的总出库数量
     */
    @Query("SELECT COALESCE(SUM(o.quantity), 0) FROM OutboundRecord o " +
           "WHERE o.outDate BETWEEN :startDate AND :endDate")
    Integer sumQuantityByDateRange(@Param("startDate") LocalDate startDate, 
                                 @Param("endDate") LocalDate endDate);

    /**
     * 查询最近N天的出库趋势数据
     */
    @Query("SELECT o.outDate as date, SUM(o.quantity) as totalQuantity " +
           "FROM OutboundRecord o WHERE o.outDate >= :startDate " +
           "GROUP BY o.outDate ORDER BY o.outDate")
    List<Object[]> findOutboundTrendData(@Param("startDate") LocalDate startDate);

    /**
     * 统计指定商品截止到指定日期的累计出库数量
     */
    @Query("SELECT COALESCE(SUM(o.quantity), 0) FROM OutboundRecord o " +
           "WHERE o.productId = :productId AND o.outDate <= :date")
    Integer sumQuantityByProductIdBeforeDate(@Param("productId") Long productId, 
                                           @Param("date") LocalDate date);

    /**
     * 查询所有商品截止到指定日期的累计出库数量
     */
    @Query("SELECT o.productId, COALESCE(SUM(o.quantity), 0) FROM OutboundRecord o " +
           "WHERE o.outDate <= :date GROUP BY o.productId")
    List<Object[]> sumQuantityGroupByProductBeforeDate(@Param("date") LocalDate date);
} 
