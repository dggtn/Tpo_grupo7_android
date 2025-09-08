package com.example.tpo_mobile.Repository;

import com.example.tpo_mobile.model.Clase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClasesRepositoryMemory implements ClasesRepository {
    private final List<Clase> clases;

    @Inject
    public ClasesRepositoryMemory() {
        this.clases = new ArrayList<>();
        initializeClases();
    }

    private void initializeClases() {
        clases.add(new Clase("Pilates","Eduardo"));
        clases.add(new Clase("Box","Juan"));
        clases.add(new Clase("Zumba","Erika"));
        clases.add(new Clase("Pesas","Maria"));

    }



    @Override
    public void getAllClases(ClasesServiceCallBack callback) {
        callback.onSuccess(clases);
    }

    @Override
    public void getClasesByName(String name, ClasesServiceCallBack callback) {
        callback.onSuccess(clases);
    }
}