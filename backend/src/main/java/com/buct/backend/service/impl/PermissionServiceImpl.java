package com.buct.backend.service.impl;

import com.buct.backend.entity.Permission;
import com.buct.backend.mapper.PermissionMapper;
import com.buct.backend.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<Permission> listPermissions() {
        return permissionMapper.selectList(null);
    }
}