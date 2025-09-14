package com.example.tpo_mobile.network;

import com.example.tpo_mobile.utils.TokenManager;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    @Inject
    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // No agregar token a endpoints de autenticación
        String url = originalRequest.url().toString();
        if (url.contains("/auth/")) {
            return chain.proceed(originalRequest);
        }

        String token = tokenManager.getToken();
        if (token != null) {
            Request authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(authenticatedRequest);
        }

        return chain.proceed(originalRequest);
    }
}