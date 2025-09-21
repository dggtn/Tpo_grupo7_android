package com.example.tpo_mobile.utils;

import android.content.Context;
import android.util.Log;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.functions.Consumer;

@Singleton
public class BiometricDataStore {
    private static final String TAG = "BiometricDataStore";
    private static final String BIOMETRIC_DATASTORE_NAME = "biometric_preferences";

    // Keys para DataStore biométrico
    private static final Preferences.Key<Boolean> BIOMETRIC_ENABLED_KEY = PreferencesKeys.booleanKey("biometric_enabled");
    private static final Preferences.Key<String> BIOMETRIC_USER_EMAIL_KEY = PreferencesKeys.stringKey("biometric_user_email");
    private static final Preferences.Key<Long> BIOMETRIC_LAST_USED_KEY = PreferencesKeys.longKey("biometric_last_used");
    private static final Preferences.Key<Long> BIOMETRIC_SETUP_TIME_KEY = PreferencesKeys.longKey("biometric_setup_time");
    private static final Preferences.Key<Boolean> BIOMETRIC_AUTO_REQUEST_KEY = PreferencesKeys.booleanKey("biometric_auto_request");

    private final RxDataStore<Preferences> biometricDataStore;

    @Inject
    public BiometricDataStore(@ApplicationContext Context context) {
        this.biometricDataStore = new RxPreferenceDataStoreBuilder(context, BIOMETRIC_DATASTORE_NAME).build();
        Log.d(TAG, "BiometricDataStore inicializado");
    }

    /**
     * Habilita/deshabilita la autenticación biométrica
     */
    public void setBiometricEnabled(boolean enabled) {
        try {
            biometricDataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.set(BIOMETRIC_ENABLED_KEY, enabled);

                // Si se está habilitando por primera vez, guardar tiempo de configuración
                if (enabled && prefsIn.get(BIOMETRIC_SETUP_TIME_KEY) == null) {
                    mutablePreferences.set(BIOMETRIC_SETUP_TIME_KEY, System.currentTimeMillis());
                }

                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "Biometría " + (enabled ? "habilitada" : "deshabilitada"));
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error configurando biometría: " + throwable.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error en setBiometricEnabled: " + e.getMessage());
        }
    }

/**
 * Verifica si la autenticación biométrica está habilitada
 */
public boolean isBiometricEnabled() {
    try {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        biometricDataStore.data()
                .map(prefs -> prefs.get(BIOMETRIC_ENABLED_KEY))
                .first(false)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean enabled) throws Throwable {
                                future.complete(enabled != null ? enabled : false);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Throwable {
                                Log.e(TAG, "Error obteniendo estado biométrico: " + throwable.getMessage());
                                future.complete(false);
                            }
                        }
                );

        return future.get(2, TimeUnit.SECONDS);
    } catch (Exception e) {
        Log.e(TAG, "Error en isBiometricEnabled: " + e.getMessage());
        return false;
    }
}

/**
 * Guarda el email del usuario asociado a la configuración biométrica
 */
public void setBiometricUserEmail(String email) {
    try {
        biometricDataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(BIOMETRIC_USER_EMAIL_KEY, email);
            return Single.just(mutablePreferences);
        }).subscribe(
                new Consumer<Preferences>() {
                    @Override
                    public void accept(Preferences preferences) throws Throwable {
                        Log.d(TAG, "Email biométrico guardado: " + email);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        Log.e(TAG, "Error guardando email biométrico: " + throwable.getMessage());
                    }
                }
        );
    } catch (Exception e) {
        Log.e(TAG, "Error en setBiometricUserEmail: " + e.getMessage());
    }
}

/**
 * Obtiene el email del usuario asociado a la configuración biométrica
 */
