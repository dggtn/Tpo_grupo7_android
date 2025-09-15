package com.example.tpo_mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREFS_NAME = "auth_prefs";
    private static final String TOKEN_KEY = "access_token";
    private static final String EMAIL_KEY = "user_email";
    private static final String LOGIN_TIME_KEY = "login_time";
    private static final String USER_NAME_KEY = "user_name";

    private final SharedPreferences sharedPreferences;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sharedPreferences.edit()
                .putString(TOKEN_KEY, token)
                .putLong(LOGIN_TIME_KEY, System.currentTimeMillis())
                .apply();
        Log.d(TAG, "Token guardado exitosamente");
    }

    public String getToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public void saveUserEmail(String email) {
        sharedPreferences.edit()
                .putString(EMAIL_KEY, email)
                .apply();
        Log.d(TAG, "Email de usuario guardado: " + email);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(EMAIL_KEY, null);
    }

    public void saveUserName(String name) {
        sharedPreferences.edit()
                .putString(USER_NAME_KEY, name)
                .apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(USER_NAME_KEY, null);
    }

    public long getLoginTime() {
        return sharedPreferences.getLong(LOGIN_TIME_KEY, 0);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        boolean hasToken = token != null && !token.trim().isEmpty();

        if (hasToken) {
            Log.d(TAG, "Usuario logueado - Token presente");
        } else {
            Log.d(TAG, "Usuario no logueado - No hay token");
        }

        return hasToken;
    }

    public void clearToken() {
        String email = getUserEmail(); // Guardar para log

        sharedPreferences.edit()
                .remove(TOKEN_KEY)
                .remove(EMAIL_KEY)
                .remove(LOGIN_TIME_KEY)
                .remove(USER_NAME_KEY)
                .apply();

        Log.d(TAG, "Sesión cerrada para usuario: " + (email != null ? email : "desconocido"));
    }

    // Método para verificar si la sesión ha expirado (opcional)
    public boolean isSessionExpired(long maxSessionTimeMillis) {
        if (!isLoggedIn()) {
            return true;
        }

        long loginTime = getLoginTime();
        if (loginTime == 0) {
            return false; // No se puede determinar, asumir que no ha expirado
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - loginTime) > maxSessionTimeMillis;
    }

    // Método para obtener información básica del usuario logueado
    public String getUserInfo() {
        if (!isLoggedIn()) {
            return "Usuario no logueado";
        }

        String email = getUserEmail();
        String name = getUserName();
        long loginTime = getLoginTime();

        StringBuilder info = new StringBuilder();
        if (name != null) {
            info.append("Usuario: ").append(name);
        } else if (email != null) {
            info.append("Email: ").append(email);
        } else {
            info.append("Usuario autenticado");
        }

        if (loginTime > 0) {
            long sessionDuration = System.currentTimeMillis() - loginTime;
            long minutes = sessionDuration / (1000 * 60);
            info.append(" (sesión: ").append(minutes).append(" min)");
        }

        return info.toString();
    }
}