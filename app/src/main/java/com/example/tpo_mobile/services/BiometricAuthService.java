package com.example.tpo_mobile.services;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

import com.example.tpo_mobile.core.BiometricAuthenticator;
import com.example.tpo_mobile.utils.TokenManager;
import com.example.tpo_mobile.utils.BiometricDataStore;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class BiometricAuthService {

    private static final String TAG = "BiometricAuthService";

    public interface BiometricAuthCallback {
        void onBiometricAuthSuccess();
        void onBiometricAuthError(String error);
        void onBiometricNotAvailable(String reason);
        void onBiometricAuthFailed();
    }

    private final Context context;
    private final TokenManager tokenManager;
    private final BiometricDataStore biometricDataStore;

    @Inject
    public BiometricAuthService(@ApplicationContext Context context,
                                TokenManager tokenManager,
                                BiometricDataStore biometricDataStore) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.biometricDataStore = biometricDataStore;
    }

    /**
     * Verifica si el dispositivo tiene capacidades biométricas
     */
    public boolean isDeviceBiometricCapable() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG |
                BiometricManager.Authenticators.DEVICE_CREDENTIAL;

        int result = biometricManager.canAuthenticate(authenticators);

        boolean capable = result == BiometricManager.BIOMETRIC_SUCCESS;
        Log.d(TAG, "Dispositivo biométrico capaz: " + capable + " (código: " + result + ")");

        return capable;
    }

    /**
     * Verifica si la autenticación biométrica está habilitada para el usuario actual
     */
    public boolean isBiometricAuthEnabled() {
        return biometricDataStore.isBiometricEnabled();
    }

    /**
     * Verifica si hay configuración biométrica guardada
     */
    public boolean hasBiometricConfiguration() {
        String email = biometricDataStore.getBiometricUserEmail();
        boolean enabled = biometricDataStore.isBiometricEnabled();

        boolean hasConfig = email != null && !email.isEmpty() && enabled;
        Log.d(TAG, "Tiene configuración biométrica: " + hasConfig + " (email: " + email + ", enabled: " + enabled + ")");

        return hasConfig;
    }

    /**
     * Obtiene el email del usuario configurado para biometría
     */
    public String getBiometricUserEmail() {
        return biometricDataStore.getBiometricUserEmail();
    }

    /**
     * Habilita la autenticación biométrica para el usuario actual
     */
    public void enableBiometricAuth() {
        String currentUserEmail = tokenManager.getUserEmail();
        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            biometricDataStore.setBiometricEnabled(true);
            biometricDataStore.setBiometricUserEmail(currentUserEmail);
            biometricDataStore.setBiometricLastUsed(System.currentTimeMillis());

            Log.d(TAG, "Autenticación biométrica habilitada para: " + currentUserEmail);
        } else {
            Log.w(TAG, "No se puede habilitar biometría: no hay usuario logueado");
        }
    }

    /**
     * Deshabilita la autenticación biométrica
     */
    public void disableBiometricAuth() {
        biometricDataStore.setBiometricEnabled(false);
        Log.d(TAG, "Autenticación biométrica deshabilitada");
    }

    /**
     * Limpia completamente la configuración biométrica
     */
    public void clearBiometricConfiguration() {
        biometricDataStore.clearBiometricData();
        Log.d(TAG, "Configuración biométrica completamente limpiada");
    }

    /**
     * Determina si se debe solicitar autenticación biométrica automáticamente
     */
    public boolean shouldRequestBiometricAuth() {
        // Solo si está habilitada y hay usuario logueado
        if (!isBiometricAuthEnabled() || !tokenManager.isLoggedIn()) {
            return false;
        }

        // Verificar que el usuario biométrico coincida con el usuario actual
        String biometricEmail = getBiometricUserEmail();
        String currentEmail = tokenManager.getUserEmail();

        if (biometricEmail == null || currentEmail == null) {
            return false;
        }

        boolean shouldRequest = biometricEmail.equals(currentEmail);
        Log.d(TAG, "¿Solicitar biometría automática? " + shouldRequest +
                " (biométrico: " + biometricEmail + ", actual: " + currentEmail + ")");

        return shouldRequest;
    }

    /**
     * Autentica usando biometría
     */
    public void authenticateWithBiometric(AppCompatActivity activity,
                                          BiometricAuthCallback callback) {
        authenticateWithBiometric(activity, callback, null);
    }

    /**
     * Autentica usando biometría con título personalizado
     */
    public void authenticateWithBiometric(AppCompatActivity activity,
                                          BiometricAuthCallback callback,
                                          String customTitle) {
        if (!isDeviceBiometricCapable()) {
            String reason = BiometricAuthenticator.getBiometricStatusMessage(context);
            callback.onBiometricNotAvailable(reason);
            return;
        }

        Log.d(TAG, "Iniciando autenticación biométrica");

        BiometricAuthenticator authenticator = new BiometricAuthenticator(
                activity,
                new BiometricAuthenticator.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSuccess() {
                        Log.d(TAG, "Autenticación biométrica exitosa");

                        // Actualizar último uso
                        biometricDataStore.setBiometricLastUsed(System.currentTimeMillis());

                        callback.onBiometricAuthSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, String errorMessage) {
                        Log.e(TAG, "Error biométrico: " + errorMessage + " (código: " + errorCode + ")");
                        callback.onBiometricAuthError(errorMessage);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Log.w(TAG, "Autenticación biométrica fallida");
                        callback.onBiometricAuthFailed();
                    }

                    @Override
                    public void onBiometricNotAvailable(String reason) {
                        Log.w(TAG, "Biometría no disponible: " + reason);
                        callback.onBiometricNotAvailable(reason);
                    }
                },
                BiometricAuthenticator.AuthenticationType.BIOMETRIC_OR_DEVICE_CREDENTIAL
        );

        // Usar título personalizado si se proporciona
        if (customTitle != null && !customTitle.trim().isEmpty()) {
            authenticator.authenticate(customTitle);
        } else {
            authenticator.authenticate();
        }
    }

    /**
     * Verifica si la configuración biométrica es válida para el usuario actual
     */
    public boolean isBiometricConfigValidForCurrentUser() {
        if (!isBiometricAuthEnabled()) {
            return false;
        }

        String biometricEmail = getBiometricUserEmail();
        String currentEmail = tokenManager.getUserEmail();

        if (biometricEmail == null || currentEmail == null) {
            return false;
        }

        return biometricEmail.equals(currentEmail);
    }

    /**
     * Actualiza el email del usuario biométrico si es necesario
     */
    public void updateBiometricUserIfNeeded() {
        if (!isBiometricAuthEnabled()) {
            return;
        }

        String currentEmail = tokenManager.getUserEmail();
        String biometricEmail = getBiometricUserEmail();

        if (currentEmail != null && !currentEmail.equals(biometricEmail)) {
            Log.d(TAG, "Actualizando usuario biométrico de " + biometricEmail + " a " + currentEmail);
            biometricDataStore.setBiometricUserEmail(currentEmail);
        }
    }

    /**
     * Obtiene información sobre el estado de la configuración biométrica
     */
    public String getBiometricStatusInfo() {
        if (!isDeviceBiometricCapable()) {
            return "Dispositivo no compatible con biometría";
        }

        if (!isBiometricAuthEnabled()) {
            return "Biometría disponible pero no habilitada";
        }

        String email = getBiometricUserEmail();
        long lastUsed = biometricDataStore.getBiometricLastUsed();

        StringBuilder info = new StringBuilder("Biometría activa");
        if (email != null) {
            info.append(" para: ").append(email);
        }

        if (lastUsed > 0) {
            long daysSinceLastUse = (System.currentTimeMillis() - lastUsed) / (1000 * 60 * 60 * 24);
            info.append(" (último uso: hace ").append(daysSinceLastUse).append(" días)");
        }

        return info.toString();
    }

    /**
     * Determina si se debe mostrar el botón biométrico en la UI
     */
    public boolean shouldShowBiometricButton() {
        return isDeviceBiometricCapable() &&
                (isBiometricAuthEnabled() ||
                        (!isBiometricAuthEnabled() && tokenManager.isLoggedIn()));
    }

    /**
     * Método para testing/debug
     */
    public void logBiometricStatus() {
        Log.d(TAG, "=== Estado Biométrico ===");
        Log.d(TAG, "Dispositivo capaz: " + isDeviceBiometricCapable());
        Log.d(TAG, "Habilitado: " + isBiometricAuthEnabled());
        Log.d(TAG, "Email biométrico: " + getBiometricUserEmail());
        Log.d(TAG, "Email actual: " + tokenManager.getUserEmail());
        Log.d(TAG, "Usuario logueado: " + tokenManager.isLoggedIn());
        Log.d(TAG, "Debe solicitar auto: " + shouldRequestBiometricAuth());
        Log.d(TAG, "Válido para usuario actual: " + isBiometricConfigValidForCurrentUser());
        Log.d(TAG, "========================");
    }

    /**
     * Verifica si ha pasado mucho tiempo desde el último uso biométrico
     * @param maxDaysWithoutUse máximo de días sin uso antes de requerir reconfiguración
     */
    public boolean shouldReconfirmBiometric(int maxDaysWithoutUse) {
        if (!isBiometricAuthEnabled()) {
            return false;
        }

        long lastUsed = biometricDataStore.getBiometricLastUsed();
        if (lastUsed == 0) {
            return false; // No hay registro de último uso
        }

        long daysSinceLastUse = (System.currentTimeMillis() - lastUsed) / (1000 * 60 * 60 * 24);
        return daysSinceLastUse > maxDaysWithoutUse;
    }

    /**
     * Migrar configuración biométrica de SharedPreferences a DataStore si existe
     * (Solo para compatibilidad con versiones anteriores)
     */
    public void migrateLegacyBiometricConfig() {
        // Este método se puede implementar si hay datos previos en SharedPreferences
        // Por ahora, solo registramos que se llamó
        Log.d(TAG, "Verificación de migración de configuración biométrica legacy completada");
    }
}