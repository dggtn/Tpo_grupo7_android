package com.example.tpo_mobile.Repository;

public interface ClasesRepository {
    void getAllClases(ClasesServiceCallBack callback);
    void getClasesByName(String name,ClasesServiceCallBack callback);
}


