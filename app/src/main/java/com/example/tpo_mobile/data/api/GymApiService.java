package com.example.tpo_mobile.data.api;

import com.example.tpo_mobile.data.model.ClaseDTO;
import com.example.tpo_mobile.data.model.UserDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GymApiService {
    @GET("clases")
    Call<List<ClaseDTO>> obtenerClases();



    @GET("user")
    Call<UserDTO> obtenerUser();
}
