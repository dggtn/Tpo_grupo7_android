package com.example.tpo_mobile.data.modelDTO;

public class ShiftDTO {
    private Long id;
    private int diaEnQueSeDicta;   // 1=Lun ... 7=Dom
    private String horaInicio;     // "HH:mm"
    private String horaFin;        // "HH:mm"
    private int vacancy;           // cupos disponibles

    // Relaciones
    private Long claseId;
    private Long headquarterId;
    private Long teacherId;

    // ====== Getters y setters ======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getDiaEnQueSeDicta() { return diaEnQueSeDicta; }
    public void setDiaEnQueSeDicta(int diaEnQueSeDicta) { this.diaEnQueSeDicta = diaEnQueSeDicta; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public int getVacancy() { return vacancy; }
    public void setVacancy(int vacancy) { this.vacancy = vacancy; }

    public Long getClaseId() { return claseId; }
    public void setClaseId(Long claseId) { this.claseId = claseId; }

    public Long getHeadquarterId() { return headquarterId; }
    public void setHeadquarterId(Long headquarterId) { this.headquarterId = headquarterId; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}
