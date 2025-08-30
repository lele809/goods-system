package com.shelf.repository;

import com.shelf.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 管理员数据访问接口
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    /**
     * 根据管理员用户名查询
     */
    Optional<Admin> findByAdminName(String adminName);

    /**
     * 检查管理员用户名是否存在
     */
    boolean existsByAdminName(String adminName);

    /**
     * 更新最后登录时间
     */
    @Modifying
    @Query("UPDATE Admin a SET a.lastLogin = :lastLogin WHERE a.id = :id")
    void updateLastLogin(@Param("id") Integer id, @Param("lastLogin") LocalDateTime lastLogin);
} 