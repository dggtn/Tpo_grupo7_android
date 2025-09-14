package com.example.tpo_mobile.data.modelDTO;

public class ApiResponse<T> {
    private Boolean ok;
    private String error;
    private T data;

    public Boolean getOk() { return ok; }
    public void setOk(Boolean ok) { this.ok = ok; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public boolean isSuccess() {
        return ok != null && ok;
    }
}