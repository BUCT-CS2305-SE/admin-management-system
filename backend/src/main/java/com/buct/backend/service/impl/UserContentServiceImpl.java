package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buct.backend.dto.UserContentQueryDTO;
import com.buct.backend.common.PageResult;
import com.buct.backend.entity.UserContent;
import com.buct.backend.mapper.UserContentMapper;
import com.buct.backend.service.UserContentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserContentServiceImpl implements UserContentService {

    private final UserContentMapper userContentMapper;

    // 构造器注入
    public UserContentServiceImpl(UserContentMapper userContentMapper) {
        this.userContentMapper = userContentMapper;
    }

    @Override
    public PageResult<UserContent> pageList(UserContentQueryDTO dto) {
        Page<UserContent> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<UserContent> wrapper = new LambdaQueryWrapper<>();
        userContentMapper.selectPage(page, wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public void approve(Long id, Long auditorId) {
        UserContent content = userContentMapper.selectById(id);
        content.setAuditStatus(1);
        content.setAuditTime(LocalDateTime.now());
        userContentMapper.updateById(content);
    }

    @Override
    public void reject(Long id, Long auditorId, String reason) {
        UserContent content = userContentMapper.selectById(id);
        content.setAuditStatus(2);
        content.setRejectReason(reason);
        content.setAuditTime(LocalDateTime.now());
        userContentMapper.updateById(content);
    }

    @Override
    public void recheck(Long id) {
        UserContent content = userContentMapper.selectById(id);
        content.setAuditStatus(3);
        userContentMapper.updateById(content);
    }
}