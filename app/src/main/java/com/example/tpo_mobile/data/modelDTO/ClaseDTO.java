package com.example.tpo_mobile.data.modelDTO;

import java.util.List;

public class ClaseDTO {
    private long id;
    private String name;
    private String fechaInicio;
    private String fechaFin;
    private int length;
    private double  price;
    private List<?> sedes;
    private List<?> teachers;
    private List<ShiftDTO> shifts;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ClaseDTO(String name, String fechaInicio, String fechaFin, int length, double price, List<?> teachers) {
        this.name = name;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.length = length;
        this.price = price;
        this.teachers = teachers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<?> getSedes() {
        return sedes;
    }

    public void setSedes(List<?> sedes) {
        this.sedes = sedes;
    }

    public List<?> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<?> teachers) {
        this.teachers = teachers;
    }

    public List<ShiftDTO> getShifts() { return shifts; }
    public void setShifts(List<ShiftDTO> shifts) { this.shifts = shifts; }

}
