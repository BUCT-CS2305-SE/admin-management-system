package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buct.backend.dto.ContentQueryDTO;
import com.buct.backend.dto.ContentRejectDTO;
import com.buct.backend.entity.UserContent;
import com.buct.backend.mapper.UserContentMapper;
import com.buct.backend.common.BusinessException;
import com.buct.backend.common.PageResult;
import com.buct.backend.service.OperationLogService;
import com.buct.backend.service.ContentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    private final UserContentMapper mapper;
    private final OperationLogService operationLogService;

    public ContentServiceImpl(UserContentMapper mapper, OperationLogService operationLogService) {
        this.mapper = mapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public PageResult<UserContent> pageList(ContentQueryDTO dto) {
        Page<UserContent> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<UserContent> qw = new LambdaQueryWrapper<>();

        if (dto.getContentType() != null) {
            qw.eq(UserContent::getContentType, dto.getContentType());
        }
        if (dto.getSource() != null) {
            qw.eq(UserContent::getSource, dto.getSource());
        }
        if (dto.getAuditStatus() != null) {
            qw.eq(UserContent::getAuditStatus, dto.getAuditStatus());
        }
        if (dto.getUserId() != null) {
            qw.eq(UserContent::getUserId, dto.getUserId());
        }

        // 用ID排序，绝对不报错
        qw.orderByDesc(UserContent::getId);

        Page<UserContent> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public UserContent getById(Long id) {
        UserContent content = mapper.selectById(id);
        if (content == null) {
            throw new BusinessException("内容不存在");
        }
        return content;
    }

    @Override
    public void approve(Long id) {
        UserContent content = getById(id);
        content.setAuditStatus(1);
        content.setAuditTime(LocalDateTime.now());
        mapper.updateById(content);
        operationLogService.record("内容审核", "审核通过", "user_content", String.valueOf(id), null, "auditStatus=1");
    }

    @Override
    public void reject(Long id, ContentRejectDTO dto) {
        UserContent content = getById(id);
        content.setAuditStatus(2);
        content.setRejectReason(dto.getRejectReason());
        content.setAuditTime(LocalDateTime.now());
        mapper.updateById(content);
        operationLogService.record("内容审核", "审核拒绝", "user_content", String.valueOf(id), null, dto.getRejectReason());
    }

    @Override
    public void recheck(Long id) {
        UserContent content = getById(id);
        content.setAuditStatus(3);
        mapper.updateById(content);
        operationLogService.record("内容审核", "标记复审", "user_content", String.valueOf(id), null, "auditStatus=3");
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
        operationLogService.record("内容审核", "删除内容", "user_content", String.valueOf(id), null, null);
    }

    @Override
    public void batchApprove(List<Long> ids) {
        checkIds(ids);
        for (Long id : ids) {
            approve(id);
        }
        operationLogService.record("内容审核", "批量通过", "user_content", String.valueOf(ids), null, "count=" + ids.size());
    }

    @Override
    public void batchReject(List<Long> ids, String rejectReason) {
        checkIds(ids);
        ContentRejectDTO dto = new ContentRejectDTO();
        dto.setRejectReason(rejectReason);
        for (Long id : ids) {
            reject(id, dto);
        }
        operationLogService.record("内容审核", "批量拒绝", "user_content", String.valueOf(ids), null, rejectReason);
    }

    private void checkIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要操作的内容");
        }
    }
}
