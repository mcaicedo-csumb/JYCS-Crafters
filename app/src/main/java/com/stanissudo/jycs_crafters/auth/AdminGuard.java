package com.stanissudo.jycs_crafters.auth;

import android.content.Context;

public final class AdminGuard {
    private AdminGuard() {}
    public static void ensureAdmin(Context ctx) {
        if (!UserSessionManager.isAdmin(ctx)) {
            throw new SecurityException("Admin-only operation");
        }
    }
}
