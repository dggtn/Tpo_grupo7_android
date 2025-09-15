package com.example.tpo_mobile.data.api;

import com.example.tpo_mobile.data.modelDTO.AuthRequest;
import com.example.tpo_mobile.data.modelDTO.AuthResponse;
import com.example.tpo_mobile.data.modelDTO.RegisterRequest;
import com.example.tpo_mobile.data.modelDTO.VerificationRequest;
import com.example.tpo_mobile.data.modelDTO.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("auth/iniciar-registro")
    Call<ApiResponse<String>> iniciarRegistro(@Body RegisterRequest request);

    @POST("auth/finalizar-registro")
    Call<ApiResponse<String>> finalizarRegistro(@Body VerificationRequest request);

    @POST("auth/authenticate")
    Call<ApiResponse<AuthResponse>> authenticate(@Body AuthRequest request);
}