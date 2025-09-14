package com.example.tpo_mobile.repository;

import com.example.tpo_mobile.data.api.AuthApiService;
import com.example.tpo_mobile.data.modelDTO.AuthRequest;
import com.example.tpo_mobile.data.modelDTO.AuthResponse;
import com.example.tpo_mobile.data.modelDTO.RegisterRequest;
import com.example.tpo_mobile.data.modelDTO.VerificationRequest;
import com.example.tpo_mobile.data.modelDTO.ApiResponse;

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

    @Inject
    public AuthRepository(AuthApiService authApiService) {
        this.authApiService = authApiService;
    }

    public void iniciarRegistro(RegisterRequest request, AuthCallback<String> callback) {
        authApiService.iniciarRegistro(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
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
            public void onFailure(Call<String> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    public void finalizarRegistro(VerificationRequest request, AuthCallback<String> callback) {
        authApiService.finalizarRegistro(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
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
            public void onFailure(Call<String> call, Throwable t) {
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
}