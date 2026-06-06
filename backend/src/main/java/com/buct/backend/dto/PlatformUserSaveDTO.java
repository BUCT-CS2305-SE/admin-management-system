package com.buct.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class PlatformUserSaveDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 可选；新增时若为空则不设置密码（用户后续无法登录），后台可重置。 */
    private String password;

    /** 可选；不传时使用默认普通用户角色（NORMAL_USER）。 */
    private Long roleId;

    private String phone;

    private String email;

    private String avatar;

    private String source;

    private Integer status;

    private Integer banComment;

    private Integer banUpload;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
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
}
