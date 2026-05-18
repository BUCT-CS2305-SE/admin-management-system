package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buct.backend.common.PageResult;
import com.buct.backend.dto.PlatformUserQueryDTO;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.entity.UserContent;
import com.buct.backend.mapper.PlatformUserMapper;
import com.buct.backend.mapper.UserContentMapper;
import com.buct.backend.service.PlatformUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformUserServiceImpl extends ServiceImpl<PlatformUserMapper, PlatformUser> implements PlatformUserService {

    @Autowired
    private UserContentMapper userContentMapper;

    @Override
    public PageResult<PlatformUser> page(PlatformUserQueryDTO dto) {
        Page<PlatformUser> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<PlatformUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(dto.getUsername() != null, PlatformUser::getUsername, dto.getUsername());
        wrapper.eq(dto.getSource() != null, PlatformUser::getSource, dto.getSource());
        wrapper.eq(dto.getStatus() != null, PlatformUser::getStatus, dto.getStatus());

        page(page, wrapper);

        return PageResult.of(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    @Override
    public List<UserContent> getUserContents(Long userId) {
        LambdaQueryWrapper<UserContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserContent::getUserId, userId);
        wrapper.orderByDesc(UserContent::getSubmitTime);
        return userContentMapper.selectList(wrapper);
    }
}