public String getBiometricUserEmail() {
    try {
        CompletableFuture<String> future = new CompletableFuture<>();

        biometricDataStore.data()
                .map(prefs -> prefs.get(BIOMETRIC_USER_EMAIL_KEY))
                .first("")
                .subscribeOn(Schedulers.io())
                .subscribe(
                        new Consumer<String>() {
                            @Override
                            public void accept(String email) throws Throwable {
                                future.complete(email != null && !email.isEmpty() ? email : null);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Throwable {
                                Log.e(TAG, "Error obteniendo email biométrico: " + throwable.getMessage());
                                future.complete(null);
                            }
                        }
                );

        return future.get(2, TimeUnit.SECONDS);
    } catch (Exception e) {
        Log.e(TAG, "Error en getBiometricUserEmail: " + e.getMessage());
        return null;
    }
}

/**
 * Actualiza el tiempo del último uso biométrico
 */
public void setBiometricLastUsed(long timestamp) {
    try {
        biometricDataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(BIOMETRIC_LAST_USED_KEY, timestamp);
            return Single.just(mutablePreferences);
        }).subscribe(
                new Consumer<Preferences>() {
                    @Override
                    public void accept(Preferences preferences) throws Throwable {
                        Log.d(TAG, "Último uso biométrico actualizado");
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        Log.e(TAG, "Error actualizando último uso: " + throwable.getMessage());
                    }
                }
        );
    } catch (Exception e) {
        Log.e(TAG, "Error en setBiometricLastUsed: " + e.getMessage());
    }
}

/**
 * Obtiene el timestamp del último uso biométrico
 */
public long getBiometricLastUsed() {
    try {
        CompletableFuture<Long> future = new CompletableFuture<>();

        biometricDataStore.data()
                .map(prefs -> prefs.get(BIOMETRIC_LAST_USED_KEY))
                .first(0L)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        new Consumer<Long>() {
                            @Override
                            public void accept(Long timestamp) throws Throwable {
                                future.complete(timestamp != null ? timestamp : 0L);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Throwable {
                                Log.e(TAG, "Error obteniendo último uso: " + throwable.getMessage());
                                future.complete(0L);
                            }
                        }
                );

        return future.get(2, TimeUnit.SECONDS);
    } catch (Exception e) {
        Log.e(TAG, "Error en getBiometricLastUsed: " + e.getMessage());
        return 0L;
    }
}

/**
 * Obtiene el tiempo cuando se configuró la biometría por primera vez
 */
public long getBiometricSetupTime() {
    try {
        CompletableFuture<Long> future = new CompletableFuture<>();

        biometricDataStore.data()
                .map(prefs -> prefs.get(BIOMETRIC_SETUP_TIME_KEY))
                .first(0L)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        new Consumer<Long>() {
                            @Override
                            public void accept(Long timestamp) throws Throwable {
                                future.complete(timestamp != null ? timestamp : 0L);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Throwable {
                                Log.e(TAG, "Error obteniendo tiempo de configuración: " + throwable.getMessage());
                                future.complete(0L);
                            }
                        }
                );

        return future.get(2, TimeUnit.SECONDS);
    } catch (Exception e) {
        Log.e(TAG, "Error en getBiometricSetupTime: " + e.getMessage());
        return 0L;
    }
}

/**
 * Configura si se debe solicitar autenticación biométrica automáticamente
 */
public void setBiometricAutoRequest(boolean autoRequest) {
    try {
        biometricDataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(BIOMETRIC_AUTO_REQUEST_KEY, autoRequest);
            return Single.just(mutablePreferences);
        }).subscribe(
                new Consumer<Preferences>() {
                    @Override
                    public void accept(Preferences preferences) throws Throwable {
                        Log.d(TAG, "Auto-request biométrico configurado: " + autoRequest);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        Log.e(TAG, "Error configurando auto-request: " + throwable.getMessage());
                    }
                }
        );
    } catch (Exception e) {
        Log.e(TAG, "Error en setBiometricAutoRequest: " + e.getMessage());
    }
}

/**
 * Verifica si se debe solicitar autenticación biométrica automáticamente
 */
