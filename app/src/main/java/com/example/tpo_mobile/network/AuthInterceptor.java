package com.example.tpo_mobile.network;

import android.util.Log;
import com.example.tpo_mobile.session.SessionHolder;
import com.example.tpo_mobile.utils.TokenManager;
import java.io.IOException;
import javax.inject.Inject;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";

    private final TokenManager tokenManager;

    @Inject
    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String url = originalRequest.url().toString();

        // No agregar token a endpoints de autenticación
        if (shouldSkipAuth(url)) {
            Log.d(TAG, "Omitiendo autenticación para: " + url);
            return chain.proceed(originalRequest);
        }

        // Intentar obtener token de ambos sistemas (para compatibilidad durante migración)
        String token = getAuthToken();

        if (token != null && !token.trim().isEmpty()) {
            Log.d(TAG, "Agregando token de autorización a: " + url);
            Request authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(authenticatedRequest);
        } else {
            Log.w(TAG, "No hay token disponible para: " + url);
            return chain.proceed(originalRequest);
        }
    }

    /**
     * Obtiene el token de autenticación desde múltiples fuentes (para compatibilidad)
     */
    private String getAuthToken() {
        String token = null;

        try {
            // Prioridad 1: TokenManager (sistema principal)
            token = tokenManager.getToken();
            if (token != null && !token.trim().isEmpty()) {
                Log.d(TAG, "Token obtenido de TokenManager");
                return token;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error obteniendo token de TokenManager: " + e.getMessage());
        }

        try {
            // Prioridad 2: SessionManager (nuevo sistema)
            if (SessionHolder.isInitialized()) {
                token = SessionHolder.getCurrentToken();
                if (token != null && !token.trim().isEmpty()) {
                    Log.d(TAG, "Token obtenido de SessionManager");
                    return token;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error obteniendo token de SessionManager: " + e.getMessage());
        }

        Log.w(TAG, "No se encontró token en ningún sistema");
        return null;
    }

    /**
     * Determina si una URL debe omitir la autenticación
     */
    private boolean shouldSkipAuth(String url) {
        return url.contains("/auth/") ||
                url.contains("/public/") ||
                url.contains("/health") ||
                url.contains("/actuator/");
    }

    /**
     * Método estático de utilidad para verificar si hay token disponible
     * Útil para otros componentes que necesiten verificar autenticación
     */
    public static boolean hasValidToken() {
        try {
            // Verificar TokenManager
            // Nota: Aquí necesitarías inyectar o acceder al TokenManager de alguna manera
            // Por simplicidad, verifico solo SessionManager

            if (SessionHolder.isInitialized()) {
                return SessionHolder.hasActiveSession();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error verificando token: " + e.getMessage());
        }

        return false;
    }
}