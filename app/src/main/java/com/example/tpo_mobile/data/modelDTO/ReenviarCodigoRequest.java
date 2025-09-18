package com.example.tpo_mobile.data.modelDTO;

public class ReenviarCodigoRequest {
    private String email;

    public ReenviarCodigoRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

