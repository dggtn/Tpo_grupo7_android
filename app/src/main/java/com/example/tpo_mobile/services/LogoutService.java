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
     * Realiza el logout completo: notifica al servidor y limpia datos locales
     */
    public void performLogout(LogoutCallback callback) {
        Log.d(TAG, "Iniciando logout completo");

        authRepository.logout(new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Logout exitoso en servidor: " + result);
                callback.onLogoutSuccess(result);
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Error en logout del servidor: " + error);
                // Aún así considerar como exitoso porque se limpió localmente
                callback.onLogoutError(error);
            }
        });
    }

    /**
     * Logout rápido: solo limpia datos locales y navega
     */
    public void logoutLocalAndNavigateToLogin() {
        Log.d(TAG, "Realizando logout local y navegación");

        // Limpiar datos locales
        authRepository.logoutLocal();

        // Navegar al login
        navigateToLogin();
    }

    /**
     * Solo logout local sin navegación
     */
    public void logoutLocalOnly() {
        Log.d(TAG, "Realizando solo logout local");
        authRepository.logoutLocal();
    }

    /**
     * Navega a la pantalla de login
     */
    public void navigateToLogin() {
        Log.d(TAG, "Navegando al login");

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Verifica si el usuario está logueado
     */
    public boolean isUserLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    /**
     * Obtiene información básica del usuario actual
     */
    public String getCurrentUserInfo() {
        return tokenManager.getUserInfo();
    }

    /**
     * Verifica si la sesión ha expirado
     * @param maxSessionHours máximo de horas permitidas para la sesión
     */
    public boolean isSessionExpired(int maxSessionHours) {
        long maxSessionMillis = maxSessionHours * 60 * 60 * 1000L;
        return tokenManager.isSessionExpired(maxSessionMillis);
    }

    /**
     * Logout automático por expiración de sesión
     */
    public void handleSessionExpired(LogoutCallback callback) {
        Log.w(TAG, "Sesión expirada, realizando logout automático");

        // Limpiar datos locales inmediatamente
        logoutLocalOnly();

        // Notificar
        if (callback != null) {
            callback.onLogoutSuccess("Sesión expirada");
        }

        // Navegar al login
        navigateToLogin();
    }
}