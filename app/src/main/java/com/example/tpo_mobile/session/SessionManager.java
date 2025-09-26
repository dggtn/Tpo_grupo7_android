package com.example.tpo_mobile.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "session";
    private static final String KEY_TOKEN = "jwt";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        this.sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveToken(String jwt) { sp.edit().putString(KEY_TOKEN, jwt).apply(); }
    public String getToken() { return sp.getString(KEY_TOKEN, null); }
    public void clearToken() { sp.edit().remove(KEY_TOKEN).apply(); }

    public void saveUserId(Long id) {
        if (id == null) sp.edit().remove(KEY_USER_ID).apply();
        else sp.edit().putLong(KEY_USER_ID, id).apply();
    }
    public Long getUserId() {
        if (!sp.contains(KEY_USER_ID)) return null;
        long v = sp.getLong(KEY_USER_ID, -1L);
        return v == -1L ? null : v;
    }
}
