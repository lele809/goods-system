package com.shelf.repository;

import com.shelf.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品数据访问接口
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 根据商品名称模糊查询（分页）
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 根据商品名称和规格模糊查询
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:spec IS NULL OR LOWER(p.spec) LIKE LOWER(CONCAT('%', :spec, '%')))")
    Page<Product> findByNameAndSpecContaining(@Param("name") String name, 
                                            @Param("spec") String spec, 
                                            Pageable pageable);

    /**
     * 查询所有商品（用于导出）
     */
    List<Product> findAllByOrderByCreatedAtDesc();

    /**
     * 根据商品名称精确查询
     */
    List<Product> findByName(String name);

    /**
     * 根据商品名称和规格精确查询
     */
    List<Product> findByNameAndSpec(String name, String spec);

    /**
     * 查询库存不足的商品（库存小于指定数量）
     */
    @Query("SELECT p FROM Product p WHERE p.initialStock < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
}