package com.stanissudo.jycs_crafters.auth;

import android.content.Context;
import android.content.SharedPreferences;

public final class UserSessionManager {
    private static final String PREFS = "fueltrack_prefs";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final String KEY_USER_ID  = "user_id";

    private UserSessionManager() {}

    public static boolean isAdmin(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_IS_ADMIN, false);
    }

    public static int currentUserId(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getInt(KEY_USER_ID, -1);
    }

    public static void setSession(Context ctx, int userId, boolean isAdmin) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_USER_ID, userId)
                .putBoolean(KEY_IS_ADMIN, isAdmin)
                .apply();
    }

    public static void clearSession(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
