package com.buct.backend.controller;

import com.buct.backend.common.AuthContext;
import com.buct.backend.common.Result;
import com.buct.backend.dto.LoginDTO;
import com.buct.backend.dto.UserLoginResponseDTO;
import com.buct.backend.dto.UserRegisterDTO;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台普通用户的鉴权接口（其他客户端 / 子系统调用）。
 * 路径前缀 /api/user/*；登录/注册免鉴权由 WebMvcConfig 配置。
 */
@RestController
@RequestMapping("/api/user/auth")
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO,
                                              HttpServletRequest request) {
        return Result.success(userAuthService.login(loginDTO, getClientIp(request)));
    }

    @PostMapping("/register")
    public Result<UserLoginResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO,
                                                 HttpServletRequest request) {
        return Result.success(userAuthService.register(registerDTO, getClientIp(request)));
    }

    @GetMapping("/profile")
    public Result<PlatformUser> profile() {
        return Result.success(userAuthService.getProfile(AuthContext.getCurrentUserId()));
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }
        return request.getRemoteAddr();
    }
}
