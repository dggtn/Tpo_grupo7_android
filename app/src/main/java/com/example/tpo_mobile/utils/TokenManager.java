package com.example.tpo_mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String TOKEN_KEY = "access_token";
    private static final String EMAIL_KEY = "user_email";

    private final SharedPreferences sharedPreferences;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sharedPreferences.edit()
                .putString(TOKEN_KEY, token)
                .apply();
    }

    public String getToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public void saveUserEmail(String email) {
        sharedPreferences.edit()
                .putString(EMAIL_KEY, email)
                .apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(EMAIL_KEY, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clearToken() {
        sharedPreferences.edit()
                .remove(TOKEN_KEY)
                .remove(EMAIL_KEY)
                .apply();
    }
}