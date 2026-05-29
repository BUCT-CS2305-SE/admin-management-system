package com.buct.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buct.backend.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    List<OperationLog> selectByConditions(
            @Param("adminUsername") String adminUsername,
            @Param("operationType") String operationType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    Long countByConditions(
            @Param("adminUsername") String adminUsername,
            @Param("operationType") String operationType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}