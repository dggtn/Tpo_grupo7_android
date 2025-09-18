package com.example.tpo_mobile.data.api;

import com.example.tpo_mobile.data.modelDTO.AuthRequest;
import com.example.tpo_mobile.data.modelDTO.AuthResponse;
import com.example.tpo_mobile.data.modelDTO.RegisterRequest;
import com.example.tpo_mobile.data.modelDTO.VerificationRequest;
import com.example.tpo_mobile.data.modelDTO.ApiResponse;
import com.example.tpo_mobile.data.modelDTO.ReenviarCodigoRequest;
import com.example.tpo_mobile.data.modelDTO.VerificarEmailRequest;

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

    @POST("auth/logout")
    Call<ApiResponse<String>> logout();

    // NUEVOS ENDPOINTS PARA RECUPERACIÓN DE ACCESO
    @POST("auth/verificar-email-pendiente")
    Call<ApiResponse<String>> verificarEmailPendiente(@Body VerificarEmailRequest request);

    @POST("auth/reenviar-codigo")
    Call<ApiResponse<String>> reenviarCodigo(@Body ReenviarCodigoRequest request);
}