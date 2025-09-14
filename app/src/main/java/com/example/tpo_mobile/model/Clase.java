package com.example.tpo_mobile.model;

public class Clase {
    private String nombre;
    private String profesor;

    public Clase(String nombre) {
        this.nombre = nombre;
        this.profesor = "";
    }

    public Clase(String nombre, String profesor) {
        this.nombre = nombre;
        this.profesor = profesor;
    }

    public String getName() {
        return nombre;
    }
}
