package com.example.tpo_mobile.repository;

public interface SimpleCallback<T> {
    void onSuccess(T data);
    void onError(Throwable error);
}
