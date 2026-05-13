package com.buct.backend.dto;

public class ContentQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String contentType;
    private String source;
    private Integer auditStatus;
    private Long userId;

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getAuditStatus() { return auditStatus; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}