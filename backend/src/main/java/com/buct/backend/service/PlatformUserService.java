package com.buct.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buct.backend.common.PageResult;
import com.buct.backend.dto.PlatformUserQueryDTO;
import com.buct.backend.dto.PlatformUserSaveDTO;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.entity.UserContent;

import java.util.List;

public interface PlatformUserService extends IService<PlatformUser> {
    PageResult<PlatformUser> page(PlatformUserQueryDTO dto);

    void addPlatformUser(PlatformUserSaveDTO saveDTO);

    void updatePlatformUser(Long id, PlatformUserSaveDTO saveDTO);

    void deletePlatformUser(Long id);

    // 查询用户提交的内容
    List<UserContent> getUserContents(Long userId);

    void batchUpdateStatus(List<Long> ids, Integer status);

    void batchUpdateBanComment(List<Long> ids, Integer banComment);

    void batchUpdateBanUpload(List<Long> ids, Integer banUpload);
}
