package com.buct.backend.service;

import com.buct.backend.entity.Permission;

import java.util.List;

public interface PermissionService {

    List<Permission> listPermissions();
}