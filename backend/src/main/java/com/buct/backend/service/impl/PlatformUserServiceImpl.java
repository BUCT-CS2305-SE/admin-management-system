package com.buct.backend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buct.backend.dto.PlatformUserQueryDTO;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.mapper.PlatformUserMapper;
import com.buct.backend.service.PlatformUserService;
import com.buct.backend.common.PageResult;
import org.springframework.stereotype.Service;

@Service
public class PlatformUserServiceImpl extends ServiceImpl<PlatformUserMapper, PlatformUser>
        implements PlatformUserService {

    @Override
    public PageResult<PlatformUser> page(PlatformUserQueryDTO dto) {
        QueryWrapper<PlatformUser> qw = new QueryWrapper<>();
        qw.like(StrUtil.isNotBlank(dto.getUsername()), "username", dto.getUsername());
        qw.eq(StrUtil.isNotBlank(dto.getSource()), "source", dto.getSource());
        qw.eq(dto.getStatus() != null, "status", dto.getStatus());
        qw.eq(dto.getBanComment() != null, "ban_comment", dto.getBanComment());
        qw.eq(dto.getBanUpload() != null, "ban_upload", dto.getBanUpload());
        qw.orderByDesc("create_time");

        Page<PlatformUser> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }
}