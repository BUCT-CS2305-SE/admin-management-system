package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buct.backend.common.BusinessException;
import com.buct.backend.dto.RoleSaveDTO;
import com.buct.backend.entity.Role;
import com.buct.backend.entity.RolePermission;
import com.buct.backend.entity.Permission;
import com.buct.backend.mapper.RoleMapper;
import com.buct.backend.mapper.RolePermissionMapper;
import com.buct.backend.mapper.PermissionMapper;
import com.buct.backend.service.OperationLogService;
import com.buct.backend.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final OperationLogService operationLogService;

    public RoleServiceImpl(RoleMapper roleMapper,
                           RolePermissionMapper rolePermissionMapper,
                           PermissionMapper permissionMapper,
                           OperationLogService operationLogService) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public List<Role> listRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    public void addRole(RoleSaveDTO saveDTO) {
        checkRoleCodeUnique(saveDTO.getRoleCode(), null);

        Role role = new Role();
        role.setRoleName(saveDTO.getRoleName());
        role.setRoleCode(saveDTO.getRoleCode());
        role.setDescription(saveDTO.getDescription());
        role.setCreateTime(LocalDateTime.now());
        roleMapper.insert(role);
        operationLogService.record("角色权限管理", "新增角色", "role", String.valueOf(role.getId()), null, role.getRoleCode());
    }

    @Override
    public void updateRole(Long roleId, RoleSaveDTO saveDTO) {
        Role role = getRoleOrThrow(roleId);
        checkRoleCodeUnique(saveDTO.getRoleCode(), roleId);

        String before = role.getRoleName() + "/" + role.getRoleCode();
        role.setRoleName(saveDTO.getRoleName());
        role.setRoleCode(saveDTO.getRoleCode());
        role.setDescription(saveDTO.getDescription());
        roleMapper.updateById(role);
        operationLogService.record("角色权限管理", "修改角色", "role", String.valueOf(roleId), before, role.getRoleName() + "/" + role.getRoleCode());
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = getRoleOrThrow(roleId);
        if (roleId <= 4) {
            throw new BusinessException("系统内置角色不允许删除");
        }

        LambdaQueryWrapper<RolePermission> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(deleteWrapper);
        roleMapper.deleteById(roleId);
        operationLogService.record("角色权限管理", "删除角色", "role", String.valueOf(roleId), role.getRoleCode(), null);
    }

    @Override
    public List<Permission> listPermissionsByRoleId(Long roleId) {
        getRoleOrThrow(roleId);

        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        List<Long> permissionIds = rolePermissionMapper.selectList(wrapper)
                .stream()
                .map(RolePermission::getPermissionId)
                .toList();

        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionMapper.selectBatchIds(permissionIds);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        getRoleOrThrow(roleId);

        LambdaQueryWrapper<RolePermission> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(deleteWrapper);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                Permission permission = permissionMapper.selectById(permissionId);
                if (permission == null) {
                    throw new BusinessException("权限ID " + permissionId + " 不存在");
                }

                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }
        operationLogService.record("角色权限管理", "分配权限", "role", String.valueOf(roleId), null, String.valueOf(permissionIds));
    }

    private Role getRoleOrThrow(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    private void checkRoleCodeUnique(String roleCode, Long currentId) {
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException("角色编码不能为空");
        }
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode)
                .ne(currentId != null, Role::getId, currentId);
        Long count = roleMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("角色编码已存在");
        }
    }
}
