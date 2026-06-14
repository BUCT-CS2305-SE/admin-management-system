package com.buct.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buct.backend.common.PageResult;
import com.buct.backend.common.Result;
import com.buct.backend.dto.LogQueryDTO;
import com.buct.backend.entity.LoginLog;
import com.buct.backend.entity.OperationLog;
import com.buct.backend.mapper.LoginLogMapper;
import com.buct.backend.service.OperationLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/logs")
public class LogController {

    private final OperationLogService operationLogService;
    private final LoginLogMapper loginLogMapper;

    public LogController(OperationLogService operationLogService, LoginLogMapper loginLogMapper) {
        this.operationLogService = operationLogService;
        this.loginLogMapper = loginLogMapper;
    }

    @GetMapping("/operations/page")
    public Result<PageResult<OperationLog>> getOperationLogs(
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {

        LogQueryDTO queryDTO = new LogQueryDTO();
        queryDTO.setOperator(operator);
        queryDTO.setModuleName(moduleName);
        queryDTO.setOperationType(operationType);
        queryDTO.setKeyword(keyword);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        PageResult<OperationLog> result = operationLogService.getOperationLogs(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/login/page")
    public Result<PageResult<LoginLog>> getLoginLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer loginStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {

        Page<LoginLog> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(LoginLog::getUsername, username);
        }
        if (loginStatus != null) {
            wrapper.eq(LoginLog::getLoginStatus, loginStatus);
        }
        if (startTime != null) {
            wrapper.ge(LoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(LoginLog::getLoginTime, endTime);
        }
        wrapper.orderByDesc(LoginLog::getLoginTime);

        Page<LoginLog> resultPage = loginLogMapper.selectPage(page, wrapper);

        PageResult<LoginLog> result = PageResult.of(
                resultPage.getRecords(),
                resultPage.getTotal(),
                resultPage.getCurrent(),
                resultPage.getSize()
        );

        return Result.success(result);
    }
}
