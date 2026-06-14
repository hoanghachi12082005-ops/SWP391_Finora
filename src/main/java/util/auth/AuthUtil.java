package com.storemanagement.util.auth;

import jakarta.servlet.http.HttpSession;

public final class AuthUtil {
    private AuthUtil() {}

    public static boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("currentUser") != null;
    }
}
