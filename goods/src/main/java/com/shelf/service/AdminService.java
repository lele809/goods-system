package com.shelf.service;

import com.shelf.entity.Admin;
import com.shelf.repository.AdminRepository;
import com.shelf.util.ScryptPasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 管理员业务逻辑服务
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final ScryptPasswordEncoder passwordEncoder;

    /**
     * 管理员登录验证
     */
    @Transactional(readOnly = true)
    public Optional<Admin> login(String adminName, String password) {
        Optional<Admin> adminOpt = adminRepository.findByAdminName(adminName);
        
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            // 这里应该使用密码加密验证，当前简化处理
            if (verifyPassword(password, admin.getPassword())) {
                return adminOpt;
            }
        }
        
        return Optional.empty();
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLogin(Integer adminId) {
        adminRepository.updateLastLogin(adminId, LocalDateTime.now());
    }

    /**
     * 检查管理员用户名是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByAdminName(String adminName) {
        return adminRepository.existsByAdminName(adminName);
    }

    /**
     * 根据用户名获取管理员
     */
    @Transactional(readOnly = true)
    public Optional<Admin> getAdminByName(String adminName) {
        return adminRepository.findByAdminName(adminName);
    }

    /**
     * 密码验证（使用scrypt验证）
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 密码加密（使用scrypt加密）
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
} 