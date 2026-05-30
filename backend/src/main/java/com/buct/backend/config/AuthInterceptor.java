package com.buct.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.buct.backend.common.AuthContext;
import com.buct.backend.common.AuthUser;
import com.buct.backend.common.AuthUserType;
import com.buct.backend.common.Result;
import com.buct.backend.entity.AdminUser;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.mapper.AdminUserMapper;
import com.buct.backend.mapper.PlatformUserMapper;
import com.buct.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AdminUserMapper adminUserMapper;
    private final PlatformUserMapper platformUserMapper;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtil jwtUtil,
                           AdminUserMapper adminUserMapper,
                           PlatformUserMapper platformUserMapper,
                           ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.adminUserMapper = adminUserMapper;
        this.platformUserMapper = platformUserMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            writeError(response, 401, "请先登录");
            return false;
        }

        AuthUser authUser;
        try {
            authUser = jwtUtil.verify(token);
        } catch (Exception e) {
            writeError(response, 401, "登录已过期或令牌无效");
            return false;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/") && !AuthUserType.ADMIN.equals(authUser.getUserType())) {
            writeError(response, 403, "当前令牌不是后台管理员令牌");
            return false;
        }
        if (path.startsWith("/api/user/") && !AuthUserType.PLATFORM_USER.equals(authUser.getUserType())) {
            writeError(response, 403, "当前令牌不是平台用户令牌");
            return false;
        }

        if (AuthUserType.ADMIN.equals(authUser.getUserType())) {
            AdminUser adminUser = adminUserMapper.selectById(authUser.getUserId());
            if (adminUser == null || adminUser.getStatus() == null || adminUser.getStatus() == 0) {
                writeError(response, 403, "后台管理员不存在或已被禁用");
                return false;
            }
            authUser.setUsername(adminUser.getUsername());
            authUser.setRoleId(adminUser.getRoleId());
        }

        if (AuthUserType.PLATFORM_USER.equals(authUser.getUserType())) {
            PlatformUser platformUser = platformUserMapper.selectById(authUser.getUserId());
            if (platformUser == null || platformUser.getStatus() == null || platformUser.getStatus() == 0) {
                writeError(response, 403, "平台用户不存在或已被禁用");
                return false;
            }
            authUser.setUsername(platformUser.getUsername());
        }

        AuthContext.set(authUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("X-Token");
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Result<Void> result = new Result<>(code, message, null);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
