package com.buct.backend.service;

import com.buct.backend.entity.Role;

import java.util.List;

public interface RoleService {

    List<Role> listRoles();

    void assignPermissions(Long roleId, List<Long> permissionIds);
}