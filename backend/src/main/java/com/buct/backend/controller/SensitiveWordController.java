package com.buct.backend.controller;

import com.buct.backend.dto.SensitiveWordQueryDTO;
import com.buct.backend.dto.SensitiveWordSaveDTO;
import com.buct.backend.entity.SensitiveWord;
import com.buct.backend.common.PageResult;
import com.buct.backend.common.Result;
import com.buct.backend.service.SensitiveWordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sensitive-words")
public class SensitiveWordController {

    private final SensitiveWordService service;

    // 手动构造器
    public SensitiveWordController(SensitiveWordService service) {
        this.service = service;
    }

    @GetMapping("/page")
    public Result<PageResult<SensitiveWord>> page(SensitiveWordQueryDTO dto) {
        return Result.success(service.pageList(dto));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SensitiveWordSaveDTO dto) {
        service.save(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SensitiveWordSaveDTO dto) {
        service.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }
}