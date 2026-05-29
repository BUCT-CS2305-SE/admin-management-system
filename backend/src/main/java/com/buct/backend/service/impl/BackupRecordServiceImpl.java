package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buct.backend.common.PageResult;
import com.buct.backend.dto.BackupQueryDTO;
import com.buct.backend.dto.BackupSaveDTO;
import com.buct.backend.entity.BackupRecord;
import com.buct.backend.mapper.BackupRecordMapper;
import com.buct.backend.service.BackupRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BackupRecordServiceImpl implements BackupRecordService {

    private final BackupRecordMapper backupRecordMapper;

    public BackupRecordServiceImpl(BackupRecordMapper backupRecordMapper) {
        this.backupRecordMapper = backupRecordMapper;
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
        backupRecord.setStatus(1);
        backupRecord.setCreateTime(LocalDateTime.now());

        backupRecordMapper.insert(backupRecord);
        return backupRecord;
    }
}