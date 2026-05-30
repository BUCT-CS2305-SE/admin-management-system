package com.buct.backend.service;

import com.buct.backend.dto.RoleSaveDTO;
import com.buct.backend.entity.Role;
import com.buct.backend.entity.Permission;

import java.util.List;

public interface RoleService {

    List<Role> listRoles();

    void addRole(RoleSaveDTO saveDTO);

    void updateRole(Long roleId, RoleSaveDTO saveDTO);

    void deleteRole(Long roleId);

    List<Permission> listPermissionsByRoleId(Long roleId);

    void assignPermissions(Long roleId, List<Long> permissionIds);
}
