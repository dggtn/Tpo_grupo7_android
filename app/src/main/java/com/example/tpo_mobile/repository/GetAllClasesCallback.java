package com.example.tpo_mobile.repository;

import com.example.tpo_mobile.model.Clase;

import java.util.List;

public interface GetAllClasesCallback {
    void onSuccess(List<Clase> clases);
    void onError(Throwable error);



}
