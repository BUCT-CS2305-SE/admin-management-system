package com.buct.backend.controller;

import com.buct.backend.dto.UserContentQueryDTO;
import com.buct.backend.entity.UserContent;
import com.buct.backend.common.PageResult;
import com.buct.backend.common.Result;
import com.buct.backend.service.UserContentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/contents")
public class UserContentController {

    private final UserContentService userContentService;

    // 手动添加构造器，替代 @RequiredArgsConstructor，解决报错
    public UserContentController(UserContentService userContentService) {
        this.userContentService = userContentService;
    }

    // 1.待审核内容分页列表
    @GetMapping("/page")
    public Result<PageResult<UserContent>> page(UserContentQueryDTO dto) {
        return Result.success(userContentService.pageList(dto));
    }

    // 2.审核通过（auditorId暂时硬编码1，后续登录后替换）
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        userContentService.approve(id, 1L);
        return Result.success();
    }

    // 3.审核拒绝
    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        userContentService.reject(id, 1L, reason);
        return Result.success();
    }

    // 4.标记复审
    @PutMapping("/{id}/recheck")
    public Result<Void> recheck(@PathVariable Long id) {
        userContentService.recheck(id);
        return Result.success();
    }
}