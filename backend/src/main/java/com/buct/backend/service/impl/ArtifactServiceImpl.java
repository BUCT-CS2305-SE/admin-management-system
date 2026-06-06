package com.buct.backend.service.impl;

import com.buct.backend.client.KgArtifactClient;
import com.buct.backend.common.BusinessException;
import com.buct.backend.common.PageResult;
import com.buct.backend.dto.ArtifactQueryDTO;
import com.buct.backend.dto.ArtifactSaveDTO;
import com.buct.backend.entity.Artifact;
import com.buct.backend.service.ArtifactService;
import com.buct.backend.service.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文物 Service：所有读写都委托给 KG 子系统的 REST API。
 */
@Service
public class ArtifactServiceImpl implements ArtifactService {

    private final KgArtifactClient kgClient;
    private final OperationLogService operationLogService;

    public ArtifactServiceImpl(KgArtifactClient kgClient,
                               OperationLogService operationLogService) {
        this.kgClient = kgClient;
        this.operationLogService = operationLogService;
    }

    @Override
    public PageResult<Artifact> pageArtifacts(ArtifactQueryDTO queryDTO) {
        long pageNum = normalizePageNum(queryDTO.getPageNum());
        long pageSize = normalizePageSize(queryDTO.getPageSize());

        Map<String, Object> resp = kgClient.listArtifacts(
                pageNum, pageSize, queryDTO.getType(), queryDTO.getMuseum());

        List<Map<String, Object>> data = KgArtifactClient.readDataList(resp);
        // 客户端侧过滤 title / objectId / period / material / location（KG 列表接口不支持这些条件）
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> raw : data) {
            Map<String, Object> item = KgArtifactClient.toAdminListItem(raw);
            if (!matchesLocalFilter(item, queryDTO)) {
                continue;
            }
            filtered.add(item);
        }

        List<Artifact> records = new ArrayList<>(filtered.size());
        for (Map<String, Object> item : filtered) {
            records.add(mapToArtifact(item));
        }

