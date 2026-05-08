package com.buct.backend.controller;

import com.buct.backend.common.Result;
import com.buct.backend.entity.Permission;
import com.buct.backend.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public Result<List<Permission>> listPermissions() {
        return Result.success(permissionService.listPermissions());
    }
}