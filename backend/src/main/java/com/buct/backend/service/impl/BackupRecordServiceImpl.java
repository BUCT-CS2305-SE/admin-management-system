package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buct.backend.common.AuthContext;
import com.buct.backend.common.BusinessException;
import com.buct.backend.common.PageResult;
import com.buct.backend.dto.BackupQueryDTO;
import com.buct.backend.dto.BackupSaveDTO;
import com.buct.backend.entity.BackupRecord;
import com.buct.backend.mapper.BackupRecordMapper;
import com.buct.backend.service.OperationLogService;
import com.buct.backend.service.BackupRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BackupRecordServiceImpl implements BackupRecordService {

    private final BackupRecordMapper backupRecordMapper;
    private final OperationLogService operationLogService;

    public BackupRecordServiceImpl(BackupRecordMapper backupRecordMapper, OperationLogService operationLogService) {
        this.backupRecordMapper = backupRecordMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public PageResult<BackupRecord> getBackups(BackupQueryDTO queryDTO) {
        Page<BackupRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<BackupRecord> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getBackupType() != null && !queryDTO.getBackupType().isEmpty()) {
            wrapper.eq(BackupRecord::getBackupType, queryDTO.getBackupType());
        }
        wrapper.orderByDesc(BackupRecord::getCreateTime);

        Page<BackupRecord> resultPage = backupRecordMapper.selectPage(page, wrapper);

        return PageResult.of(
                resultPage.getRecords(),
                resultPage.getTotal(),
                resultPage.getCurrent(),
                resultPage.getSize()
        );
    }

    @Override
    public BackupRecord createBackup(BackupSaveDTO saveDTO) {
        BackupRecord backupRecord = new BackupRecord();
        backupRecord.setBackupName(saveDTO.getBackupName());
        backupRecord.setBackupType(saveDTO.getBackupType() != null ? saveDTO.getBackupType() : "FULL");
        backupRecord.setFilePath("runtime-temp/backups/" + backupRecord.getBackupName() + ".sql");
        backupRecord.setFileSize("演示记录");
        backupRecord.setOperatorId(AuthContext.getCurrentUserId());
        backupRecord.setStatus(1);
        backupRecord.setCreateTime(LocalDateTime.now());

        backupRecordMapper.insert(backupRecord);
        operationLogService.record("数据备份", "创建备份", "backup_record", String.valueOf(backupRecord.getId()), null, backupRecord.getBackupName());
        return backupRecord;
    }

    @Override
    public BackupRecord getBackup(Long id) {
        BackupRecord backupRecord = backupRecordMapper.selectById(id);
        if (backupRecord == null) {
            throw new BusinessException("备份记录不存在");
        }
        return backupRecord;
    }

    @Override
    public BackupRecord restoreBackup(Long id) {
        BackupRecord backupRecord = getBackup(id);
        operationLogService.record("数据备份", "恢复备份", "backup_record", String.valueOf(id), null, backupRecord.getBackupName());
        return backupRecord;
    }
}
