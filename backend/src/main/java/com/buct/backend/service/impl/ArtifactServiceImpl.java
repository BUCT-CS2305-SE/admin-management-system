package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buct.backend.common.BusinessException;
import com.buct.backend.common.PageResult;
import com.buct.backend.dto.ArtifactQueryDTO;
import com.buct.backend.dto.ArtifactSaveDTO;
import com.buct.backend.entity.Artifact;
import com.buct.backend.mapper.ArtifactMapper;
import com.buct.backend.service.ArtifactService;
import com.buct.backend.service.OperationLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ArtifactServiceImpl implements ArtifactService {

    private final ArtifactMapper artifactMapper;
    private final OperationLogService operationLogService;

    public ArtifactServiceImpl(ArtifactMapper artifactMapper, OperationLogService operationLogService) {
        this.artifactMapper = artifactMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public PageResult<Artifact> pageArtifacts(ArtifactQueryDTO queryDTO) {
        long pageNum = normalizePageNum(queryDTO.getPageNum());
        long pageSize = normalizePageSize(queryDTO.getPageSize());

        LambdaQueryWrapper<Artifact> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Artifact::getTitle, queryDTO.getTitle())
                .like(StringUtils.hasText(queryDTO.getObjectId()), Artifact::getObjectId, queryDTO.getObjectId())
                .like(StringUtils.hasText(queryDTO.getPeriod()), Artifact::getPeriod, queryDTO.getPeriod())
                .eq(StringUtils.hasText(queryDTO.getType()), Artifact::getType, queryDTO.getType())
                .like(StringUtils.hasText(queryDTO.getMaterial()), Artifact::getMaterial, queryDTO.getMaterial())
                .like(StringUtils.hasText(queryDTO.getMuseum()), Artifact::getMuseum, queryDTO.getMuseum())
                .like(StringUtils.hasText(queryDTO.getLocation()), Artifact::getLocation, queryDTO.getLocation())
                .eq(queryDTO.getAuditStatus() != null, Artifact::getAuditStatus, queryDTO.getAuditStatus())
                .eq(queryDTO.getKgSyncStatus() != null, Artifact::getKgSyncStatus, queryDTO.getKgSyncStatus())
                .orderByDesc(Artifact::getUpdateTime)
                .orderByDesc(Artifact::getId);

        IPage<Artifact> page = artifactMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Artifact getArtifactById(Long id) {
        Artifact artifact = artifactMapper.selectById(id);
        if (artifact == null) {
            throw new BusinessException("文物不存在");
        }
        return artifact;
    }

    @Override
    public void addArtifact(ArtifactSaveDTO saveDTO) {
        checkObjectIdUnique(saveDTO.getObjectId(), null);

        Artifact artifact = new Artifact();
        BeanUtils.copyProperties(saveDTO, artifact);
        fillDefaultStatus(artifact);
        artifactMapper.insert(artifact);
        operationLogService.record("文物数据管理", "新增文物", "artifact", String.valueOf(artifact.getId()), null, artifact.getObjectId());
    }

    @Override
    public void updateArtifact(Long id, ArtifactSaveDTO saveDTO) {
        Artifact existing = artifactMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("文物不存在，无法修改");
        }
        checkObjectIdUnique(saveDTO.getObjectId(), id);

        Artifact artifact = new Artifact();
        BeanUtils.copyProperties(saveDTO, artifact);
        artifact.setId(id);
        fillDefaultStatus(artifact);
        artifactMapper.updateById(artifact);
        operationLogService.record("文物数据管理", "修改文物", "artifact", String.valueOf(id), existing.getObjectId(), artifact.getObjectId());
    }

    @Override
    public void deleteArtifact(Long id) {
        Artifact existing = artifactMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("文物不存在，无法删除");
        }
        artifactMapper.deleteById(id);
        operationLogService.record("文物数据管理", "删除文物", "artifact", String.valueOf(id), existing.getObjectId(), null);
    }

    @Override
    public int importArtifacts(List<ArtifactSaveDTO> artifacts) {
        if (artifacts == null || artifacts.isEmpty()) {
            throw new BusinessException("导入数据不能为空");
        }
        int count = 0;
        for (ArtifactSaveDTO artifact : artifacts) {
            addArtifact(artifact);
            count++;
        }
        operationLogService.record("文物数据管理", "批量导入文物", "artifact", "batch", null, "count=" + count);
        return count;
    }

    @Override
    public String exportArtifactsCsv(ArtifactQueryDTO queryDTO) {
        queryDTO.setPageNum(1L);
        queryDTO.setPageSize(100L);
        List<Artifact> artifacts = pageArtifacts(queryDTO).getRecords();

        StringBuilder csv = new StringBuilder();
        csv.append("id,objectId,title,period,type,material,museum,location,auditStatus,kgSyncStatus,crawlDate\n");
        for (Artifact artifact : artifacts) {
            csv.append(value(artifact.getId())).append(',')
                    .append(value(artifact.getObjectId())).append(',')
                    .append(value(artifact.getTitle())).append(',')
                    .append(value(artifact.getPeriod())).append(',')
                    .append(value(artifact.getType())).append(',')
                    .append(value(artifact.getMaterial())).append(',')
                    .append(value(artifact.getMuseum())).append(',')
                    .append(value(artifact.getLocation())).append(',')
                    .append(value(artifact.getAuditStatus())).append(',')
                    .append(value(artifact.getKgSyncStatus())).append(',')
                    .append(value(artifact.getCrawlDate())).append('\n');
        }
        operationLogService.record("文物数据管理", "导出文物CSV", "artifact", "export", null, "count=" + artifacts.size());
        return csv.toString();
    }

    private void checkObjectIdUnique(String objectId, Long currentId) {
        LambdaQueryWrapper<Artifact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Artifact::getObjectId, objectId)
                .ne(currentId != null, Artifact::getId, currentId);
        Long count = artifactMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("文物唯一标识符已存在");
        }
    }

    private void fillDefaultStatus(Artifact artifact) {
        if (artifact.getAuditStatus() == null) {
            artifact.setAuditStatus(1);
        }
        if (artifact.getKgSyncStatus() == null) {
            artifact.setKgSyncStatus(0);
        }
    }

    private long normalizePageNum(Long pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private long normalizePageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private String value(Object raw) {
        String text = raw == null ? "" : String.valueOf(raw);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
