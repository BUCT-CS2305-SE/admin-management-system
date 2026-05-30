package com.buct.backend.service;

import com.buct.backend.dto.ContentQueryDTO;
import com.buct.backend.dto.ContentRejectDTO;
import com.buct.backend.entity.UserContent;
import com.buct.backend.common.PageResult;

import java.util.List;

public interface ContentService {
    // 分页查询
    PageResult<UserContent> pageList(ContentQueryDTO dto);

    // 详情
    UserContent getById(Long id);

    // 审核通过
    void approve(Long id);

    // 审核拒绝
    void reject(Long id, ContentRejectDTO dto);

    // 标记复审
    void recheck(Long id);

    // 删除
    void delete(Long id);

    void batchApprove(List<Long> ids);

    void batchReject(List<Long> ids, String rejectReason);
}
