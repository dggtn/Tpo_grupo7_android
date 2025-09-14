package com.example.tpo_mobile.data.modelDTO;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}