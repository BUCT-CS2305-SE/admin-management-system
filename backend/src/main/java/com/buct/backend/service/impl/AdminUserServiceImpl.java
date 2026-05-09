package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buct.backend.common.BusinessException;
import com.buct.backend.common.PageResult;
import com.buct.backend.dto.AdminUserQueryDTO;
import com.buct.backend.dto.AdminUserSaveDTO;
import com.buct.backend.dto.LoginDTO;
import com.buct.backend.entity.AdminUser;
import com.buct.backend.entity.LoginLog;
import com.buct.backend.entity.Role;
import com.buct.backend.mapper.AdminUserMapper;
import com.buct.backend.mapper.LoginLogMapper;
import com.buct.backend.mapper.RoleMapper;
import com.buct.backend.service.AdminUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final RoleMapper roleMapper;
    private final LoginLogMapper loginLogMapper;

    public AdminUserServiceImpl(AdminUserMapper adminUserMapper, RoleMapper roleMapper, LoginLogMapper loginLogMapper) {
        this.adminUserMapper = adminUserMapper;
        this.roleMapper = roleMapper;
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public AdminUser login(LoginDTO loginDTO, String ip) {
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUser::getUsername, loginDTO.getUsername());
        AdminUser adminUser = adminUserMapper.selectOne(queryWrapper);

        if (adminUser == null) {
            throw new BusinessException("用户名不存在");
        }

        if (!adminUser.getPassword().equals(loginDTO.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (adminUser.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        Role role = roleMapper.selectById(adminUser.getRoleId());
        if (role != null) {
            adminUser.setRoleName(role.getRoleName());
        }

        LoginLog loginLog = new LoginLog();
        loginLog.setAdminId(adminUser.getId());
        loginLog.setUsername(adminUser.getUsername());
        loginLog.setLoginIp(ip);
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setCreateTime(LocalDateTime.now());
        loginLogMapper.insert(loginLog);

        adminUser.setLastLoginTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());
        adminUserMapper.updateById(adminUser);

        return adminUser;
    }

    @Override
    public AdminUser getProfile(Long adminId) {
        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (adminUser == null) {
            throw new BusinessException("用户不存在");
        }

        Role role = roleMapper.selectById(adminUser.getRoleId());
        if (role != null) {
            adminUser.setRoleName(role.getRoleName());
        }

        return adminUser;
    }

    @Override
    public PageResult<AdminUser> pageAdminUsers(AdminUserQueryDTO queryDTO) {
        Page<AdminUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getUsername() != null && !queryDTO.getUsername().isEmpty()) {
            queryWrapper.like(AdminUser::getUsername, queryDTO.getUsername());
        }

        IPage<AdminUser> pageResult = adminUserMapper.selectPage(page, queryWrapper);
        List<AdminUser> records = pageResult.getRecords();

        for (AdminUser adminUser : records) {
            Role role = roleMapper.selectById(adminUser.getRoleId());
            if (role != null) {
                adminUser.setRoleName(role.getRoleName());
            }
        }

        return new PageResult<>(records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public void addAdminUser(AdminUserSaveDTO saveDTO) {
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUser::getUsername, saveDTO.getUsername());
        AdminUser existingUser = adminUserMapper.selectOne(queryWrapper);

        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        Role role = roleMapper.selectById(saveDTO.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(saveDTO.getUsername());
        adminUser.setPassword(saveDTO.getPassword());
        adminUser.setRealName(saveDTO.getRealName());
        adminUser.setRoleId(saveDTO.getRoleId());
        adminUser.setStatus(1);
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());

        adminUserMapper.insert(adminUser);
    }

    @Override
    public void updateAdminUser(Long id, AdminUserSaveDTO saveDTO) {
        AdminUser adminUser = adminUserMapper.selectById(id);
        if (adminUser == null) {
            throw new BusinessException("用户不存在");
        }

        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUser::getUsername, saveDTO.getUsername());
        queryWrapper.ne(AdminUser::getId, id);
        AdminUser existingUser = adminUserMapper.selectOne(queryWrapper);

        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        Role role = roleMapper.selectById(saveDTO.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        adminUser.setUsername(saveDTO.getUsername());
        adminUser.setPassword(saveDTO.getPassword());
        adminUser.setRealName(saveDTO.getRealName());
        adminUser.setRoleId(saveDTO.getRoleId());
        adminUser.setUpdateTime(LocalDateTime.now());

        adminUserMapper.updateById(adminUser);
    }

    @Override
    public void updateAdminUserStatus(Long id, Integer status) {
        AdminUser adminUser = adminUserMapper.selectById(id);
        if (adminUser == null) {
            throw new BusinessException("用户不存在");
        }

        adminUser.setStatus(status);
        adminUser.setUpdateTime(LocalDateTime.now());

        adminUserMapper.updateById(adminUser);
    }
}
