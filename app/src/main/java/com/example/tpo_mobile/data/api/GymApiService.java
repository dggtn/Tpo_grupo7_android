package com.example.tpo_mobile.data.api;

import com.example.tpo_mobile.data.modelDTO.ApiResponse;
import com.example.tpo_mobile.data.modelDTO.ClaseDTO;
import com.example.tpo_mobile.data.modelDTO.UserDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface GymApiService {

//    @GET("shifts/available")
//    Call<ApiResponse<List<ClaseDTO>>> obtenerClases();

    @GET("users")
    Call<ApiResponse<List<UserDTO>>> obtenerUsers();

    // Método para obtener el usuario autenticado actual
    @GET("users/me")
    Call<ApiResponse<UserDTO>> obtenerUserActual();
    @GET("courses/allCourses")
    Call<ApiResponse<List<ClaseDTO>>> obtenerClases();

    @GET("courses/{id}")
    Call<ApiResponse<ClaseDTO>> obtenerClaseById(@Path("id") Long claseId);

    @PUT("users/name")
    Call<ApiResponse<UserDTO>> actualizarUsuario(@Body UserDTO usuario);




}