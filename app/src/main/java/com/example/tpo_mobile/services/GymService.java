package com.example.tpo_mobile.services;

import com.example.tpo_mobile.data.modelDTO.ClaseDTO;
import com.example.tpo_mobile.data.modelDTO.ReservationDTO;
import com.example.tpo_mobile.data.modelDTO.ReservationStatusDTO;
import com.example.tpo_mobile.data.modelDTO.UserDTO;
import com.example.tpo_mobile.repository.GetAllClasesCallback;
import com.example.tpo_mobile.repository.GetClaseByIdCallback;
import com.example.tpo_mobile.repository.GetUserCallback;
import com.example.tpo_mobile.repository.SimpleCallback;

import java.util.List;

public interface GymService {
    void getAllClases(GetAllClasesCallback callback);
    void getUser(GetUserCallback callback);
    void getClasePorId(Long id, GetClaseByIdCallback callback);
    void reservar(Long shiftId, SimpleCallback<String> callback);
    void cancelarReserva(Long shiftId, SimpleCallback<String> callback);
    void getMisReservas(SimpleCallback<List<ReservationDTO>> callback);

    void getClaseDtoPorId(Long id, SimpleCallback<ClaseDTO> cb);

    void getProximasReservas(SimpleCallback<List<ReservationStatusDTO>> callback);

    void actualizarUsuario(UserDTO user, SimpleCallback<UserDTO> callback);
}


