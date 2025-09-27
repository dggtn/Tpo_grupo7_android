package com.example.tpo_mobile;

import android.app.Application;
import android.util.Log;

import com.example.tpo_mobile.session.SessionHolder;
import com.example.tpo_mobile.utils.GlobalErrorHandler;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AppApplication extends Application {

    private static final String TAG = "AppApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Iniciando aplicación TPO Mobile");

        // Inicializar SessionHolder para que esté disponible globalmente
        try {
            SessionHolder.init(this);
            Log.d(TAG, "SessionHolder inicializado exitosamente");
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando SessionHolder: " + e.getMessage(), e);
        }

        // Aquí puedes agregar otras inicializaciones globales si es necesario
        // Por ejemplo: inicialización de librerías, configuración de crash reporting, etc.
        // Configurar el manejador global de errores
        setupGlobalErrorHandling();

        Log.d(TAG, "Aplicación inicializada correctamente");
    }

    private void setupGlobalErrorHandling() {
        try {
            // Configurar el manejador global para crashes no capturados
            GlobalErrorHandler.setupGlobalErrorHandler(this);
            Log.d(TAG, "Manejador global de errores configurado exitosamente");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando el manejador global de errores: " + e.getMessage(), e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.d(TAG, "Terminando aplicación");

        // Limpiar recursos si es necesario
        try {
            SessionHolder.release();
            Log.d(TAG, "SessionHolder liberado");
        } catch (Exception e) {
            Log.w(TAG, "Error liberando SessionHolder: " + e.getMessage());
        }
    }

    @Override
    public void onLowMemory() {
        Log.w(TAG, "Memoria baja detectada");
        super.onLowMemory();

        // Opcional: Limpiar caches o recursos no esenciales
        System.gc();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        switch (level) {
            case TRIM_MEMORY_UI_HIDDEN:
                Log.d(TAG, "UI oculta - liberando recursos UI");
                break;
            case TRIM_MEMORY_RUNNING_MODERATE:
                Log.w(TAG, "Memoria moderadamente baja");
                break;
            case TRIM_MEMORY_RUNNING_LOW:
                Log.w(TAG, "Memoria baja");
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL:
                Log.e(TAG, "Memoria críticamente baja");
                break;
        }
    }
}