public boolean shouldBiometricAutoRequest() {
    try {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        biometricDataStore.data()
                .map(prefs -> prefs.get(BIOMETRIC_AUTO_REQUEST_KEY))
                .first(true) // Por defecto true
                .subscribeOn(Schedulers.io())
                .subscribe(
                        new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean autoRequest) throws Throwable {
                                future.complete(autoRequest != null ? autoRequest : true);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Throwable {
                                Log.e(TAG, "Error obteniendo auto-request: " + throwable.getMessage());
                                future.complete(true);
                            }
                        }
                );

        return future.get(2, TimeUnit.SECONDS);
    } catch (Exception e) {
        Log.e(TAG, "Error en shouldBiometricAutoRequest: " + e.getMessage());
        return true;
    }
}

/**
 * Limpia todos los datos biométricos
 */
public void clearBiometricData() {
    String email = getBiometricUserEmail(); // Para logging

    try {
        biometricDataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.remove(BIOMETRIC_ENABLED_KEY);
            mutablePreferences.remove(BIOMETRIC_USER_EMAIL_KEY);
            mutablePreferences.remove(BIOMETRIC_LAST_USED_KEY);
            mutablePreferences.remove(BIOMETRIC_SETUP_TIME_KEY);
            mutablePreferences.remove(BIOMETRIC_AUTO_REQUEST_KEY);
            return Single.just(mutablePreferences);
        }).subscribe(
                new Consumer<Preferences>() {
                    @Override
                    public void accept(Preferences preferences) throws Throwable {
                        Log.d(TAG, "Datos biométricos limpiados para usuario: " + (email != null ? email : "desconocido"));
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        Log.e(TAG, "Error limpiando datos biométricos: " + throwable.getMessage());
                    }
                }
        );
    } catch (Exception e) {
        Log.e(TAG, "Error en clearBiometricData: " + e.getMessage());
    }
}

/**
 * Observa cambios en el estado biométrico
 */
public Flowable<Boolean> observeBiometricEnabled() {
    return biometricDataStore.data()
            .map(prefs -> prefs.get(BIOMETRIC_ENABLED_KEY))
            .map(enabled -> enabled != null ? enabled : false)
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io());
}

/**
 * Observa cambios en el email biométrico
 */
public Flowable<String> observeBiometricUserEmail() {
    return biometricDataStore.data()
            .map(prefs -> prefs.get(BIOMETRIC_USER_EMAIL_KEY))
            .map(email -> email != null && !email.isEmpty() ? email : null)
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io());
}

/**
 * Verifica si hay datos biométricos configurados
 */
public boolean hasBiometricData() {
    return isBiometricEnabled() && getBiometricUserEmail() != null;
}

/**
 * Obtiene información de debug sobre el estado biométrico
 */
public String getBiometricDebugInfo() {
    StringBuilder info = new StringBuilder();
    info.append("=== Biometric DataStore Debug ===\n");
    info.append("Enabled: ").append(isBiometricEnabled()).append("\n");
    info.append("User Email: ").append(getBiometricUserEmail()).append("\n");
    info.append("Last Used: ").append(getBiometricLastUsed()).append("\n");
    info.append("Setup Time: ").append(getBiometricSetupTime()).append("\n");
    info.append("Auto Request: ").append(shouldBiometricAutoRequest()).append("\n");
    info.append("Has Data: ").append(hasBiometricData()).append("\n");
    info.append("===============================");

    return info.toString();
}

    /**
     * Actualiza configuración biométrica de manera transaccional
     */
    public void updateBiometricConfig(String email, boolean enabled, boolean autoRequest) {
        try {
            biometricDataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();

                mutablePreferences.set(BIOMETRIC_ENABLED_KEY, enabled);
                mutablePreferences.set(BIOMETRIC_USER_EMAIL_KEY, email);
                mutablePreferences.set(BIOMETRIC_AUTO_REQUEST_KEY, autoRequest);
                mutablePreferences.set(BIOMETRIC_LAST_USED_KEY, System.currentTimeMillis());

                // Si es primera vez que se habilita
                if (enabled && prefsIn.get(BIOMETRIC_SETUP_TIME_KEY) == null) {
                    mutablePreferences.set(BIOMETRIC_SETUP_TIME_KEY, System.currentTimeMillis());
                }

                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "Configuración biométrica actualizada para: " + email);
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error actualizando configuración biométrica: " + throwable.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error en updateBiometricConfig: " + e.getMessage());
        }
    }
}