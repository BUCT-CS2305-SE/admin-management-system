package com.buct.backend.controller;

import com.buct.backend.common.AuthContext;
import com.buct.backend.common.AuthUser;
import com.buct.backend.common.AuthUserType;
import com.buct.backend.common.Result;
import com.buct.backend.dto.LoginDTO;
import com.buct.backend.dto.LoginResponseDTO;
import com.buct.backend.entity.AdminUser;
import com.buct.backend.service.AdminUserService;
import com.buct.backend.util.JwtUtil;
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
    private final JwtUtil jwtUtil;

    public AuthController(AdminUserService adminUserService, JwtUtil jwtUtil) {
        this.adminUserService = adminUserService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String ip = getClientIp(request);
        AdminUser adminUser = adminUserService.login(loginDTO, ip);
        AuthUser authUser = new AuthUser(adminUser.getId(), adminUser.getUsername(), AuthUserType.ADMIN, adminUser.getRoleId());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(jwtUtil.createToken(authUser));
        response.setTokenType("Bearer");
        response.setUserType(AuthUserType.ADMIN);
        response.setExpireAt(jwtUtil.getExpireAt());
        response.setAdminUser(adminUser);
        return Result.success(response);
    }

    @GetMapping("/profile")
    public Result<AdminUser> getProfile() {
        AdminUser adminUser = adminUserService.getProfile(AuthContext.getCurrentUserId());
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
