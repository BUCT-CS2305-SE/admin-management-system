package com.buct.backend.service;
import com.buct.backend.dto.UserContentQueryDTO;
import com.buct.backend.entity.UserContent;
import com.buct.backend.common.PageResult;

public interface UserContentService {
    // 分页列表
    PageResult<UserContent> pageList(UserContentQueryDTO dto);
    // 审核通过
    void approve(Long id, Long auditorId);
    // 审核拒绝
    void reject(Long id, Long auditorId, String reason);
    // 标记复审
    void recheck(Long id);
}