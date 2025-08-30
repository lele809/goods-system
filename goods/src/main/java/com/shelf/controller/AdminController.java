package com.shelf.controller;

import com.shelf.dto.ApiResponse;
import com.shelf.entity.Admin;
import com.shelf.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/admin/login")
    public ApiResponse<Admin> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String adminName = loginRequest.get("adminName");
            String password = loginRequest.get("password");
            
            if (adminName == null || adminName.trim().isEmpty()) {
                return ApiResponse.badRequest("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return ApiResponse.badRequest("密码不能为空");
            }

            Optional<Admin> adminOpt = adminService.login(adminName, password);
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                // 更新最后登录时间
                adminService.updateLastLogin(admin.getId());
                // 不返回密码信息
                admin.setPassword(null);
                return ApiResponse.success("登录成功", admin);
            } else {
                return ApiResponse.error(401, "用户名或密码错误");
            }
        } catch (Exception e) {
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 检查管理员用户名是否存在
     */
    @GetMapping("/admin/check/{adminName}")
    public ApiResponse<Boolean> checkAdminExists(@PathVariable String adminName) {
        try {
            boolean exists = adminService.existsByAdminName(adminName);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.error("检查用户名失败: " + e.getMessage());
        }
    }

    /**
     * 获取管理员信息（根据用户名）
     */
    @GetMapping("/admin/{adminName}")
    public ApiResponse<Admin> getAdminByName(@PathVariable String adminName) {
        try {
            Optional<Admin> adminOpt = adminService.getAdminByName(adminName);
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                // 不返回密码信息
                admin.setPassword(null);
                return ApiResponse.success(admin);
            } else {
                return ApiResponse.notFound("管理员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("获取管理员信息失败: " + e.getMessage());
        }
    }
} 