package com.example.tpo_mobile.Repository;

import com.example.tpo_mobile.data.api.GymApiService;
import com.example.tpo_mobile.data.model.ClaseDTO;
import com.example.tpo_mobile.model.Clase;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class GymRetrofitRepository implements ClasesRepository{

    private final GymApiService api;

    @Inject
    public GymRetrofitRepository(GymApiService api) {
        this.api = api;
    }
    @Override
    public void getAllClases(ClasesServiceCallBack callback) {
        this.api.obtenerClases().enqueue(new Callback<List<ClaseDTO>>() {
            @Override
            public void onResponse(Call<List<ClaseDTO>> call, Response<List<ClaseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ClaseDTO> clases = response.body();
                    callback.onSuccess(
                    clases.stream().map((dto) -> new Clase(dto.getNombre()))
                            .collect(Collectors.toList()));
                }
            }

            @Override
            public void onFailure(Call<List<ClaseDTO>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    @Override
    public void getClasesByName(String name, ClasesServiceCallBack callback) {

    }
}
