package com.buct.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("user_content")
public class UserContent {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("artifact_object_id")
    private String artifactObjectId;

    @TableField("content_type")
    private String contentType;

    @TableField("content_text")
    private String contentText;

    @TableField("file_url")
    private String fileUrl;

    @TableField("source")
    private String source;

    @TableField("audit_status")
    private Integer auditStatus;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("auditor_id")
    private Long auditorId;

    // getter
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getArtifactObjectId() { return artifactObjectId; }
    public String getContentType() { return contentType; }
    public String getContentText() { return contentText; }
    public String getFileUrl() { return fileUrl; }
    public String getSource() { return source; }
    public Integer getAuditStatus() { return auditStatus; }
    public String getRejectReason() { return rejectReason; }
    public LocalDateTime getSubmitTime() { return submitTime; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public Long getAuditorId() { return auditorId; }

    // setter
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setArtifactObjectId(String artifactObjectId) { this.artifactObjectId = artifactObjectId; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setSource(String source) { this.source = source; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
    public void setAuditorId(Long auditorId) { this.auditorId = auditorId; }
}