        long total = KgArtifactClient.readTotal(resp);
        return PageResult.of(records, total, pageNum, pageSize);
    }

    @Override
    public Artifact getArtifactById(String objectId) {
        if (!StringUtils.hasText(objectId)) {
            throw new BusinessException("文物 ID 不能为空");
        }
        Map<String, Object> resp = kgClient.getArtifactDetail(objectId);
        if (resp == null) {
            throw new BusinessException("文物不存在");
        }
        return mapToArtifact(KgArtifactClient.toAdminDetail(resp));
    }

    @Override
    public void addArtifact(ArtifactSaveDTO saveDTO) {
        Map<String, Object> camel = beanToMap(saveDTO);
        Map<String, Object> payload = KgArtifactClient.toKgWritePayload(saveDTO.getObjectId(), camel);
        kgClient.createArtifact(payload);
        operationLogService.record("文物数据管理", "新增文物", "artifact",
                saveDTO.getObjectId(), null, saveDTO.getObjectId());
    }

    @Override
    public void updateArtifact(String objectId, ArtifactSaveDTO saveDTO) {
        if (!StringUtils.hasText(objectId)) {
            throw new BusinessException("文物 ID 不能为空");
        }
        Map<String, Object> camel = beanToMap(saveDTO);
        Map<String, Object> payload = KgArtifactClient.toKgWritePayload(objectId, camel);
        // 写接口不需要 object_id 在 body 里也行，但带上无害
        kgClient.updateArtifact(objectId, payload);
        operationLogService.record("文物数据管理", "修改文物", "artifact",
                objectId, objectId, saveDTO.getObjectId());
    }

    @Override
    public void deleteArtifact(String objectId) {
        if (!StringUtils.hasText(objectId)) {
            throw new BusinessException("文物 ID 不能为空");
        }
        kgClient.deleteArtifact(objectId);
        operationLogService.record("文物数据管理", "删除文物", "artifact",
                objectId, objectId, null);
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
        operationLogService.record("文物数据管理", "批量导入文物", "artifact",
                "batch", null, "count=" + count);
        return count;
    }

    @Override
    public String exportArtifactsCsv(ArtifactQueryDTO queryDTO) {
        ArtifactQueryDTO q = new ArtifactQueryDTO();
        q.setPageNum(1L);
        q.setPageSize(100L);
        q.setTitle(queryDTO.getTitle());
        q.setObjectId(queryDTO.getObjectId());
        q.setPeriod(queryDTO.getPeriod());
        q.setType(queryDTO.getType());
        q.setMaterial(queryDTO.getMaterial());
        q.setMuseum(queryDTO.getMuseum());
        q.setLocation(queryDTO.getLocation());
        List<Artifact> artifacts = pageArtifacts(q).getRecords();

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
        operationLogService.record("文物数据管理", "导出文物CSV", "artifact",
                "export", null, "count=" + artifacts.size());
        return csv.toString();
    }

    // ---------- helpers ----------

    private boolean matchesLocalFilter(Map<String, Object> item, ArtifactQueryDTO q) {
        if (StringUtils.hasText(q.getTitle())
                && !contains(item.get("title"), q.getTitle())) {
            return false;
        }
        if (StringUtils.hasText(q.getObjectId())
                && !contains(item.get("objectId"), q.getObjectId())) {
            return false;
        }
        if (StringUtils.hasText(q.getPeriod())
                && !contains(item.get("period"), q.getPeriod())) {
            return false;
        }
        if (StringUtils.hasText(q.getMaterial())
                && !contains(item.get("material"), q.getMaterial())) {
            return false;
        }
        if (StringUtils.hasText(q.getLocation())
                && !contains(item.get("location"), q.getLocation())) {
            return false;
        }
        return true;
    }

    private boolean contains(Object value, String keyword) {
        if (value == null) return false;
        return String.valueOf(value).toLowerCase().contains(keyword.toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private Artifact mapToArtifact(Map<String, Object> item) {
        Artifact a = new Artifact();
        a.setId(asString(item.get("id")));
        a.setObjectId(asString(item.get("objectId")));
        a.setTitle(asString(item.get("title")));
        a.setPeriod(asString(item.get("period")));
        a.setType(asString(item.get("type")));
        a.setMaterial(asString(item.get("material")));
        a.setDescription(asString(item.get("description")));
        a.setDimensions(asString(item.get("dimensions")));
        a.setMuseum(asString(item.get("museum")));
        a.setLocation(asString(item.get("location")));
        a.setDetailUrl(asString(item.get("detailUrl")));
        a.setImageUrl(asString(item.get("imageUrl")));
        a.setImagePath(asString(item.get("imagePath")));
        a.setCreditLine(asString(item.get("creditLine")));
        a.setAccessionNumber(asString(item.get("accessionNumber")));
        Object crawl = item.get("crawlDate");
        if (crawl instanceof String s && !s.isBlank()) {
            try {
                a.setCrawlDate(LocalDate.parse(s.length() >= 10 ? s.substring(0, 10) : s));
            } catch (Exception ignored) {
            }
        }
        Object audit = item.get("auditStatus");
        if (audit instanceof Number n) a.setAuditStatus(n.intValue());
        Object sync = item.get("kgSyncStatus");
        if (sync instanceof Number n) a.setKgSyncStatus(n.intValue());
        return a;
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    /** Bean → Map（camelCase）。仅用于把 DTO 里非空字段透传给 KG client。 */
    private static Map<String, Object> beanToMap(Object bean) {
        Map<String, Object> map = new HashMap<>();
        try {
            for (PropertyDescriptor pd : java.beans.Introspector
                    .getBeanInfo(bean.getClass(), Object.class)
                    .getPropertyDescriptors()) {
                Method reader = pd.getReadMethod();
                if (reader == null) continue;
                Object value = reader.invoke(bean);
                if (value != null) {
                    map.put(pd.getName(), value);
                }
            }
        } catch (Exception e) {
            throw new BusinessException("参数解析失败：" + e.getMessage());
        }
        return map;
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
