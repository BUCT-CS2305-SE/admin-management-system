package com.buct.backend.dto;

import java.util.List;

public class BatchPlatformUserStatusDTO {

    private List<Long> ids;

    private Integer status;

    private Integer banComment;

    private Integer banUpload;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getBanComment() {
        return banComment;
    }

    public void setBanComment(Integer banComment) {
        this.banComment = banComment;
    }

    public Integer getBanUpload() {
        return banUpload;
    }

    public void setBanUpload(Integer banUpload) {
        this.banUpload = banUpload;
    }
}
