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
public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String DATASTORE_NAME = "auth_preferences";

    // Keys para DataStore
    private static final Preferences.Key<String> TOKEN_KEY = PreferencesKeys.stringKey("access_token");
    private static final Preferences.Key<String> EMAIL_KEY = PreferencesKeys.stringKey("user_email");
    private static final Preferences.Key<Long> LOGIN_TIME_KEY = PreferencesKeys.longKey("login_time");
    private static final Preferences.Key<String> USER_NAME_KEY = PreferencesKeys.stringKey("user_name");

    private final RxDataStore<Preferences> dataStore;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.dataStore = new RxPreferenceDataStoreBuilder(context, DATASTORE_NAME).build();
    }

    public void saveToken(String token) {
        try {
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.set(TOKEN_KEY, token);
                mutablePreferences.set(LOGIN_TIME_KEY, System.currentTimeMillis());
                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "Token guardado exitosamente");
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error guardando token: " + throwable.getMessage());
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
                    .map(prefs -> prefs.get(TOKEN_KEY))
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
                                    Log.e(TAG, "Error obteniendo token: " + throwable.getMessage());
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

    public void saveUserEmail(String email) {
        try {
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.set(EMAIL_KEY, email);
                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "Email de usuario guardado: " + email);
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error guardando email: " + throwable.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error en saveUserEmail: " + e.getMessage());
        }
    }

    public String getUserEmail() {
        try {
            CompletableFuture<String> future = new CompletableFuture<>();

            dataStore.data()
                    .map(prefs -> prefs.get(EMAIL_KEY))
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
                                    Log.e(TAG, "Error obteniendo email: " + throwable.getMessage());
                                    future.complete(null);
                                }
                            }
                    );

            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Error en getUserEmail: " + e.getMessage());
            return null;
        }
    }

    public void saveUserName(String name) {
        try {
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.set(USER_NAME_KEY, name);
                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "Nombre de usuario guardado: " + name);
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error guardando nombre: " + throwable.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error en saveUserName: " + e.getMessage());
        }
    }

    public String getUserName() {
        try {
            CompletableFuture<String> future = new CompletableFuture<>();

            dataStore.data()
                    .map(prefs -> prefs.get(USER_NAME_KEY))
                    .first("")
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            new Consumer<String>() {
                                @Override
                                public void accept(String name) throws Throwable {
                                    future.complete(name != null && !name.isEmpty() ? name : null);
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Throwable {
                                    Log.e(TAG, "Error obteniendo nombre: " + throwable.getMessage());
                                    future.complete(null);
                                }
                            }
                    );

            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Error en getUserName: " + e.getMessage());
            return null;
        }
    }

    public long getLoginTime() {
        try {
            CompletableFuture<Long> future = new CompletableFuture<>();

            dataStore.data()
                    .map(prefs -> prefs.get(LOGIN_TIME_KEY))
                    .first(0L)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            new Consumer<Long>() {
                                @Override
                                public void accept(Long time) throws Throwable {
                                    future.complete(time != null ? time : 0L);
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Throwable {
                                    Log.e(TAG, "Error obteniendo tiempo de login: " + throwable.getMessage());
                                    future.complete(0L);
                                }
                            }
                    );

            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Error en getLoginTime: " + e.getMessage());
            return 0L;
        }
    }

    public boolean isLoggedIn() {
        String token = getToken();
        boolean hasToken = token != null && !token.trim().isEmpty();

        if (hasToken) {
            Log.d(TAG, "Usuario logueado - Token presente");
        } else {
            Log.d(TAG, "Usuario no logueado - No hay token");
        }

        return hasToken;
    }

    public void clearToken() {
        String email = getUserEmail(); // Guardar para log

        try {
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.remove(TOKEN_KEY);
                mutablePreferences.remove(EMAIL_KEY);
                mutablePreferences.remove(LOGIN_TIME_KEY);
                mutablePreferences.remove(USER_NAME_KEY);
                return Single.just(mutablePreferences);
            }).subscribe(
                    new Consumer<Preferences>() {
                        @Override
                        public void accept(Preferences preferences) throws Throwable {
                            Log.d(TAG, "Sesión cerrada para usuario: " + (email != null ? email : "desconocido"));
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            Log.e(TAG, "Error limpiando token: " + throwable.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error en clearToken: " + e.getMessage());
        }
    }

    // Método para verificar si la sesión ha expirado (opcional)
    public boolean isSessionExpired(long maxSessionTimeMillis) {
        if (!isLoggedIn()) {
            return true;
        }

        long loginTime = getLoginTime();
        if (loginTime == 0) {
            return false; // No se puede determinar, asumir que no ha expirado
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - loginTime) > maxSessionTimeMillis;
    }

    // Metodo para obtener información básica del usuario logueado
    public String getUserInfo() {
        if (!isLoggedIn()) {
            return "Usuario no logueado";
        }

        String email = getUserEmail();
        String name = getUserName();
        long loginTime = getLoginTime();

        StringBuilder info = new StringBuilder();
        if (name != null) {
            info.append("Usuario: ").append(name);
        } else if (email != null) {
            info.append("Email: ").append(email);
        } else {
            info.append("Usuario autenticado");
        }

        if (loginTime > 0) {
            long sessionDuration = System.currentTimeMillis() - loginTime;
            long minutes = sessionDuration / (1000 * 60);
            info.append(" (sesión: ").append(minutes).append(" min)");
        }

        return info.toString();
    }

    // Metodo para observar cambios en el token
    public Flowable<String> observeToken() {
        return dataStore.data()
                .map(prefs -> prefs.get(TOKEN_KEY))
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io());
    }

    // Metodo para observar el estado de login
    public Flowable<Boolean> observeLoginState() {
        return observeToken()
                .map(token -> token != null && !token.trim().isEmpty())
                .distinctUntilChanged();
    }
}