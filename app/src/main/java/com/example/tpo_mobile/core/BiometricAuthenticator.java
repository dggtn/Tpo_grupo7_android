package com.example.tpo_mobile.core;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class BiometricAuthenticator {
    private static final String TAG = "BiometricAuthenticator";

    private final Context context;
    private final Executor executor;
    private final BiometricPrompt biometricPrompt;
    private final BiometricPrompt.PromptInfo promptInfo;

    public interface AuthenticationCallback {
        void onAuthenticationSuccess();
        void onAuthenticationError(int errorCode, String errorMessage);
        void onAuthenticationFailed();
        void onBiometricNotAvailable(String reason);
    }

    // Enum para tipos de autenticación disponibles
    public enum AuthenticationType {
        BIOMETRIC_STRONG,
        BIOMETRIC_WEAK,
        DEVICE_CREDENTIAL,
        BIOMETRIC_OR_DEVICE_CREDENTIAL
    }

    public BiometricAuthenticator(AppCompatActivity activity, AuthenticationCallback callback) {
        this(activity, callback, AuthenticationType.BIOMETRIC_OR_DEVICE_CREDENTIAL);
    }

    public BiometricAuthenticator(AppCompatActivity activity, AuthenticationCallback callback, AuthenticationType type) {
        this.context = activity;
        this.executor = ContextCompat.getMainExecutor(activity);

        biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e(TAG, "Authentication error: " + errString + " (Code: " + errorCode + ")");

                // Manejar códigos de error específicos
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    callback.onAuthenticationError(errorCode, "Autenticación cancelada por el usuario");
                } else if (errorCode == BiometricPrompt.ERROR_LOCKOUT ||
                        errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT) {
                    callback.onAuthenticationError(errorCode, "Demasiados intentos fallidos. Intenta más tarde.");
                } else {
                    callback.onAuthenticationError(errorCode, errString.toString());
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Authentication succeeded!");

                // Verificar el tipo de autenticación utilizada
                if (result.getAuthenticationType() == BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) {
                    Log.d(TAG, "Autenticación biométrica exitosa");
                } else if (result.getAuthenticationType() == BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL) {
                    Log.d(TAG, "Autenticación con credencial de dispositivo exitosa");
                }

                callback.onAuthenticationSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.w(TAG, "Authentication failed - biometric not recognized");
                callback.onAuthenticationFailed();
            }
        });

        // Configurar PromptInfo según el tipo de autenticación
        promptInfo = buildPromptInfo(type);
    }

    private BiometricPrompt.PromptInfo buildPromptInfo(AuthenticationType type) {
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación Biométrica")
                .setSubtitle("Usa tu huella digital, rostro o credencial del dispositivo para iniciar sesión");

        switch (type) {
            case BIOMETRIC_STRONG:
                builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                        .setNegativeButtonText("Cancelar");
                break;

            case BIOMETRIC_WEAK:
                builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                        .setNegativeButtonText("Cancelar");
                break;

            case DEVICE_CREDENTIAL:
                builder.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL);
                break;

            case BIOMETRIC_OR_DEVICE_CREDENTIAL:
            default:
                builder.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG |
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                );
                break;
        }

        return builder.build();
    }

    public void authenticate() {
        authenticate(null);
    }

    public void authenticate(String customTitle) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int allowedAuthenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG |
                BiometricManager.Authenticators.DEVICE_CREDENTIAL;

        switch (biometricManager.canAuthenticate(allowedAuthenticators)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "App can authenticate using biometrics.");

                // Crear nuevo PromptInfo si se proporciona título personalizado
                BiometricPrompt.PromptInfo currentPromptInfo = promptInfo;
                if (customTitle != null && !customTitle.isEmpty()) {
                    currentPromptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle(customTitle)
                            .setSubtitle("Usa tu huella digital, rostro o credencial del dispositivo")
                            .setAllowedAuthenticators(allowedAuthenticators)
                            .build();
                }

                biometricPrompt.authenticate(currentPromptInfo);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "No biometric features available on this device.");
                handleBiometricNotAvailable("Este dispositivo no tiene hardware biométrico disponible.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Biometric features are currently unavailable.");
                handleBiometricNotAvailable("La autenticación biométrica no está disponible temporalmente.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e(TAG, "The user hasn't associated any biometric credentials with their account.");
                handleBiometricNotAvailable("No hay datos biométricos configurados. Configúralos en los ajustes de seguridad.");

                // Ofrecer opción para ir a configuración
                showSecuritySettings();
                break;

            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Log.e(TAG, "Security update required for biometric authentication.");
                handleBiometricNotAvailable("Se requiere una actualización de seguridad para usar autenticación biométrica.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Log.e(TAG, "Biometric authentication is not supported.");
                handleBiometricNotAvailable("La autenticación biométrica no es compatible con este dispositivo.");
                break;

            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Log.e(TAG, "Biometric authentication status is unknown.");
                handleBiometricNotAvailable("Estado de autenticación biométrica desconocido.");
                break;

            default:
                Log.e(TAG, "Unknown biometric error.");
                handleBiometricNotAvailable("Error desconocido con la autenticación biométrica.");
                break;
        }
    }

    private void handleBiometricNotAvailable(String reason) {
        // Intentar usar solo credencial del dispositivo como fallback
        BiometricManager biometricManager = BiometricManager.from(context);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                == BiometricManager.BIOMETRIC_SUCCESS) {

            Log.d(TAG, "Usando credencial de dispositivo como alternativa");

            BiometricPrompt.PromptInfo deviceCredentialPrompt = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Autenticación Requerida")
                    .setSubtitle("Usa tu PIN, patrón o contraseña del dispositivo")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();

            biometricPrompt.authenticate(deviceCredentialPrompt);
        } else {
            // Notificar que no hay ninguna autenticación disponible
            if (context instanceof AuthenticationCallback) {
                ((AuthenticationCallback) context).onBiometricNotAvailable(reason);
            }
        }
    }

    private void showSecuritySettings() {
        try {
            final Intent enrollIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            context.startActivity(enrollIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening security settings: " + e.getMessage());

            // Intent alternativo
            try {
                final Intent biometricIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                biometricIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG |
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL);
                context.startActivity(biometricIntent);
            } catch (Exception e2) {
                Log.e(TAG, "Error opening biometric enrollment: " + e2.getMessage());
            }
        }
    }

    // Métodos de utilidad
    public static boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        return biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static boolean isDeviceCredentialAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        return biometricManager.canAuthenticate(
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static String getBiometricStatusMessage(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int status = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        switch (status) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return "Autenticación biométrica disponible";
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "No hay hardware biométrico";
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Hardware biométrico no disponible";
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "No hay datos biométricos configurados";
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "Actualización de seguridad requerida";
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                return "Autenticación biométrica no soportada";
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                return "Estado biométrico desconocido";
            default:
                return "Error biométrico desconocido";
        }
    }
}