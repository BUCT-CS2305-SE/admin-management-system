package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buct.backend.common.BusinessException;
import com.buct.backend.common.PageResult;
import com.buct.backend.dto.PlatformUserQueryDTO;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.entity.UserContent;
import com.buct.backend.mapper.PlatformUserMapper;
import com.buct.backend.mapper.UserContentMapper;
import com.buct.backend.service.OperationLogService;
import com.buct.backend.service.PlatformUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformUserServiceImpl extends ServiceImpl<PlatformUserMapper, PlatformUser> implements PlatformUserService {

    @Autowired
    private UserContentMapper userContentMapper;

    @Autowired
    private OperationLogService operationLogService;

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

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        checkBatch(ids, status, "用户状态不能为空");
        for (Long id : ids) {
            PlatformUser user = new PlatformUser();
            user.setId(id);
            user.setStatus(status);
            updateById(user);
        }
        operationLogService.record("平台用户管理", "批量更新用户状态", "platform_user", String.valueOf(ids), null, "status=" + status);
    }

    @Override
    public void batchUpdateBanComment(List<Long> ids, Integer banComment) {
        checkBatch(ids, banComment, "禁评状态不能为空");
        for (Long id : ids) {
            PlatformUser user = new PlatformUser();
            user.setId(id);
            user.setBanComment(banComment);
            updateById(user);
        }
        operationLogService.record("平台用户管理", "批量设置禁评", "platform_user", String.valueOf(ids), null, "banComment=" + banComment);
    }

    @Override
    public void batchUpdateBanUpload(List<Long> ids, Integer banUpload) {
        checkBatch(ids, banUpload, "禁传状态不能为空");
        for (Long id : ids) {
            PlatformUser user = new PlatformUser();
            user.setId(id);
            user.setBanUpload(banUpload);
            updateById(user);
        }
        operationLogService.record("平台用户管理", "批量设置禁传", "platform_user", String.valueOf(ids), null, "banUpload=" + banUpload);
    }

    private void checkBatch(List<Long> ids, Integer value, String message) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要操作的用户");
        }
        if (value == null) {
            throw new BusinessException(message);
        }
    }
}
