package com.example.tpo_mobile.services;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.tpo_mobile.repository.GetAllClasesCallback;
import com.example.tpo_mobile.repository.GetClaseByIdCallback;
import com.example.tpo_mobile.repository.GetUserCallback;
import com.example.tpo_mobile.repository.GymRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GymServiceImpl implements GymService {

    private final GymRepository gymRepository;

    @Inject
    public GymServiceImpl(GymRepository  clasesRepository) {
        this. gymRepository = clasesRepository;
    }

    @Override
    public void getAllClases(GetAllClasesCallback callback) {
        this.gymRepository.getClases(callback);

    }

    @Override
    public void getUser(GetUserCallback callback) {
        Log.d(TAG, "Solicitando datos del usuario");

        this.gymRepository.getUser(callback);
    }

    @Override
    public void getClasePorId(Long id, GetClaseByIdCallback callback) {
        Log.d(TAG, "Buscando curso por id:" + id);
        this.gymRepository.obtenerClaseById(id, callback);
    }
}
