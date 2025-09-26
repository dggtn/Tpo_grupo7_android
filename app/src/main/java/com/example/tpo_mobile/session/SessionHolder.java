package com.example.tpo_mobile.session;

import android.content.Context;

public final class SessionHolder {
    private static SessionManager INSTANCE;

    private SessionHolder() {}

    public static void init(Context appCtx) {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager(appCtx.getApplicationContext());
        }
    }

    public static SessionManager get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("SessionHolder no inicializado. Llamar init() en Application.");
        }
        return INSTANCE;
    }
}
