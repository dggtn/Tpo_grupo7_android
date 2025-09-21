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
    private final BiometricAuthService biometricAuthService;
    private final Context context;

    @Inject
    public LogoutService(AuthRepository authRepository,
                         TokenManager tokenManager,
                         BiometricAuthService biometricAuthService,
                         @ApplicationContext Context context) {
        this.authRepository = authRepository;
        this.tokenManager = tokenManager;
        this.biometricAuthService = biometricAuthService;
        this.context = context;
    }

    /**
     * Realiza el logout completo: notifica al servidor y limpia datos locales
     */
    public void performLogout(LogoutCallback callback) {
        performLogout(callback, true);
    }

    /**
     * Realiza el logout con opción de mantener configuración biométrica
     * @param callback Callback para resultado
     * @param clearBiometric true para limpiar config biométrica, false para mantenerla
     */
    public void performLogout(LogoutCallback callback, boolean clearBiometric) {
        Log.d(TAG, "Iniciando logout completo (clearBiometric: " + clearBiometric + ")");

        authRepository.logout(new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Logout exitoso en servidor: " + result);

                // Limpiar configuración biométrica si se solicita
                if (clearBiometric) {
                    biometricAuthService.clearBiometricConfiguration();
                    Log.d(TAG, "Configuración biométrica limpiada");
                }

                callback.onLogoutSuccess(result);
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Error en logout del servidor: " + error);

                // Aún así limpiar datos locales
                if (clearBiometric) {
                    biometricAuthService.clearBiometricConfiguration();
                    Log.d(TAG, "Configuración biométrica limpiada tras error");
                }

                callback.onLogoutError(error);
            }
        });
    }

    /**
     * Logout rápido: solo limpia datos locales y navega
     */
    public void logoutLocalAndNavigateToLogin() {
        logoutLocalAndNavigateToLogin(true);
    }

    /**
     * Logout local con opción de mantener configuración biométrica
     * @param clearBiometric true para limpiar config biométrica, false para mantenerla
     */
    public void logoutLocalAndNavigateToLogin(boolean clearBiometric) {
        Log.d(TAG, "Realizando logout local y navegación (clearBiometric: " + clearBiometric + ")");

        // Limpiar datos locales
        authRepository.logoutLocal();

        if (clearBiometric) {
            biometricAuthService.clearBiometricConfiguration();
            Log.d(TAG, "Configuración biométrica limpiada en logout local");
        }

        // Navegar al login
        navigateToLogin();
    }

    /**
     * Solo logout local sin navegación
     */
    public void logoutLocalOnly() {
        logoutLocalOnly(true);
    }

    /**
     * Logout local sin navegación con opción de mantener configuración biométrica
     * @param clearBiometric true para limpiar config biométrica, false para mantenerla
     */
    public void logoutLocalOnly(boolean clearBiometric) {
        Log.d(TAG, "Realizando solo logout local (clearBiometric: " + clearBiometric + ")");
        authRepository.logoutLocal();

        if (clearBiometric) {
            biometricAuthService.clearBiometricConfiguration();
            Log.d(TAG, "Configuración biométrica limpiada en logout local only");
        }
    }

    /**
     * Logout específico para cambio de usuario - mantiene capacidad de configurar biometría para otro usuario
     */
    public void logoutForUserSwitch(LogoutCallback callback) {
        Log.d(TAG, "Logout para cambio de usuario - manteniendo estructura biométrica");
        performLogout(callback, false); // No limpiar configuración biométrica completamente
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

        // En caso de expiración, mantener configuración biométrica para re-login rápido
        logoutLocalOnly(false);

        // Notificar
        if (callback != null) {
            callback.onLogoutSuccess("Sesión expirada");
        }

        // Navegar al login
        navigateToLogin();
    }

    /**
     * Obtiene información sobre el estado de la configuración biométrica
     */
    public String getBiometricConfigInfo() {
        if (!biometricAuthService.isDeviceBiometricCapable()) {
            return "Dispositivo sin soporte biométrico";
        }

        if (biometricAuthService.isBiometricAuthEnabled()) {
            String email = biometricAuthService.getBiometricUserEmail();
            return "Biometría activa para: " + (email != null ? email : "usuario actual");
        }

        return "Biometría disponible pero no configurada";
    }

    /**
     * Verifica si se debe mostrar opción de login biométrico después del logout
     */
    public boolean shouldShowBiometricLoginAfterLogout() {
        return biometricAuthService.isDeviceBiometricCapable() &&
                biometricAuthService.hasBiometricConfiguration();
    }
}