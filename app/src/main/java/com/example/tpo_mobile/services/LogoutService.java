package com.example.tpo_mobile.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.tpo_mobile.activities.MainActivity;
import com.example.tpo_mobile.repository.AuthRepository;
import com.example.tpo_mobile.utils.TokenManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class LogoutService {

    private static final String TAG = "LogoutService";

    public interface LogoutCallback {
        void onLogoutSuccess(String message);
        void onLogoutError(String error);
    }

    private final AuthRepository authRepository;
    private final TokenManager tokenManager;
    private final Context context;

    @Inject
    public LogoutService(AuthRepository authRepository, TokenManager tokenManager, @ApplicationContext Context context) {
        this.authRepository = authRepository;
        this.tokenManager = tokenManager;
        this.context = context;
    }

    /**
     * Logout completo con comunicación al servidor
     */
    public void performLogout(LogoutCallback callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onLogoutError("No hay sesión activa");
            return;
        }

        String userInfo = tokenManager.getUserInfo();
        Log.d(TAG, "Iniciando logout para: " + userInfo);

        authRepository.logout(new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Logout exitoso: " + result);
                callback.onLogoutSuccess(result);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error en logout: " + error);
                // Aún así realizar logout local
                performLocalLogout();
                callback.onLogoutError(error);
            }
        });
    }

    /**
     * Logout local solamente (más rápido)
     */
    public void performLocalLogout() {
        String userInfo = tokenManager.getUserInfo();
        Log.d(TAG, "Realizando logout local para: " + userInfo);

        tokenManager.clearToken();
        Log.d(TAG, "Logout local completado");
    }

    /**
     * Logout y navegación automática al login
     */
    public void logoutAndNavigateToLogin(LogoutCallback callback) {
        performLogout(new LogoutCallback() {
            @Override
            public void onLogoutSuccess(String message) {
                navigateToLogin();
                if (callback != null) {
                    callback.onLogoutSuccess(message);
                }
            }

            @Override
            public void onLogoutError(String error) {
                // Aún así navegar al login porque se hizo logout local
                navigateToLogin();
                if (callback != null) {
                    callback.onLogoutError(error);
                }
            }
        });
    }

    /**
     * Logout local y navegación automática (más rápido)
     */
    public void logoutLocalAndNavigateToLogin() {
        performLocalLogout();
        navigateToLogin();
    }

    /**
     * Navegar a la pantalla de login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Log.d(TAG, "Navegación a login iniciada");
    }

    /**
     * Verificar si el usuario está logueado
     */
    public boolean isUserLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    /**
     * Obtener información del usuario actual
     */
    public String getCurrentUserInfo() {
        return tokenManager.getUserInfo();
    }
}
