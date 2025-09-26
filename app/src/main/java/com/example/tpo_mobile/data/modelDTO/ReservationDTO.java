package com.example.tpo_mobile.data.modelDTO;

public class ReservationDTO {
    private Long id;
    private Long idUser;
    private Long idShift;
    private String metodoDePago;
    private String expiryDate;

    public ReservationDTO() {}

    public ReservationDTO(Long idUser, Long idShift) {
        this.idUser = idUser;
        this.idShift = idShift;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    public Long getIdShift() { return idShift; }
    public void setIdShift(Long idShift) { this.idShift = idShift; }

    public String getMetodoDePago() { return metodoDePago; }
    public void setMetodoDePago(String metodoDePago) { this.metodoDePago = metodoDePago; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}
