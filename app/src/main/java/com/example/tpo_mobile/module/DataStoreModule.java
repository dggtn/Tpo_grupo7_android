package com.example.tpo_mobile.module;

import android.content.Context;

import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.datastore.preferences.core.Preferences;

import com.example.tpo_mobile.utils.BiometricDataStore;

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

    @Provides
    @Singleton
    public RxDataStore<Preferences> provideDataStore(@ApplicationContext Context context) {
        return new RxPreferenceDataStoreBuilder(context, AUTH_PREFERENCES_NAME).build();
    }

    @Provides
    @Singleton
    public BiometricDataStore provideBiometricDataStore(@ApplicationContext Context context) {
        return new BiometricDataStore(context);
    }
}