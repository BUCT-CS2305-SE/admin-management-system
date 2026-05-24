package com.buct.backend.service;

import com.buct.backend.common.PageResult;
import com.buct.backend.dto.BackupQueryDTO;
import com.buct.backend.dto.BackupSaveDTO;
import com.buct.backend.entity.BackupRecord;

public interface BackupRecordService {

    PageResult<BackupRecord> getBackups(BackupQueryDTO queryDTO);

    BackupRecord createBackup(BackupSaveDTO saveDTO);
}