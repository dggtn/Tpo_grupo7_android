package com.example.tpo_mobile.session;

import android.content.Context;
import android.util.Log;

public final class SessionHolder {
    private static final String TAG = "SessionHolder";
    private static SessionManager INSTANCE;
    private static final Object lock = new Object();

    private SessionHolder() {
        // Constructor privado para prevenir instanciación
    }

    /**
     * Inicializa el SessionManager con el contexto de la aplicación.
     * Este método debe ser llamado en Application.onCreate()
     *
     * @param appContext Contexto de la aplicación
     */
    public static void init(Context appContext) {
        if (appContext == null) {
            throw new IllegalArgumentException("Context no puede ser null");
        }

        synchronized (lock) {
            if (INSTANCE == null) {
                INSTANCE = new SessionManager(appContext.getApplicationContext());
                Log.d(TAG, "SessionManager inicializado exitosamente");
            } else {
                Log.w(TAG, "SessionManager ya estaba inicializado, ignorando reinicialización");
            }
        }
    }

    /**
     * Obtiene la instancia singleton del SessionManager.
     *
     * @return SessionManager instance
     * @throws IllegalStateException si no se ha llamado init() primero
     */
    public static SessionManager get() {
        synchronized (lock) {
            if (INSTANCE == null) {
                throw new IllegalStateException(
                        "SessionHolder no inicializado. Debes llamar SessionHolder.init(context) " +
                                "en el método onCreate() de tu Application class antes de usar get()."
                );
            }
            return INSTANCE;
        }
    }

    /**
     * Verifica si el SessionManager está inicializado.
     *
     * @return true si está inicializado, false en caso contrario
     */
    public static boolean isInitialized() {
        synchronized (lock) {
            return INSTANCE != null;
        }
    }

    /**
     * Reinicia el SessionManager. Útil para testing o casos especiales.
     * No recomendado para uso normal de la aplicación.
     *
     * @param appContext Nuevo contexto de la aplicación
     */
    public static void reinitialize(Context appContext) {
        if (appContext == null) {
            throw new IllegalArgumentException("Context no puede ser null");
        }

        synchronized (lock) {
            Log.d(TAG, "Reinicializando SessionManager");
            INSTANCE = new SessionManager(appContext.getApplicationContext());
        }
    }

    /**
     * Libera la instancia del SessionManager. Útil para testing.
     * Después de llamar este método, será necesario llamar init() nuevamente.
     */
    public static void release() {
        synchronized (lock) {
            Log.d(TAG, "Liberando SessionManager");
            INSTANCE = null;
        }
    }

    // Métodos de conveniencia para acceso rápido a funciones comunes de SessionManager

    /**
     * Método de conveniencia para verificar si hay una sesión activa.
     *
     * @return true si hay sesión activa, false en caso contrario
     * @throws IllegalStateException si SessionHolder no está inicializado
     */
    public static boolean hasActiveSession() {
        return get().hasActiveSession();
    }

    /**
     * Método de conveniencia para obtener el token actual.
     *
     * @return Token JWT actual o null si no hay sesión
     * @throws IllegalStateException si SessionHolder no está inicializado
     */
    public static String getCurrentToken() {
        return get().getToken();
    }

    /**
     * Método de conveniencia para obtener el User ID actual.
     *
     * @return User ID actual o null si no hay sesión
     * @throws IllegalStateException si SessionHolder no está inicializado
     */
    public static Long getCurrentUserId() {
        return get().getUserId();
    }

    /**
     * Método de conveniencia para cerrar la sesión actual.
     *
     * @throws IllegalStateException si SessionHolder no está inicializado
     */
    public static void clearCurrentSession() {
        get().clearSession();
    }

    /**
     * Método de conveniencia para obtener información de la sesión actual.
     * Útil para debugging.
     *
     * @return String con información de la sesión
     * @throws IllegalStateException si SessionHolder no está inicializado
     */
    public static String getCurrentSessionInfo() {
        return get().getSessionInfo();
    }
}