package com.buct.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ArtifactSaveDTO {

    @NotBlank(message = "文物唯一标识符不能为空")
    private String objectId;

    @NotBlank(message = "文物名称不能为空")
    private String title;

    @NotBlank(message = "年代/时期不能为空")
    private String period;

    @NotBlank(message = "文物类型不能为空")
    private String type;

    private String material;

    @NotBlank(message = "文物介绍不能为空")
    private String description;

    private String dimensions;

    @NotBlank(message = "所属博物馆不能为空")
    private String museum;

    @NotBlank(message = "博物馆所在地不能为空")
    private String location;

    @NotBlank(message = "文物详情页URL不能为空")
    private String detailUrl;

    @NotBlank(message = "图片原始下载链接不能为空")
    private String imageUrl;

    @NotBlank(message = "本地图片存储路径不能为空")
    private String imagePath;

    private String creditLine;
    private String accessionNumber;

    @NotNull(message = "爬取日期不能为空")
    private LocalDate crawlDate;

    private Integer auditStatus;
    private Integer kgSyncStatus;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getMuseum() {
        return museum;
    }

    public void setMuseum(String museum) {
        this.museum = museum;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCreditLine() {
        return creditLine;
    }

    public void setCreditLine(String creditLine) {
        this.creditLine = creditLine;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public LocalDate getCrawlDate() {
        return crawlDate;
    }

    public void setCrawlDate(LocalDate crawlDate) {
        this.crawlDate = crawlDate;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Integer getKgSyncStatus() {
        return kgSyncStatus;
    }

    public void setKgSyncStatus(Integer kgSyncStatus) {
        this.kgSyncStatus = kgSyncStatus;
    }
}
