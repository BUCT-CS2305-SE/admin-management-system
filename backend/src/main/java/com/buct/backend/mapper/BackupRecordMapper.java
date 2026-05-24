package com.buct.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buct.backend.entity.BackupRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BackupRecordMapper extends BaseMapper<BackupRecord> {
}