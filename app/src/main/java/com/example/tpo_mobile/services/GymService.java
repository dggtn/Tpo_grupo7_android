package com.example.tpo_mobile.services;

import com.example.tpo_mobile.repository.GetAllClasesCallback;
import com.example.tpo_mobile.repository.GetUserCallback;

public interface GymService {
    void getAllClases(GetAllClasesCallback callback);
    void getUser(GetUserCallback callback);

}
