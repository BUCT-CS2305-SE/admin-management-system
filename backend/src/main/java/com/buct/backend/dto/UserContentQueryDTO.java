package com.buct.backend.dto;

public class UserContentQueryDTO {
    private Integer pageNum;
    private Integer pageSize;
    private Integer contentType;
    private String source;
    private Integer auditStatus;
    private Long userId;

    // 必须的 getter 方法
    public Integer getPageNum() {
        return pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getContentType() {
        return contentType;
    }

    public String getSource() {
        return source;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public Long getUserId() {
        return userId;
    }

    // 可选的 setter 方法
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}