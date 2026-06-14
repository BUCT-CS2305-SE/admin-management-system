package com.buct.backend.service;

import com.buct.backend.common.PageResult;
import com.buct.backend.dto.AdminUserQueryDTO;
import com.buct.backend.dto.AdminUserSaveDTO;
import com.buct.backend.dto.LoginDTO;
import com.buct.backend.entity.AdminUser;

public interface AdminUserService {

    AdminUser login(LoginDTO loginDTO, String ip);

    AdminUser getProfile(Long adminId);

    PageResult<AdminUser> pageAdminUsers(AdminUserQueryDTO queryDTO);

    void addAdminUser(AdminUserSaveDTO saveDTO);

    void updateAdminUser(Long id, AdminUserSaveDTO saveDTO);

    void updateAdminUserStatus(Long id, Integer status);
}