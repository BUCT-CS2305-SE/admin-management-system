package com.buct.backend.controller;

import com.buct.backend.dto.ContentQueryDTO;
import com.buct.backend.dto.ContentRejectDTO;
import com.buct.backend.dto.BatchIdsDTO;
import com.buct.backend.dto.BatchRejectDTO;
import com.buct.backend.entity.UserContent;
import com.buct.backend.common.PageResult;
import com.buct.backend.common.Result;
import com.buct.backend.service.ContentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/content/audit")
public class ContentController {

    private final ContentService service;

    public ContentController(ContentService service) {
        this.service = service;
    }

    // 分页查询
    @GetMapping("/page")
    public Result<PageResult<UserContent>> page(ContentQueryDTO dto) {
        return Result.success(service.pageList(dto));
    }

    // 详情
    @GetMapping("/{id}")
    public Result<UserContent> get(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    // 审核通过
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        service.approve(id);
        return Result.success();
    }

    // 审核拒绝
    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody ContentRejectDTO dto) {
        service.reject(id, dto);
        return Result.success();
    }

    // 标记复审
    @PutMapping("/{id}/recheck")
    public Result<Void> recheck(@PathVariable Long id) {
        service.recheck(id);
        return Result.success();
    }

    @PutMapping("/batch/approve")
    public Result<Void> batchApprove(@RequestBody BatchIdsDTO dto) {
        service.batchApprove(dto.getIds());
        return Result.success();
    }

    @PutMapping("/batch/reject")
    public Result<Void> batchReject(@RequestBody BatchRejectDTO dto) {
        service.batchReject(dto.getIds(), dto.getRejectReason());
        return Result.success();
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }
}
