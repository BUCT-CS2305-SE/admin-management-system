package com.buct.backend.service;

import com.buct.backend.common.PageResult;
import com.buct.backend.dto.LogQueryDTO;
import com.buct.backend.entity.OperationLog;

public interface OperationLogService {

    PageResult<OperationLog> getOperationLogs(LogQueryDTO queryDTO);

    void record(String moduleName, String operationType, String targetType, String targetId, String beforeData, String afterData);
}
