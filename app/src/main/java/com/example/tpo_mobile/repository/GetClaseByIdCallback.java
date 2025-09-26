package com.example.tpo_mobile.repository;

import com.example.tpo_mobile.model.Clase;

public interface GetClaseByIdCallback {


    void onSuccess(Clase clase);


    void onError(Throwable error);
}
