package com.example.tpo_mobile.services;

import com.example.tpo_mobile.Repository.GetAllClasesCallback;
import com.example.tpo_mobile.Repository.GetUserCallback;

public interface GymService {
    void getAllClases(GetAllClasesCallback callback);
    void getUser(GetUserCallback callback);
    //void getClaseByName(String name, ClasesServiceCallBack callback);
}
