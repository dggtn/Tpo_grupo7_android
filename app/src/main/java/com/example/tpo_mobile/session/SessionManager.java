package com.example.tpo_mobile.session;

import android.content.Context;
import android.util.Log;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.functions.Consumer;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String DATASTORE_NAME = "session_preferences";

    // Keys para DataStore
    private static final Preferences.Key<String> KEY_TOKEN = PreferencesKeys.stringKey("jwt");
    private static final Preferences.Key<Long> KEY_USER_ID = PreferencesKeys.longKey("user_id");
    private static final Preferences.Key<Long> KEY_SESSION_TIME = PreferencesKeys.longKey("session_time");

    private final RxDataStore<Preferences> dataStore;

    public SessionManager(Context context) {
        this.dataStore = new RxPreferenceDataStoreBuilder(context.getApplicationContext(), DATASTORE_NAME).build();
    }

    public void saveToken(String jwt) {
        if (jwt == null || jwt.trim().isEmpty()) {
            Log.w(TAG, "Intentando guardar token nulo o vacío");
            return;
        }

        try {
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.set(KEY_TOKEN, jwt);
                mutablePreferences.set(KEY_SESSION_TIME, System.currentTimeMillis());
                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "Token JWT guardado exitosamente");
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error guardando token JWT: " + throwable.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error en saveToken: " + e.getMessage());
        }
    }

    public String getToken() {
        try {
            CompletableFuture<String> future = new CompletableFuture<>();

            dataStore.data()
                    .map(prefs -> prefs.get(KEY_TOKEN))
                    .first("")
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            new Consumer<String>() {
                                @Override
                                public void accept(String token) throws Throwable {
                                    future.complete(token != null && !token.isEmpty() ? token : null);
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Throwable {
                                    Log.e(TAG, "Error obteniendo token JWT: " + throwable.getMessage());
                                    future.complete(null);
                                }
                            }
                    );

            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Error en getToken: " + e.getMessage());
            return null;
        }
    }

    public void clearToken() {
        try {
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.remove(KEY_TOKEN);
                mutablePreferences.remove(KEY_SESSION_TIME);
                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "Token JWT eliminado exitosamente");
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error eliminando token JWT: " + throwable.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error en clearToken: " + e.getMessage());
        }
    }

    public void saveUserId(Long id) {
        try {
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                if (id == null) {
                    mutablePreferences.remove(KEY_USER_ID);
                } else {
                    mutablePreferences.set(KEY_USER_ID, id);
                }
                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "User ID guardado: " + (id != null ? id.toString() : "eliminado"));
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error guardando User ID: " + throwable.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error en saveUserId: " + e.getMessage());
        }
    }

    public Long getUserId() {
        try {
            CompletableFuture<Long> future = new CompletableFuture<>();

            dataStore.data()
                    .map(prefs -> prefs.get(KEY_USER_ID))
                    .first(null)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            new Consumer<Long>() {
                                @Override
                                public void accept(Long userId) throws Throwable {
                                    future.complete(userId);
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Throwable {
                                    Log.e(TAG, "Error obteniendo User ID: " + throwable.getMessage());
                                    future.complete(null);
                                }
                            }
                    );

            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Error en getUserId: " + e.getMessage());
            return null;
        }
    }

    // Método adicional para verificar si hay una sesión activa
    public boolean hasActiveSession() {
        String token = getToken();
        boolean hasToken = token != null && !token.trim().isEmpty();

        if (hasToken) {
            Log.d(TAG, "Sesión activa encontrada");
        } else {
            Log.d(TAG, "No hay sesión activa");
        }

        return hasToken;
    }

    // Método para obtener el tiempo de inicio de sesión
    public long getSessionTime() {
        try {
            CompletableFuture<Long> future = new CompletableFuture<>();

            dataStore.data()
                    .map(prefs -> prefs.get(KEY_SESSION_TIME))
                    .first(0L)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            new Consumer<Long>() {
                                @Override
                                public void accept(Long sessionTime) throws Throwable {
                                    future.complete(sessionTime != null ? sessionTime : 0L);
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Throwable {
                                    Log.e(TAG, "Error obteniendo session time: " + throwable.getMessage());
                                    future.complete(0L);
                                }
                            }
                    );

            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Error en getSessionTime: " + e.getMessage());
            return 0L;
        }
    }

    // Método para verificar si la sesión ha expirado
    public boolean isSessionExpired(long maxSessionTimeMillis) {
        if (!hasActiveSession()) {
            return true;
        }

        long sessionTime = getSessionTime();
        if (sessionTime == 0) {
            return false; // No se puede determinar, asumir que no ha expirado
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - sessionTime) > maxSessionTimeMillis;
    }

    // Método para limpiar toda la sesión
    public void clearSession() {
        try {
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.remove(KEY_TOKEN);
                mutablePreferences.remove(KEY_USER_ID);
                mutablePreferences.remove(KEY_SESSION_TIME);
                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "Sesión completa eliminada exitosamente");
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error eliminando sesión completa: " + throwable.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error en clearSession: " + e.getMessage());
        }
    }

    // Método para obtener información de la sesión (debug)
    public String getSessionInfo() {
        if (!hasActiveSession()) {
            return "No hay sesión activa";
        }

        Long userId = getUserId();
        long sessionTime = getSessionTime();

        StringBuilder info = new StringBuilder("Sesión activa");

        if (userId != null) {
            info.append(" - User ID: ").append(userId);
        }

        if (sessionTime > 0) {
            long sessionDuration = System.currentTimeMillis() - sessionTime;
            long minutes = sessionDuration / (1000 * 60);
            info.append(" - Duración: ").append(minutes).append(" min");
        }

        return info.toString();
    }

    // Método para observar cambios en el token (útil para reactividad)
    public Flowable<String> observeToken() {
        return dataStore.data()
                .map(prefs -> prefs.get(KEY_TOKEN))
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io());
    }

    // Método para observar el estado de la sesión
    public Flowable<Boolean> observeSessionState() {
        return observeToken()
                .map(token -> token != null && !token.trim().isEmpty())
                .distinctUntilChanged();
    }
}