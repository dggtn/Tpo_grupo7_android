package com.example.tpo_mobile.repository;

public interface GymRepository {

    void getClasesByName(String name, GetAllClasesCallback callback);

    void getUser(GetUserCallback callback);


}


