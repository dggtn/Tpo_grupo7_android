package com.example.tpo_mobile.services;

import com.example.tpo_mobile.Repository.ClasesRepository;
import com.example.tpo_mobile.Repository.ClasesServiceCallBack;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClaseServiceImpl implements ClaseService {

    private ClasesRepository clasesRepository;

    @Inject
    public ClaseServiceImpl(ClasesRepository clasesRepository) {
        this.clasesRepository = clasesRepository;
    }

    @Override
    public void getAllClases(ClasesServiceCallBack callback) {
        this.clasesRepository.getAllClases(callback);
    }

    //@Override
    public void getClaseByName(String name, ClasesServiceCallBack callback) {

    }
}
