package com.buct.backend.common;

import java.util.List;

public class AuthUser {

    private Long userId;
    private String username;
    private String userType;
    private Long roleId;
    private List<String> permissionCodes;

    public AuthUser() {
    }

    public AuthUser(Long userId, String username, String userType, Long roleId) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }
}
