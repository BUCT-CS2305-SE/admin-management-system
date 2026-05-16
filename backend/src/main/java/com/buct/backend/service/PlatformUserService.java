package com.buct.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buct.backend.dto.PlatformUserQueryDTO;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.common.PageResult;

public interface PlatformUserService extends IService<PlatformUser> {
    PageResult<PlatformUser> page(PlatformUserQueryDTO dto);
}