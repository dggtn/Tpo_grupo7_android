package com.example.tpo_mobile.services;

import com.example.tpo_mobile.Repository.GetAllClasesCallback;
import com.example.tpo_mobile.Repository.GetUserCallback;
import com.example.tpo_mobile.Repository.GymRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GymServiceImpl implements GymService {

    private GymRepository gymRepository;

    @Inject
    public GymServiceImpl(GymRepository  clasesRepository) {
        this. gymRepository = clasesRepository;
    }

    @Override
    public void getAllClases(GetAllClasesCallback callback) {
        this.gymRepository.getAllClases(callback);
    }

    @Override
    public void getUser(GetUserCallback callback) {
        this.gymRepository.getUser(callback);
    }

    //@Override
    public void getClaseByName(String name, GetAllClasesCallback callback) {

    }
}
