package com.example.tpo_mobile.model;

import com.example.tpo_mobile.data.modelDTO.ShiftDTO;

import java.util.List;

public class Clase {
    private long id;
    private String nombre;
    private String fechaInicio;
    private String fechaFin;
    private int length;
    private double  price;
    private List<?> sedes;
    private List<?> teachers;
    private List<ShiftDTO> shifts;


    public Clase(long id, String nombre, String fechaInicio, String fechaFin, int length, double price) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.length = length;
        this.price = price;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public List<ShiftDTO> getShifts() { return shifts; }
    public void setShifts(List<ShiftDTO> shifts) { this.shifts = shifts; }
}
