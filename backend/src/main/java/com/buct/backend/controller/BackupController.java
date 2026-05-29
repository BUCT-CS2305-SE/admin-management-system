package com.buct.backend.controller;

import com.buct.backend.common.PageResult;
import com.buct.backend.common.Result;
import com.buct.backend.dto.BackupQueryDTO;
import com.buct.backend.dto.BackupSaveDTO;
import com.buct.backend.entity.BackupRecord;
import com.buct.backend.service.BackupRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/backups")
public class BackupController {

    private final BackupRecordService backupRecordService;

    public BackupController(BackupRecordService backupRecordService) {
        this.backupRecordService = backupRecordService;
    }

    @GetMapping("/page")
    public Result<PageResult<BackupRecord>> getBackups(
            @RequestParam(required = false) String backupType,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {

        BackupQueryDTO queryDTO = new BackupQueryDTO();
        queryDTO.setBackupType(backupType);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        PageResult<BackupRecord> result = backupRecordService.getBackups(queryDTO);
        return Result.success(result);
    }

    @PostMapping
    public Result<BackupRecord> createBackup(@RequestBody BackupSaveDTO saveDTO) {
        BackupRecord backupRecord = backupRecordService.createBackup(saveDTO);
        return Result.success(backupRecord);
    }
}