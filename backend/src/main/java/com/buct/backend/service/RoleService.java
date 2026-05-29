package com.buct.backend.service;

import com.buct.backend.entity.Role;
import com.buct.backend.entity.Permission;

import java.util.List;

public interface RoleService {

    List<Role> listRoles();

    List<Permission> listPermissionsByRoleId(Long roleId);

    void assignPermissions(Long roleId, List<Long> permissionIds);
}
