package com.example.tpo_mobile.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.tpo_mobile.activities.ErrorHandlerActivity;

/**
 * Manejador global de errores no capturados
 * Se debe configurar en la clase Application principal
 */
public class GlobalErrorHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "GlobalErrorHandler";

    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;

    public GlobalErrorHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e(TAG, "Error no capturado en thread: " + thread.getName(), throwable);

        try {
            // Determinar el tipo de error y la actividad de origen
            String errorType = determineErrorType(throwable);
            String sourceActivity = determineSourceActivity(throwable);

            // Mostrar la pantalla de error
            ErrorHandlerActivity.showCrashError(context, throwable, sourceActivity);

            // Esperar un poco para que se muestre la actividad
            Thread.sleep(2000);

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando pantalla de error: " + e.getMessage());

            // Si falla todo, usar el handler por defecto
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        }

        // Terminar el proceso
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private String determineErrorType(Throwable throwable) {
        String className = throwable.getClass().getSimpleName();
        String message = throwable.getMessage();

        if (message != null) {
            message = message.toLowerCase();
        }

        // Analizar el tipo de excepción
        if (throwable instanceof OutOfMemoryError) {
            return "MEMORY_ERROR";
        } else if (throwable instanceof SecurityException) {
            return "SECURITY_ERROR";
        } else if (throwable instanceof RuntimeException) {
            return "RUNTIME_ERROR";
        } else if (throwable instanceof Exception) {
            return "GENERAL_ERROR";
        } else {
            return "UNKNOWN_ERROR";
        }
    }

    private String determineSourceActivity(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();

        // Buscar en el stack trace elementos que contengan nuestro package
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();

            if (className.contains("com.example.tpo_mobile.activities")) {
                return className.substring(className.lastIndexOf('.') + 1);
            } else if (className.contains("com.example.tpo_mobile.fragments")) {
                return className.substring(className.lastIndexOf('.') + 1);
            }
        }

        return "Unknown";
    }

    /**
     * Configura el manejador global de errores
     * Debe llamarse desde la clase Application
     */
    public static void setupGlobalErrorHandler(Application application) {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalErrorHandler(application));
        Log.d(TAG, "Manejador global de errores configurado");
    }
}
