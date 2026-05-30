package com.buct.backend.common;

public class AuthContext {

    private static final ThreadLocal<AuthUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        CURRENT_USER.set(user);
    }

    public static AuthUser get() {
        return CURRENT_USER.get();
    }

    public static Long getCurrentUserId() {
        AuthUser user = get();
        return user == null ? null : user.getUserId();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
