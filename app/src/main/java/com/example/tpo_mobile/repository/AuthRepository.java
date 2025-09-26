package com.example.tpo_mobile.repository;

import com.example.tpo_mobile.data.api.AuthApiService;
import com.example.tpo_mobile.data.modelDTO.AuthRequest;
import com.example.tpo_mobile.data.modelDTO.AuthResponse;
import com.example.tpo_mobile.data.modelDTO.RegisterRequest;
import com.example.tpo_mobile.data.modelDTO.VerificationRequest;
import com.example.tpo_mobile.data.modelDTO.ApiResponse;
import com.example.tpo_mobile.data.modelDTO.ReenviarCodigoRequest;
import com.example.tpo_mobile.data.modelDTO.VerificarEmailRequest;
import com.example.tpo_mobile.utils.TokenManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AuthRepository {

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    private final AuthApiService authApiService;
    private final TokenManager tokenManager;

    @Inject
    public AuthRepository(AuthApiService authApiService, TokenManager tokenManager) {
        this.authApiService = authApiService;
        this.tokenManager = tokenManager;
    }

    public void iniciarRegistro(RegisterRequest request, AuthCallback<String> callback) {
        authApiService.iniciarRegistro(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getError() != null ? apiResponse.getError() : "Error desconocido");
                    }
                } else {
                    String error = "Error en el registro";
                    if (response.errorBody() != null) {
                        try {
                            error = response.errorBody().string();
                        } catch (Exception e) {
                            // Usar error por defecto
                        }
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    public void finalizarRegistro(VerificationRequest request, AuthCallback<String> callback) {
        authApiService.finalizarRegistro(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getError() != null ? apiResponse.getError() : "Error desconocido");
                    }
                } else {
                    String error = "Error en la verificación";
                    if (response.errorBody() != null) {
                        try {
                            error = response.errorBody().string();
                        } catch (Exception e) {
                            // Usar error por defecto
                        }
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    // NUEVO: Verificar si existe un registro pendiente
    public void verificarEmailPendiente(String email, AuthCallback<String> callback) {
        VerificarEmailRequest request = new VerificarEmailRequest(email);
        authApiService.verificarEmailPendiente(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getError() != null ? apiResponse.getError() : "No hay registro pendiente para este email");
                    }
                } else {
                    callback.onError("No hay registro pendiente para este email");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    // NUEVO: Reenviar código de verificación
    public void reenviarCodigo(String email, AuthCallback<String> callback) {
        ReenviarCodigoRequest request = new ReenviarCodigoRequest(email);
        authApiService.reenviarCodigo(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getError() != null ? apiResponse.getError() : "Error al reenviar código");
                    }
                } else {
                    String error = "Error al reenviar código";
                    if (response.errorBody() != null) {
                        try {
                            error = response.errorBody().string();
                        } catch (Exception e) {
                            // Usar error por defecto
                        }
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    public void authenticate(AuthRequest request, AuthCallback<AuthResponse> callback) {
        authApiService.authenticate(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse data = apiResponse.getData();

                        // --- GUARDAR CREDENCIALES EN TOKENMANAGER ---
                        // Ajustá getters según tu AuthResponse (token/email/nombre/userId)
                        String token  = data.getAccessToken();     // p.ej. "accessToken" o "jwt"
                        if (token != null && !token.trim().isEmpty()) {
                            tokenManager.saveToken(token);
                        }
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getError() != null ? apiResponse.getError() : "Error desconocido");
                    }
                } else {
                    String error = "Credenciales inválidas";
                    if (response.errorBody() != null) {
                        try {
                            error = response.errorBody().string();
                        } catch (Exception e) {
                            // Usar error por defecto
                        }
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    public void logout(AuthCallback<String> callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("No hay sesión activa");
            return;
        }

        authApiService.logout().enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                // Independientemente de la respuesta del servidor, limpiar datos locales
                tokenManager.clearToken();

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData() != null ?
                                apiResponse.getData() : "Sesión cerrada exitosamente");
                    } else {
                        callback.onSuccess("Sesión cerrada localmente"); // Aún así es exitoso localmente
                    }
                } else {
                    callback.onSuccess("Sesión cerrada localmente"); // Aún así es exitoso localmente
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // Aunque falle la comunicación con el servidor, limpiar datos locales
                tokenManager.clearToken();
                callback.onSuccess("Sesión cerrada localmente");
            }
        });
    }

    // Método para logout sin comunicación con el servidor (más rápido)
    public void logoutLocal() {
        tokenManager.clearToken();
    }
}