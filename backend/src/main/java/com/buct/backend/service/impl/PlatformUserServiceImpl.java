package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buct.backend.common.BusinessException;
import com.buct.backend.common.PageResult;
import com.buct.backend.dto.PlatformUserQueryDTO;
import com.buct.backend.dto.PlatformUserSaveDTO;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.entity.Role;
import com.buct.backend.entity.UserContent;
import com.buct.backend.mapper.PlatformUserMapper;
import com.buct.backend.mapper.RoleMapper;
import com.buct.backend.mapper.UserContentMapper;
import com.buct.backend.service.OperationLogService;
import com.buct.backend.service.PlatformUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import java.util.List;

@Service
public class PlatformUserServiceImpl extends ServiceImpl<PlatformUserMapper, PlatformUser> implements PlatformUserService {

    @Autowired
    private UserContentMapper userContentMapper;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private RoleMapper roleMapper;

    private static final String DEFAULT_ROLE_CODE = "NORMAL_USER";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
    public void addPlatformUser(PlatformUserSaveDTO saveDTO) {
        checkUsernameUnique(saveDTO.getUsername(), null);

        PlatformUser user = new PlatformUser();
        fillUser(user, saveDTO);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
        operationLogService.record("平台用户管理", "新增平台用户", "platform_user", String.valueOf(user.getId()), null, user.getUsername());
    }

    @Override
    public void updatePlatformUser(Long id, PlatformUserSaveDTO saveDTO) {
        PlatformUser user = getById(id);
        if (user == null) {
            throw new BusinessException("平台用户不存在");
        }
        checkUsernameUnique(saveDTO.getUsername(), id);

        String before = user.getUsername();
        fillUser(user, saveDTO);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
        operationLogService.record("平台用户管理", "编辑平台用户", "platform_user", String.valueOf(id), before, user.getUsername());
    }

    @Override
    public void deletePlatformUser(Long id) {
        PlatformUser user = getById(id);
        if (user == null) {
            throw new BusinessException("平台用户不存在");
        }
        removeById(id);
        operationLogService.record("平台用户管理", "删除平台用户", "platform_user", String.valueOf(id), user.getUsername(), null);
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

    private void fillUser(PlatformUser user, PlatformUserSaveDTO saveDTO) {
        user.setUsername(saveDTO.getUsername());
        user.setPhone(saveDTO.getPhone());
        user.setEmail(saveDTO.getEmail());
        user.setAvatar(saveDTO.getAvatar());
        user.setSource(StringUtils.hasText(saveDTO.getSource()) ? saveDTO.getSource() : "WEB");
        user.setStatus(saveDTO.getStatus() == null ? 1 : saveDTO.getStatus());
        user.setBanComment(saveDTO.getBanComment() == null ? 0 : saveDTO.getBanComment());
        user.setBanUpload(saveDTO.getBanUpload() == null ? 0 : saveDTO.getBanUpload());

        // 角色：传了用传的，否则填默认普通用户角色（保持非空）
        Long roleId = saveDTO.getRoleId();
        if (roleId == null) {
            if (user.getRoleId() == null) {
                Role defaultRole = findDefaultRole();
                roleId = defaultRole == null ? null : defaultRole.getId();
            } else {
                roleId = user.getRoleId();
            }
        }
        user.setRoleId(roleId);

        // 密码：仅当后台显式传值时才覆盖；空值跳过（避免清空）
        if (StringUtils.hasText(saveDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(saveDTO.getPassword()));
        }
    }

    private Role findDefaultRole() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, DEFAULT_ROLE_CODE);
        return roleMapper.selectOne(wrapper);
    }

    private void checkUsernameUnique(String username, Long currentId) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("用户名不能为空");
        }
        LambdaQueryWrapper<PlatformUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformUser::getUsername, username)
                .ne(currentId != null, PlatformUser::getId, currentId);
        Long count = count(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("平台用户名已存在");
        }
    }
}
