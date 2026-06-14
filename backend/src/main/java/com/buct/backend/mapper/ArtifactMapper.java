package com.buct.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buct.backend.entity.Artifact;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArtifactMapper extends BaseMapper<Artifact> {
}