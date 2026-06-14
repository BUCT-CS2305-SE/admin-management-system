package com.buct.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buct.backend.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}