package com.example.tpo_mobile.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tpo_mobile.core.BiometricAuthenticator;
import com.example.tpo_mobile.utils.TokenManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class BiometricAuthService {
    private static final String TAG = "BiometricAuthService";
    private static final String PREFS_NAME = "biometric_prefs";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_LAST_BIOMETRIC_AUTH = "last_biometric_auth";
    private static final String KEY_USER_EMAIL_FOR_BIOMETRIC = "user_email_biometric";

    // Tiempo máximo para considerar válida una autenticación biométrica (30 minutos)
    private static final long BIOMETRIC_AUTH_VALIDITY_MS = 30 * 60 * 1000;

    private final Context context;
    private final TokenManager tokenManager;
    private final SharedPreferences preferences;

    public interface BiometricAuthCallback {
        void onBiometricAuthSuccess();
        void onBiometricAuthError(String error);
        void onBiometricNotAvailable(String reason);
        void onBiometricAuthFailed();
    }

    @Inject
    public BiometricAuthService(@ApplicationContext Context context, TokenManager tokenManager) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Habilita la autenticación biométrica para el usuario actual
     */
    public void enableBiometricAuth() {
        String currentUserEmail = tokenManager.getUserEmail();
        if (currentUserEmail != null) {
            preferences.edit()
                    .putBoolean(KEY_BIOMETRIC_ENABLED, true)
                    .putString(KEY_USER_EMAIL_FOR_BIOMETRIC, currentUserEmail)
                    .apply();
            Log.d(TAG, "Autenticación biométrica habilitada para: " + currentUserEmail);
        }
    }

    /**
     * Deshabilita la autenticación biométrica
     */
    public void disableBiometricAuth() {
        preferences.edit()
                .putBoolean(KEY_BIOMETRIC_ENABLED, false)
                .remove(KEY_USER_EMAIL_FOR_BIOMETRIC)
                .remove(KEY_LAST_BIOMETRIC_AUTH)
                .apply();
        Log.d(TAG, "Autenticación biométrica deshabilitada");
    }

    /**
     * Verifica si la autenticación biométrica está habilitada para el usuario actual
     */
    public boolean isBiometricAuthEnabled() {
        String currentUserEmail = tokenManager.getUserEmail();
        String storedEmail = preferences.getString(KEY_USER_EMAIL_FOR_BIOMETRIC, null);
        boolean enabled = preferences.getBoolean(KEY_BIOMETRIC_ENABLED, false);

        return enabled && currentUserEmail != null && currentUserEmail.equals(storedEmail);
    }

    /**
     * Verifica si hay una autenticación biométrica reciente válida
     */
    public boolean hasValidRecentBiometricAuth() {
        if (!isBiometricAuthEnabled()) {
            return false;
        }

        long lastAuth = preferences.getLong(KEY_LAST_BIOMETRIC_AUTH, 0);
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastAuth) < BIOMETRIC_AUTH_VALIDITY_MS;
    }

    /**
     * Registra una autenticación biométrica exitosa
     */
    private void recordBiometricAuth() {
        preferences.edit()
                .putLong(KEY_LAST_BIOMETRIC_AUTH, System.currentTimeMillis())
                .apply();
    }

    /**
     * Inicia el proceso de autenticación biométrica
     */
    public void authenticateWithBiometric(AppCompatActivity activity, BiometricAuthCallback callback) {
        authenticateWithBiometric(activity, callback, null);
    }

    /**
     * Inicia el proceso de autenticación biométrica con título personalizado
     */
    public void authenticateWithBiometric(AppCompatActivity activity, BiometricAuthCallback callback, String customTitle) {
        Log.d(TAG, "Iniciando autenticación biométrica");

        // Verificar si la biometría está disponible
        if (!BiometricAuthenticator.isBiometricAvailable(context)) {
            String statusMessage = BiometricAuthenticator.getBiometricStatusMessage(context);
            Log.w(TAG, "Biometría no disponible: " + statusMessage);
            callback.onBiometricNotAvailable(statusMessage);
            return;
        }

        BiometricAuthenticator.AuthenticationCallback authCallback = new BiometricAuthenticator.AuthenticationCallback() {
            @Override
            public void onAuthenticationSuccess() {
                Log.d(TAG, "Autenticación biométrica exitosa");
                recordBiometricAuth();
                callback.onBiometricAuthSuccess();
            }

            @Override
            public void onAuthenticationError(int errorCode, String errorMessage) {
                Log.e(TAG, "Error en autenticación biométrica: " + errorMessage + " (Code: " + errorCode + ")");
                callback.onBiometricAuthError(errorMessage);
            }

            @Override
            public void onAuthenticationFailed() {
                Log.w(TAG, "Falló la autenticación biométrica - biométrica no reconocida");
                callback.onBiometricAuthFailed();
            }

            @Override
            public void onBiometricNotAvailable(String reason) {
                Log.w(TAG, "Biometría no disponible: " + reason);
                callback.onBiometricNotAvailable(reason);
            }
        };

        BiometricAuthenticator authenticator = new BiometricAuthenticator(activity, authCallback);
        authenticator.authenticate(customTitle);
    }

    /**
     * Verifica si el dispositivo actual soporta autenticación biométrica
     */
    public boolean isDeviceBiometricCapable() {
        return BiometricAuthenticator.isBiometricAvailable(context) ||
                BiometricAuthenticator.isDeviceCredentialAvailable(context);
    }

    /**
     * Obtiene el estado actual de la capacidad biométrica del dispositivo
     */
    public String getBiometricCapabilityStatus() {
        return BiometricAuthenticator.getBiometricStatusMessage(context);
    }

    /**
     * Limpia toda la configuración biométrica (útil para logout)
     */
    public void clearBiometricConfiguration() {
        preferences.edit()
                .remove(KEY_BIOMETRIC_ENABLED)
                .remove(KEY_USER_EMAIL_FOR_BIOMETRIC)
                .remove(KEY_LAST_BIOMETRIC_AUTH)
                .apply();
        Log.d(TAG, "Configuración biométrica limpiada");
    }

    /**
     * Verifica si se debe pedir autenticación biométrica en el login
     */
    public boolean shouldRequestBiometricAuth() {
        return isBiometricAuthEnabled() &&
                !hasValidRecentBiometricAuth() &&
                isDeviceBiometricCapable();
    }

    /**
     * Obtiene información del usuario configurado para biometría
     */
    public String getBiometricUserEmail() {
        return preferences.getString(KEY_USER_EMAIL_FOR_BIOMETRIC, null);
    }

    /**
     * Verifica si hay configuración biométrica para cualquier usuario
     */
    public boolean hasBiometricConfiguration() {
        return preferences.getBoolean(KEY_BIOMETRIC_ENABLED, false) &&
                preferences.getString(KEY_USER_EMAIL_FOR_BIOMETRIC, null) != null;
    }
}
