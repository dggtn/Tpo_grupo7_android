package com.example.tpo_mobile.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.tpo_mobile.activities.ErrorHandlerActivity;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;

/**
 * Utilidades para manejo de errores en toda la aplicación
 */
public class ErrorUtils {

    private static final String TAG = "ErrorUtils";

    /**
     * Maneja errores de red/HTTP y muestra la pantalla de error apropiada
     */
    public static void handleNetworkError(Context context, Throwable error, String sourceActivity) {
        Log.e(TAG, "Error de red en " + sourceActivity, error);

        String message;
        String details = error.getMessage();

        if (error instanceof UnknownHostException) {
            message = "Sin conexión a internet. Verifica tu conexión y vuelve a intentar.";
        } else if (error instanceof SocketTimeoutException) {
            message = "La conexión está tardando demasiado. Intenta nuevamente.";
        } else if (error instanceof IOException) {
            message = "Error de conexión. Verifica tu internet y vuelve a intentar.";
        } else if (error instanceof HttpException) {
            HttpException httpError = (HttpException) error;
            switch (httpError.code()) {
                case 401:
                    ErrorHandlerActivity.showAuthError(context, "Sesión expirada", sourceActivity);
                    return;
                case 403:
                    ErrorHandlerActivity.showAuthError(context, "Sin permisos para realizar esta acción", sourceActivity);
                    return;
                case 404:
                    message = "El recurso solicitado no fue encontrado.";
                    break;
                case 500:
                    message = "Error del servidor. Intenta más tarde.";
                    break;
                default:
                    message = "Error del servidor (código " + httpError.code() + ")";
                    break;
            }
        } else {
            message = "Error de conexión inesperado.";
        }

        ErrorHandlerActivity.showNetworkError(context, details, sourceActivity);
    }

    /**
     * Maneja errores de autenticación
     */
    public static void handleAuthError(Context context, String details, String sourceActivity) {
        Log.w(TAG, "Error de autenticación en " + sourceActivity + ": " + details);
        ErrorHandlerActivity.showAuthError(context, details, sourceActivity);
    }

    /**
     * Maneja errores funcionales (lógica de negocio)
     */
    public static void handleFunctionalError(Context context, String message, String details, String sourceActivity) {
        Log.w(TAG, "Error funcional en " + sourceActivity + ": " + message);
        ErrorHandlerActivity.showFunctionalError(context, message, details, sourceActivity);
    }

    /**
     * Maneja errores críticos/crashes
     */
    public static void handleCriticalError(Context context, Throwable error, String sourceActivity) {
        Log.e(TAG, "Error crítico en " + sourceActivity, error);
        ErrorHandlerActivity.showCrashError(context, error, sourceActivity);
    }

    /**
     * Maneja errores genéricos
     */
    public static void handleGenericError(Context context, String message, Throwable error, String sourceActivity) {
        Log.e(TAG, "Error genérico en " + sourceActivity + ": " + message, error);

        String details = error != null ? error.getMessage() : null;
        ErrorHandlerActivity.showError(context, ErrorHandlerActivity.ERROR_TYPE_UNKNOWN, message, details, sourceActivity);
    }

    /**
     * Wrapper para ejecutar código con manejo automático de errores
     */
    public static void safeExecute(Activity activity, Runnable code, String operationDescription) {
        try {
            code.run();
        } catch (Exception e) {
            String sourceActivity = activity.getClass().getSimpleName();
            Log.e(TAG, "Error en " + operationDescription + " en " + sourceActivity, e);

            // Determinar tipo de error y manejar apropiadamente
            if (isNetworkError(e)) {
                handleNetworkError(activity, e, sourceActivity);
            } else if (isAuthError(e)) {
                handleAuthError(activity, e.getMessage(), sourceActivity);
            } else {
                handleGenericError(activity, "Error en " + operationDescription, e, sourceActivity);
            }
        }
    }

    /**
     * Wrapper asíncrono para callbacks
     */
    public static void safeCallback(Context context, Runnable callback, String sourceActivity, String operationDescription) {
        try {
            callback.run();
        } catch (Exception e) {
            Log.e(TAG, "Error en callback de " + operationDescription, e);
            handleGenericError(context, "Error procesando respuesta", e, sourceActivity);
        }
    }

    /**
     * Determina si un error es de red
     */
    public static boolean isNetworkError(Throwable error) {
        return error instanceof IOException ||
                error instanceof HttpException ||
                error instanceof UnknownHostException ||
                error instanceof SocketTimeoutException;
    }

    /**
     * Determina si un error es de autenticación
     */
    private static boolean isAuthError(Throwable error) {
        if (error instanceof HttpException) {
            int code = ((HttpException) error).code();
            return code == 401 || code == 403;
        }
        return false;
    }

    /**
     * Método para logging estructurado de errores
     */
    public static void logError(String source, String operation, Throwable error) {
        Log.e(TAG, String.format("ERROR | Source: %s | Operation: %s | Message: %s | Type: %s",
                source, operation, error.getMessage(), error.getClass().getSimpleName()), error);
    }

    /**
     * Verifica si el contexto es válido antes de mostrar errores
     */
    public static boolean isValidContext(Context context) {
        if (context == null) {
            Log.w(TAG, "Contexto nulo, no se puede mostrar error");
            return false;
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                Log.w(TAG, "Actividad terminando/destruida, no se puede mostrar error");
                return false;
            }
        }

        return true;
    }

    /**
     * Obtiene el nombre de la actividad actual desde el contexto
     */
    public static String getSourceActivityName(Context context) {
        if (context instanceof Activity) {
            return context.getClass().getSimpleName();
        }
        return "Unknown";
    }
}