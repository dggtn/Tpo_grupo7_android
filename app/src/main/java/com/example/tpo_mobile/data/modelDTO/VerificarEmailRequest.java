package com.example.tpo_mobile.data.modelDTO;
public class VerificarEmailRequest {
    private String email;

    public VerificarEmailRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}