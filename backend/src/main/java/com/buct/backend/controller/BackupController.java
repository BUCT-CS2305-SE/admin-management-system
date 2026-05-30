package com.buct.backend.controller;

import com.buct.backend.common.PageResult;
import com.buct.backend.common.Result;
import com.buct.backend.dto.BackupQueryDTO;
import com.buct.backend.dto.BackupSaveDTO;
import com.buct.backend.entity.BackupRecord;
import com.buct.backend.service.BackupRecordService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

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

    @PostMapping("/{id}/restore")
    public Result<BackupRecord> restoreBackup(@PathVariable Long id) {
        return Result.success(backupRecordService.restoreBackup(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable Long id) {
        BackupRecord backupRecord = backupRecordService.getBackup(id);
        String content = "-- 演示备份文件\n-- backup: " + backupRecord.getBackupName() + "\n-- type: " + backupRecord.getBackupType() + "\n";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup-" + id + ".sql")
                .contentType(new MediaType("application", "sql", StandardCharsets.UTF_8))
                .body(content.getBytes(StandardCharsets.UTF_8));
    }
}
