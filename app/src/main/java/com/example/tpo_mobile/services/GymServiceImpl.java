package com.example.tpo_mobile.services;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.tpo_mobile.data.modelDTO.ClaseDTO;
import com.example.tpo_mobile.data.modelDTO.ReservationDTO;
import com.example.tpo_mobile.data.modelDTO.ReservationStatusDTO;
import com.example.tpo_mobile.data.modelDTO.UserDTO;
import com.example.tpo_mobile.repository.GetAllClasesCallback;
import com.example.tpo_mobile.repository.GetClaseByIdCallback;
import com.example.tpo_mobile.repository.GetUserCallback;
import com.example.tpo_mobile.repository.GymRepository;
import com.example.tpo_mobile.repository.SimpleCallback;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GymServiceImpl implements GymService {

    private final GymRepository gymRepository;

    @Inject
    public GymServiceImpl(GymRepository  clasesRepository) {
        this. gymRepository = clasesRepository;
    }

    @Override
    public void getAllClases(GetAllClasesCallback callback) {
        this.gymRepository.getClases(callback);

    }

    @Override
    public void getUser(GetUserCallback callback) {
        Log.d(TAG, "Solicitando datos del usuario");

        this.gymRepository.getUser(callback);
    }

    @Override
    public void getClasePorId(Long id, GetClaseByIdCallback callback) {
        Log.d(TAG, "Buscando curso por id:" + id);
        this.gymRepository.obtenerClaseById(id, callback);
    }

    @Override
    public void reservar(Long shiftId, SimpleCallback<String> cb) {
        gymRepository.reservar(shiftId, cb);
    }
    @Override
    public void cancelarReserva(Long shiftId, SimpleCallback<String> cb) {
        gymRepository.cancelarReserva(shiftId, cb);
    }
    @Override
    public void getMisReservas(SimpleCallback<List<ReservationDTO>> cb) {
        gymRepository.getMisReservas(cb);
    }

    @Override
    public void getClaseDtoPorId(Long id, SimpleCallback<ClaseDTO> callback) {
        gymRepository.getClaseDtoPorId(id, callback);
    }

    @Override
    public void getProximasReservas(SimpleCallback<List<ReservationStatusDTO>> cb) {
        gymRepository.getProximasReservas(cb);
    }

    @Override
    public void actualizarUsuario(UserDTO user, SimpleCallback<UserDTO> callback) {
        gymRepository.actualizarUsuario(user, callback);
    }
}
