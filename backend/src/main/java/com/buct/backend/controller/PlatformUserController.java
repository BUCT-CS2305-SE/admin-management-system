package com.buct.backend.controller;

import com.buct.backend.common.PageResult;
import com.buct.backend.common.Result;
import com.buct.backend.dto.PlatformUserQueryDTO;
import com.buct.backend.entity.PlatformUser;
import com.buct.backend.entity.UserContent;
import com.buct.backend.service.PlatformUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/platform-users")
public class PlatformUserController {

    @Autowired
    private PlatformUserService platformUserService;

    // 1. 分页查询 + 筛选
    @GetMapping("/page")
    public Result<PageResult<PlatformUser>> page(PlatformUserQueryDTO dto) {
        return Result.success(platformUserService.page(dto));
    }

    // 2. 用户详情
    @GetMapping("/{id}")
    public Result<PlatformUser> getById(@PathVariable Long id) {
        return Result.success(platformUserService.getById(id));
    }

    // 3. 启用/禁用用户
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> param) {
        PlatformUser user = new PlatformUser();
        user.setId(id);
        user.setStatus(param.get("status"));
        platformUserService.updateById(user);
        return Result.success();
    }

    // 4. 禁止/允许评论
    @PutMapping("/{id}/ban-comment")
    public Result<Void> banComment(@PathVariable Long id, @RequestBody Map<String, Integer> param) {
        PlatformUser user = new PlatformUser();
        user.setId(id);
        user.setBanComment(param.get("banComment"));
        platformUserService.updateById(user);
        return Result.success();
    }

    // 5. 禁止/允许上传
    @PutMapping("/{id}/ban-upload")
    public Result<Void> banUpload(@PathVariable Long id, @RequestBody Map<String, Integer> param) {
        PlatformUser user = new PlatformUser();
        user.setId(id);
        user.setBanUpload(param.get("banUpload"));
        platformUserService.updateById(user);
        return Result.success();
    }

    // 6. 查看用户提交内容（你刚加的第7个功能）
    @GetMapping("/{userId}/contents")
    public Result<List<UserContent>> getUserContents(@PathVariable Long userId) {
        return Result.success(platformUserService.getUserContents(userId));
    }
}