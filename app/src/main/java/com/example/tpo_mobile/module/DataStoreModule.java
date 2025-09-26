package com.example.tpo_mobile.module;

import android.content.Context;

import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.datastore.preferences.core.Preferences;

import com.example.tpo_mobile.session.SessionManager;
import com.example.tpo_mobile.utils.BiometricDataStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DataStoreModule {

    private static final String AUTH_PREFERENCES_NAME = "auth_preferences";
    private static final String SESSION_PREFERENCES_NAME = "session_preferences";

    /**
     * Provee el DataStore principal para autenticación (TokenManager)
     */
    @Provides
    @Singleton
    @Named("auth")
    public RxDataStore<Preferences> provideAuthDataStore(@ApplicationContext Context context) {
        return new RxPreferenceDataStoreBuilder(context, AUTH_PREFERENCES_NAME).build();
    }

    /**
     * Provee el DataStore para sesiones (SessionManager)
     */
    @Provides
    @Singleton
    @Named("session")
    public RxDataStore<Preferences> provideSessionDataStore(@ApplicationContext Context context) {
        return new RxPreferenceDataStoreBuilder(context, SESSION_PREFERENCES_NAME).build();
    }

    /**
     * Provee el DataStore por defecto (para mantener compatibilidad)
     * Apunta al mismo DataStore de autenticación
     */
    @Provides
    @Singleton
    public RxDataStore<Preferences> provideDataStore(@ApplicationContext Context context) {
        return new RxPreferenceDataStoreBuilder(context, AUTH_PREFERENCES_NAME).build();
    }

    /**
     * Provee el BiometricDataStore para manejo de datos biométricos
     */
    @Provides
    @Singleton
    public BiometricDataStore provideBiometricDataStore(@ApplicationContext Context context) {
        return new BiometricDataStore(context);
    }

    /**
     * Provee el SessionManager como singleton
     * Nota: Este enfoque es alternativo al patrón Singleton usado en SessionHolder.
     * Puedes usar cualquiera de los dos enfoques según tu preferencia.
     */
    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }
}