package com.buct.backend.service.impl;

import com.buct.backend.common.PageResult;
import com.buct.backend.dto.LogQueryDTO;
import com.buct.backend.entity.OperationLog;
import com.buct.backend.mapper.OperationLogMapper;
import com.buct.backend.service.OperationLogService;
import org.springframework.stereotype.Service;

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
}