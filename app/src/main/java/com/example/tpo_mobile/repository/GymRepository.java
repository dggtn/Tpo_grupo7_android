package com.example.tpo_mobile.repository;

import com.example.tpo_mobile.data.modelDTO.ClaseDTO;
import com.example.tpo_mobile.data.modelDTO.ReservationDTO;
import com.example.tpo_mobile.data.modelDTO.ReservationStatusDTO;
import com.example.tpo_mobile.data.modelDTO.UserDTO;

import java.util.List;

public interface GymRepository {

    void getUser(GetUserCallback callback);
    void getClases(GetAllClasesCallback callback);
    void obtenerClaseById(long id, GetClaseByIdCallback callback);

    void reservar(Long shiftId, SimpleCallback<String> callback);
    void cancelarReserva(Long shiftId, SimpleCallback<String> callback);
    void getMisReservas(SimpleCallback<List<ReservationDTO>> callback);
    void getClaseDtoPorId(Long id, SimpleCallback<ClaseDTO> callback);
    void getProximasReservas(SimpleCallback<List<ReservationStatusDTO>> cb);

    void actualizarUsuario(UserDTO user, SimpleCallback<UserDTO> callback);
}
