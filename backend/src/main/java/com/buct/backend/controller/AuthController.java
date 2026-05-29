package com.buct.backend.controller;

import com.buct.backend.common.Result;
import com.buct.backend.dto.LoginDTO;
import com.buct.backend.entity.AdminUser;
import com.buct.backend.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AdminUserService adminUserService;

    public AuthController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping("/login")
    public Result<AdminUser> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String ip = getClientIp(request);
        AdminUser adminUser = adminUserService.login(loginDTO, ip);
        return Result.success(adminUser);
    }

    @GetMapping("/profile")
    public Result<AdminUser> getProfile() {
        AdminUser adminUser = adminUserService.getProfile(1L);
        return Result.success(adminUser);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}