package com.example.tpo_mobile.Repository;

public interface GymRepository {
    void getAllClases(GetAllClasesCallback callback);
    void getClasesByName(String name, GetAllClasesCallback callback);

    void getUser(GetUserCallback callback);


}


