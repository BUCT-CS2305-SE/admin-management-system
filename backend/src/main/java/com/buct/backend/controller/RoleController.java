package com.buct.backend.controller;

import com.buct.backend.common.Result;
import com.buct.backend.dto.RolePermissionAssignDTO;
import com.buct.backend.dto.RoleSaveDTO;
import com.buct.backend.entity.Permission;
import com.buct.backend.entity.Role;
import com.buct.backend.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    public Result<Void> addRole(@Valid @RequestBody RoleSaveDTO saveDTO) {
        roleService.addRole(saveDTO);
        return Result.success();
    }

    @PutMapping("/{roleId}")
    public Result<Void> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleSaveDTO saveDTO) {
        roleService.updateRole(roleId, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{roleId}")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return Result.success();
    }

    @GetMapping("/{roleId}/permissions")
    public Result<List<Permission>> listRolePermissions(@PathVariable Long roleId) {
        return Result.success(roleService.listPermissionsByRoleId(roleId));
    }

    @PutMapping("/{roleId}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long roleId, @RequestBody RolePermissionAssignDTO assignDTO) {
        roleService.assignPermissions(roleId, assignDTO.getPermissionIds());
        return Result.success();
    }
}
