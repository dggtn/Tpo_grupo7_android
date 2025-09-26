package com.example.tpo_mobile.repository;

public interface GymRepository {


    void getUser(GetUserCallback callback);

    void getClases( GetAllClasesCallback callback);

    void obtenerClaseById(long id , GetClaseByIdCallback callback);


}


