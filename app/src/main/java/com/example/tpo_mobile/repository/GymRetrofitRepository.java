package com.example.tpo_mobile.repository;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.tpo_mobile.data.api.GymApiService;
import com.example.tpo_mobile.data.modelDTO.ApiResponse;
import com.example.tpo_mobile.data.modelDTO.UserDTO;
import com.example.tpo_mobile.model.User;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class GymRetrofitRepository implements GymRepository {

    private final GymApiService api;

    @Inject
    public GymRetrofitRepository(GymApiService api) {
        this.api = api;
    }


    @Override
    public void getClasesByName(String name, GetAllClasesCallback callback) {

    }

    @Override
    public void getUser(GetUserCallback callback) {
        Log.d(TAG, "Obteniendo datos del usuario actual");

        // Intentamos obtener el usuario actual autenticado
        this.api.obtenerUserActual().enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                Log.d(TAG, "User response received: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserDTO> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        UserDTO userDTO = apiResponse.getData();
                        Log.d(TAG, "Usuario recibido: " + userDTO.getEmail());

                        // Convertir DTO a modelo de dominio
                        User user = convertToUser(userDTO);
                        callback.onSuccess(user);
                    } else {
                        String error = apiResponse.getError() != null ?
                                apiResponse.getError() : "Error al obtener usuario";
                        Log.e(TAG, "Error en respuesta del servidor: " + error);
                        callback.onError(new RuntimeException(error));
                    }
                } else {
                    // Si falla obtener usuario actual, intentamos obtener lista de usuarios
                    // (fallback para compatibilidad)
                    Log.w(TAG, "No se pudo obtener usuario actual, intentando lista de usuarios");
                    getUserFromList(callback);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al obtener usuario: " + t.getMessage(), t);
                // Fallback: intentar obtener de lista de usuarios
                getUserFromList(callback);
            }
        });
    }

    /**
     * Método de fallback para obtener usuario desde la lista de usuarios
     */
    private void getUserFromList(GetUserCallback callback) {
        Log.d(TAG, "Intentando obtener usuario desde lista de usuarios");

        this.api.obtenerUsers().enqueue(new Callback<ApiResponse<List<UserDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserDTO>>> call, Response<ApiResponse<List<UserDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<UserDTO>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        // Tomar el primer usuario como fallback
                        UserDTO userDTO = apiResponse.getData().get(0);
                        User user = convertToUser(userDTO);
                        Log.d(TAG, "Usuario fallback obtenido: " + user.getEmail());
                        callback.onSuccess(user);
                    } else {
                        String error = "No se encontraron usuarios";
                        Log.e(TAG, error);
                        callback.onError(new RuntimeException(error));
                    }
                } else {
                    String error = "Error al obtener lista de usuarios: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(new RuntimeException(error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserDTO>>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al obtener lista de usuarios: " + t.getMessage(), t);
                callback.onError(t);
            }
        });
    }

    /**
     * Convierte un UserDTO a un objeto User del modelo de dominio
     */
    private User convertToUser(UserDTO dto) {
        if (dto == null) {
            return new User("usuario@ejemplo.com", "Usuario Desconocido");
        }

        String email = dto.getEmail() != null ? dto.getEmail() : "usuario@ejemplo.com";
        String name = dto.getFullName(); // Usa el método que obtiene nombre completo

        return new User(email, name);
    }


}
