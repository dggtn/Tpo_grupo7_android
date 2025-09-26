package com.example.tpo_mobile.data.api;

import com.example.tpo_mobile.data.modelDTO.ApiResponse;
import com.example.tpo_mobile.data.modelDTO.ReservationDTO;
import com.example.tpo_mobile.data.modelDTO.ReservationStatusDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReservationApiService {

    @POST("reservations/reservar")
    Call<ApiResponse<String>> reservar(@Body ReservationDTO dto);

    @DELETE("reservations/cancelar/{shiftId}")
    Call<ApiResponse<String>> cancelar(@Path("shiftId") Long shiftId);

    @GET("reservations")
    Call<ApiResponse<List<ReservationDTO>>> misReservas();

    @GET("reservations/status")
    Call<ApiResponse<List<ReservationStatusDTO>>> getProximas();

}
