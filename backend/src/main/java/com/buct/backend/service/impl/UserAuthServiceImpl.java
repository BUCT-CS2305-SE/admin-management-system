package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buct.backend.common.AuthUser;
import com.buct.backend.common.AuthUserType;
import com.buct.backend.common.BusinessException;
import com.buct.backend.dto.LoginDTO;
import com.buct.backend.dto.UserLoginResponseDTO;
import com.buct.backend.dto.UserRegisterDTO;
import com.buct.backend.entity.LoginLog;
import com.buct.backend.entity.Permission;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.entity.Role;
import com.buct.backend.entity.RolePermission;
import com.buct.backend.mapper.LoginLogMapper;
import com.buct.backend.mapper.PermissionMapper;
import com.buct.backend.mapper.PlatformUserMapper;
import com.buct.backend.mapper.RoleMapper;
import com.buct.backend.mapper.RolePermissionMapper;
import com.buct.backend.service.UserAuthService;
import com.buct.backend.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    /** 默认普通用户角色编码（init.sql 中 id=4）。 */
    private static final String DEFAULT_ROLE_CODE = "NORMAL_USER";

    private final PlatformUserMapper platformUserMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserAuthServiceImpl(PlatformUserMapper platformUserMapper,
                               RoleMapper roleMapper,
                               RolePermissionMapper rolePermissionMapper,
                               PermissionMapper permissionMapper,
                               LoginLogMapper loginLogMapper,
                               JwtUtil jwtUtil) {
        this.platformUserMapper = platformUserMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.loginLogMapper = loginLogMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserLoginResponseDTO login(LoginDTO loginDTO, String ip) {
        LambdaQueryWrapper<PlatformUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformUser::getUsername, loginDTO.getUsername());
        PlatformUser user = platformUserMapper.selectOne(wrapper);

        if (user == null) {
            recordLoginLog(null, loginDTO.getUsername(), 0, ip, "用户名不存在");
            throw new BusinessException("用户名或密码错误");
        }

        if (!StringUtils.hasText(user.getPassword())
                || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            recordLoginLog(user.getId(), user.getUsername(), 0, ip, "密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            recordLoginLog(user.getId(), user.getUsername(), 0, ip, "账号已禁用");
            throw new BusinessException("账号已被禁用");
        }

        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        platformUserMapper.updateById(user);

        recordLoginLog(user.getId(), user.getUsername(), 1, ip, null);

        return buildLoginResponse(user);
    }

    @Override
    public UserLoginResponseDTO register(UserRegisterDTO registerDTO, String ip) {
        LambdaQueryWrapper<PlatformUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformUser::getUsername, registerDTO.getUsername());
        if (platformUserMapper.selectOne(wrapper) != null) {
            throw new BusinessException("用户名已存在");
        }

        Role defaultRole = findDefaultRole();
        if (defaultRole == null) {
            throw new BusinessException("默认用户角色未初始化（缺少 NORMAL_USER）");
        }

        PlatformUser user = new PlatformUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRoleId(defaultRole.getId());
        user.setEmail(StringUtils.hasText(registerDTO.getEmail()) ? registerDTO.getEmail() : "");
        user.setPhone(StringUtils.hasText(registerDTO.getPhone()) ? registerDTO.getPhone() : "");
        user.setAvatar("");
        user.setSource(StringUtils.hasText(registerDTO.getSource()) ? registerDTO.getSource() : "WEB");
        user.setStatus(1);
        user.setBanComment(0);
        user.setBanUpload(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        platformUserMapper.insert(user);

        recordLoginLog(user.getId(), user.getUsername(), 1, ip, "register");

        return buildLoginResponse(user);
    }

    @Override
    public PlatformUser getProfile(Long userId) {
        PlatformUser user = platformUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRoleId() != null) {
            Role role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                user.setRoleName(role.getRoleName());
            }
        }
        return user;
    }

    // ---------- helpers ----------

    private UserLoginResponseDTO buildLoginResponse(PlatformUser user) {
        Role role = user.getRoleId() == null ? null : roleMapper.selectById(user.getRoleId());
        List<String> permissions = loadPermissionCodes(user.getRoleId());

        AuthUser authUser = new AuthUser(
                user.getId(), user.getUsername(), AuthUserType.PLATFORM_USER, user.getRoleId());
        authUser.setPermissionCodes(permissions);

        if (role != null) {
            user.setRoleName(role.getRoleName());
        }

        UserLoginResponseDTO resp = new UserLoginResponseDTO();
        resp.setToken(jwtUtil.createToken(authUser));
        resp.setTokenType("Bearer");
        resp.setUserType(AuthUserType.PLATFORM_USER);
        resp.setExpireAt(jwtUtil.getExpireAt());
        resp.setUser(user);
        resp.setRoleCode(role == null ? null : role.getRoleCode());
        resp.setRoleName(role == null ? null : role.getRoleName());
        resp.setPermissions(permissions);
        return resp;
    }

    private List<String> loadPermissionCodes(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        List<Long> permissionIds = rolePermissionMapper.selectList(wrapper)
                .stream()
                .map(RolePermission::getPermissionId)
                .toList();
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionMapper.selectBatchIds(permissionIds)
                .stream()
                .map(Permission::getPermissionCode)
                .filter(Objects::nonNull)
                .toList();
    }

    private Role findDefaultRole() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, DEFAULT_ROLE_CODE);
        return roleMapper.selectOne(wrapper);
    }

    private void recordLoginLog(Long userId, String username, int status, String ip, String reason) {
        try {
            LoginLog log = new LoginLog();
            log.setAdminId(userId);
            log.setUsername(username == null ? "" : username);
            log.setLoginStatus(status);
            log.setIpAddress(ip == null ? "" : ip);
            log.setFailReason(reason == null ? "" : reason);
            log.setLoginTime(LocalDateTime.now());
            loginLogMapper.insert(log);
        } catch (Exception ignored) {
        }
    }
}
