package com.buct.backend.controller;

import com.buct.backend.common.PageResult;
import com.buct.backend.common.Result;
import com.buct.backend.dto.AdminUserQueryDTO;
import com.buct.backend.dto.AdminUserSaveDTO;
import com.buct.backend.entity.AdminUser;
import com.buct.backend.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/admin-users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/page")
    public Result<PageResult<AdminUser>> pageAdminUsers(AdminUserQueryDTO queryDTO) {
        return Result.success(adminUserService.pageAdminUsers(queryDTO));
    }

    @PostMapping
    public Result<Void> addAdminUser(@Valid @RequestBody AdminUserSaveDTO saveDTO) {
        adminUserService.addAdminUser(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateAdminUser(@PathVariable Long id, @Valid @RequestBody AdminUserSaveDTO saveDTO) {
        adminUserService.updateAdminUser(id, saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateAdminUserStatus(@PathVariable Long id, @RequestBody AdminUserStatusDTO statusDTO) {
        adminUserService.updateAdminUserStatus(id, statusDTO.getStatus());
        return Result.success();
    }

    public static class AdminUserStatusDTO {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}