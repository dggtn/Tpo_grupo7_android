package com.example.tpo_mobile.data.modelDTO;

public class ReservationStatusDTO {
    private Long reservationId;
    private Long shiftId;
    private String nombreCurso;
    private String diaClase;         // "Lunes", "Martes", etc.
    private String horaClase;        // "HH:mm"
    private String fechaExpiracion;  // ISO-8601 del back o timestamp

    private String estadoReserva; // ACTIVA, CANCELADA, EXPIRADA
    private boolean cancelable;
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getNombreCurso() { return nombreCurso; }
    public void setNombreCurso(String nombreCurso) { this.nombreCurso = nombreCurso; }
    public String getDiaClase() { return diaClase; }
    public void setDiaClase(String diaClase) { this.diaClase = diaClase; }
    public String getHoraClase() { return horaClase; }
    public void setHoraClase(String horaClase) { this.horaClase = horaClase; }
    public String getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(String fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public String getEstadoReserva() { return estadoReserva; }
    public void setEstadoReserva(String estado) { this.estadoReserva = estado; }
    public void setCancelable(boolean cancelable) { this.cancelable = cancelable; }
    public boolean isCancelable() { return cancelable; }
}
