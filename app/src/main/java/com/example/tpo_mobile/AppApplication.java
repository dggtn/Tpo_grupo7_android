package com.example.tpo_mobile;

import android.app.Application;
import android.util.Log;

import com.example.tpo_mobile.session.SessionHolder;

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

        Log.d(TAG, "Aplicación inicializada correctamente");
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
        super.onLowMemory();
        Log.w(TAG, "Aplicación recibió advertencia de memoria baja");
        // Aquí podrías limpiar caches o recursos no esenciales si fuera necesario
    }
}
