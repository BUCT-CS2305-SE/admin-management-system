package com.buct.backend.controller;

import com.buct.backend.common.Result;
import com.buct.backend.dto.RolePermissionAssignDTO;
import com.buct.backend.entity.Role;
import com.buct.backend.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public Result<List<Role>> listRoles() {
        return Result.success(roleService.listRoles());
    }

    @PutMapping("/{roleId}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long roleId, @RequestBody RolePermissionAssignDTO assignDTO) {
        roleService.assignPermissions(roleId, assignDTO.getPermissionIds());
        return Result.success();
    }
}