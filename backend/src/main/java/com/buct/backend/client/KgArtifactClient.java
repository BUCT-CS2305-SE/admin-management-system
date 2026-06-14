package com.buct.backend.client;

import com.buct.backend.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用知识图谱子系统（KG）暴露的文物 REST API。
 * 自动透传当前管理员的 Authorization 头（共享 JWT 密钥）。
 */
@Component
public class KgArtifactClient {

    private static final Logger log = LoggerFactory.getLogger(KgArtifactClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public KgArtifactClient(@Value("${kg.base-url}") String baseUrl,
                            @Value("${kg.connect-timeout-ms:5000}") int connectTimeout,
                            @Value("${kg.read-timeout-ms:15000}") int readTimeout) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restClient = RestClient.builder()
                .baseUrl(this.baseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** 列表查询：?page=&page_size=&type=&museum=&lang=zh */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listArtifacts(long page, long pageSize,
                                             String type, String museum) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/artifacts")
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .queryParamIfPresent("type", optional(type))
                .queryParamIfPresent("museum", optional(museum))
                .queryParam("lang", "zh")
                .build(true)
                .toUri();
        return invoke(() -> restClient.get()
                .uri(uri)
                .headers(this::injectAuth)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    throw new BusinessException("KG 列表查询失败: HTTP " + resp.getStatusCode().value());
                })
                .body(Map.class), "list artifacts");
    }

    /** 详情查询：/api/artifacts/{object_id} */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getArtifactDetail(String objectId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/artifacts/{id}")
                .queryParam("lang", "zh")
                .buildAndExpand(objectId)
                .toUri();
        return invoke(() -> restClient.get()
                .uri(uri)
                .headers(this::injectAuth)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    if (resp.getStatusCode().value() == 404) {
                        throw new BusinessException("文物不存在");
                    }
                    throw new BusinessException("KG 详情查询失败: HTTP " + resp.getStatusCode().value());
                })
                .body(Map.class), "get artifact " + objectId);
    }

    /** 新增：POST /api/admin/artifacts */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createArtifact(Map<String, Object> payload) {
        return invoke(() -> restClient.post()
                .uri("/api/admin/artifacts")
                .headers(this::injectAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    throw new BusinessException("KG 新增失败: HTTP " + resp.getStatusCode().value());
                })
                .body(Map.class), "create artifact");
    }

    /** 编辑：PUT /api/admin/artifacts/{object_id} */
    @SuppressWarnings("unchecked")
    public Map<String, Object> updateArtifact(String objectId, Map<String, Object> payload) {
        return invoke(() -> restClient.put()
                .uri("/api/admin/artifacts/{id}", objectId)
                .headers(this::injectAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    if (resp.getStatusCode().value() == 404) {
                        throw new BusinessException("文物不存在，无法修改");
                    }
                    throw new BusinessException("KG 编辑失败: HTTP " + resp.getStatusCode().value());
                })
                .body(Map.class), "update artifact " + objectId);
    }

    /** 删除：DELETE /api/admin/artifacts/{object_id} */
    @SuppressWarnings("unchecked")
    public Map<String, Object> deleteArtifact(String objectId) {
        return invoke(() -> restClient.delete()
                .uri("/api/admin/artifacts/{id}", objectId)
                .headers(this::injectAuth)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    if (resp.getStatusCode().value() == 404) {
                        throw new BusinessException("文物不存在，无法删除");
                    }
                    throw new BusinessException("KG 删除失败: HTTP " + resp.getStatusCode().value());
                })
                .body(Map.class), "delete artifact " + objectId);
    }

    // ---------- helpers ----------

    private void injectAuth(HttpHeaders headers) {
        String token = currentAuthorization();
        if (token != null) {
            headers.set(HttpHeaders.AUTHORIZATION, token);
        }
    }

    private String currentAuthorization() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && !auth.isBlank()) {
                return auth;
            }
            String xToken = req.getHeader("X-Token");
            if (xToken != null && !xToken.isBlank()) {
                return xToken.startsWith("Bearer ") ? xToken : ("Bearer " + xToken);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static java.util.Optional<String> optional(String value) {
        return (value == null || value.isBlank())
                ? java.util.Optional.empty()
                : java.util.Optional.of(value);
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private <T> T invoke(java.util.function.Supplier<T> action, String desc) {
        try {
            return action.get();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("KG call failed: {}", desc, e);
            throw new BusinessException("调用知识图谱子系统失败：" + e.getMessage());
        }
    }

    /** 把 KG 列表返回的 data 项标准化成 admin 实体 Map（key 用 admin camelCase）。*/
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toAdminListItem(Map<String, Object> kgItem) {
        Map<String, Object> m = new HashMap<>();
        Object id = kgItem.get("id");
        m.put("id", id);
        m.put("objectId", id);
        m.put("title", kgItem.getOrDefault("name", ""));
        m.put("period", kgItem.getOrDefault("period", ""));
        m.put("type", kgItem.getOrDefault("type", ""));
        m.put("material", kgItem.getOrDefault("material", ""));
        m.put("imageUrl", kgItem.getOrDefault("thumbnail_url", ""));
        Object museumObj = kgItem.get("museum");
        if (museumObj instanceof Map<?, ?> mm) {
            Map<String, Object> museumMap = (Map<String, Object>) mm;
            m.put("museum", museumMap.getOrDefault("name", ""));
            m.put("location", museumMap.getOrDefault("location", ""));
        } else {
            m.put("museum", "");
            m.put("location", "");
        }
        m.put("auditStatus", 1);
        m.put("kgSyncStatus", 1);
        return m;
    }

    /** 把 KG 详情返回标准化成 admin 实体 Map。*/
    public static Map<String, Object> toAdminDetail(Map<String, Object> kgDetail) {
        Map<String, Object> m = new HashMap<>();
        Object id = kgDetail.get("id");
        m.put("id", id);
        m.put("objectId", id);
        m.put("title", kgDetail.getOrDefault("name", ""));
        m.put("period", kgDetail.getOrDefault("period", ""));
        m.put("type", kgDetail.getOrDefault("type", ""));
        m.put("material", kgDetail.getOrDefault("material", ""));
        m.put("description", kgDetail.getOrDefault("description", ""));
        m.put("dimensions", kgDetail.getOrDefault("dimensions", ""));
        m.put("museum", kgDetail.getOrDefault("museum", ""));
        m.put("location", kgDetail.getOrDefault("location", ""));
        m.put("detailUrl", kgDetail.getOrDefault("detail_url", ""));
        m.put("imageUrl", kgDetail.getOrDefault("image_url", ""));
        m.put("imagePath", kgDetail.getOrDefault("image_path", ""));
        m.put("creditLine", kgDetail.getOrDefault("credit_line", ""));
        m.put("accessionNumber", kgDetail.getOrDefault("accession_number", ""));
        m.put("crawlDate", kgDetail.getOrDefault("crawl_date", ""));
        m.put("auditStatus", 1);
        m.put("kgSyncStatus", 1);
        return m;
    }

    /** 把 admin 的 ArtifactSaveDTO map 转成 KG 写接口要求的 snake_case payload。*/
    public static Map<String, Object> toKgWritePayload(String objectId, Map<String, Object> camel) {
        Map<String, Object> p = new HashMap<>();
        p.put("object_id", objectId);
        copyIfPresent(camel, "title", p, "title");
        copyIfPresent(camel, "period", p, "period");
        copyIfPresent(camel, "type", p, "type");
        copyIfPresent(camel, "material", p, "material");
        copyIfPresent(camel, "description", p, "description");
        copyIfPresent(camel, "dimensions", p, "dimensions");
        copyIfPresent(camel, "museum", p, "museum");
        copyIfPresent(camel, "location", p, "location");
        copyIfPresent(camel, "detailUrl", p, "detail_url");
        copyIfPresent(camel, "imageUrl", p, "image_url");
        copyIfPresent(camel, "imagePath", p, "image_path");
        copyIfPresent(camel, "creditLine", p, "credit_line");
        copyIfPresent(camel, "accessionNumber", p, "accession_number");
        return p;
    }

    private static void copyIfPresent(Map<String, Object> src, String srcKey,
                                      Map<String, Object> dst, String dstKey) {
        Object v = src.get(srcKey);
        if (v != null) dst.put(dstKey, v);
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> readDataList(Map<String, Object> resp) {
        Object data = resp == null ? null : resp.get("data");
        if (data instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    public static long readTotal(Map<String, Object> resp) {
        Object t = resp == null ? null : resp.get("total");
        if (t instanceof Number n) return n.longValue();
        return 0L;
    }
}
