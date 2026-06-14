package com.buct.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buct.backend.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}