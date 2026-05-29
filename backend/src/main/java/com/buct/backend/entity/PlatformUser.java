package com.buct.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("platform_user")
public class PlatformUser {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private String source;
    private Integer status;
    private Integer banComment;
    private Integer banUpload;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
