package com.example.tpo_mobile.repository;

import com.example.tpo_mobile.model.User;

public interface GetUserCallback {
    void onSuccess(User users);
    void onError(Throwable error);



}
