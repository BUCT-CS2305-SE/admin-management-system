package com.buct.backend.service.impl;

import com.buct.backend.common.PageResult;
import com.buct.backend.common.AuthContext;
import com.buct.backend.common.AuthUser;
import com.buct.backend.dto.LogQueryDTO;
import com.buct.backend.entity.OperationLog;
import com.buct.backend.mapper.OperationLogMapper;
import com.buct.backend.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public PageResult<OperationLog> getOperationLogs(LogQueryDTO queryDTO) {
        Long offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        
        List<OperationLog> records = operationLogMapper.selectByConditions(
                queryDTO.getOperator(),
                queryDTO.getOperationType(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime(),
                offset,
                queryDTO.getPageSize()
        );

        Long total = operationLogMapper.countByConditions(
                queryDTO.getOperator(),
                queryDTO.getOperationType(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
        );

        return PageResult.of(records, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public void record(String moduleName, String operationType, String targetType, String targetId, String beforeData, String afterData) {
        AuthUser currentUser = AuthContext.get();

        OperationLog log = new OperationLog();
        if (currentUser != null) {
            log.setAdminId(currentUser.getUserId());
            log.setAdminUsername(currentUser.getUsername());
        }
        log.setModuleName(moduleName);
        log.setOperationType(operationType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeData(beforeData);
        log.setAfterData(afterData);
        log.setIpAddress("127.0.0.1");
        log.setOperationTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }
}
