package com.example.tpo_mobile.repository;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.tpo_mobile.data.api.GymApiService;
import com.example.tpo_mobile.data.api.ReservationApiService;
import com.example.tpo_mobile.data.modelDTO.ApiResponse;
import com.example.tpo_mobile.data.modelDTO.ClaseDTO;
import com.example.tpo_mobile.data.modelDTO.ReservationDTO;
import com.example.tpo_mobile.data.modelDTO.UserDTO;
import com.example.tpo_mobile.model.Clase;
import com.example.tpo_mobile.model.User;

import java.util.List;
import java.util.stream.Collectors;

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

    @Inject
    ReservationApiService reservationApi;


    @Override
    public void getClases( GetAllClasesCallback callback) {
        this.api.obtenerClases().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ClaseDTO>>> call, Response<ApiResponse<List<ClaseDTO>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ClaseDTO>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        List<ClaseDTO> clasesDTO = apiResponse.getData();
                        List<Clase> clases = clasesDTO.stream().map((claseDTO)->{
                            Clase clase = new Clase( claseDTO.getId() ,claseDTO.getName(),claseDTO.getFechaInicio(),claseDTO.getFechaFin(),claseDTO.getLength(),claseDTO.getPrice());
                            return clase;
                        }).collect(Collectors.toList());
                        Log.d(TAG, "se obtuvieron clases");
                        callback.onSuccess(clases);
                    } else {
                        String error = "No se encontraron clases";
                        Log.e(TAG, error);
                        callback.onError(new RuntimeException(error));
                    }
                } else {
                    String error = "Error al obtener lista de clases: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(new RuntimeException(error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ClaseDTO>>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al obtener lista de usuarios: " + t.getMessage(), t);
                callback.onError(t);
            }
        });
    }

    @Override
    public void obtenerClaseById(long id, GetClaseByIdCallback callback) {
        this.api.obtenerClaseById(id).enqueue(new Callback<ApiResponse<ClaseDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ClaseDTO>> call, Response<ApiResponse<ClaseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ClaseDTO> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ClaseDTO dto = apiResponse.getData();

                        // Mapeo DTO -> modelo
                        Clase clase = new Clase(
                                dto.getId(),
                                dto.getName(),
                                dto.getFechaInicio(),
                                dto.getFechaFin(),
                                dto.getLength(),
                                dto.getPrice()
                        );

                        if (dto.getShifts() != null) {
                            clase.setShifts(dto.getShifts());  // requiere get/setShifts(List<ShiftDTO>) en Clase
                            android.util.Log.d(TAG, "Clase " + id + " con shifts=" + dto.getShifts().size());
                        } else {
                            android.util.Log.d(TAG, "Clase " + id + " llega sin shifts");
                        }

                        Log.d(TAG, "se obtuvo la clase con id " + id);
                        callback.onSuccess(clase);

                    } else {
                        String error = "No se encontro la clase con id " + id;
                        Log.e(TAG, error);
                        callback.onError(new RuntimeException(error));
                    }
                } else {
                    String error = "Error al obtener la clase: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(new RuntimeException(error));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ClaseDTO>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    @Override
    public void getClaseDtoPorId(Long id, SimpleCallback<ClaseDTO> callback) {
        api.obtenerClaseById(id).enqueue(new Callback<ApiResponse<ClaseDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ClaseDTO>> call, Response<ApiResponse<ClaseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ClaseDTO> body = response.body();
                    if (body.isSuccess() && body.getData() != null) {
                        Log.d(TAG, "DTO ok id=" + id + " shifts=" +
                                (body.getData().getShifts() != null ? body.getData().getShifts().size() : 0));
                        callback.onSuccess(body.getData());
                    } else {
                        String msg = "No se encontró la clase id=" + id;
                        Log.e(TAG, msg);
                        callback.onError(new RuntimeException(msg));
                    }
                } else {
                    String msg = "Error HTTP al obtener clase DTO: " + response.code();
                    Log.e(TAG, msg);
                    callback.onError(new RuntimeException(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ClaseDTO>> call, Throwable t) {
                callback.onError(t);
            }
        });
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

    @Override
    public void reservar(Long shiftId, SimpleCallback<String> callback) {
        // 1) obtener user actual
        api.obtenerUserActual().enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    Long userId = resp.body().getData().getId();
                    ReservationDTO dto = new ReservationDTO(userId, shiftId);
                    reservationApi.reservar(dto).enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> r) {
                            if (r.isSuccessful() && r.body() != null && r.body().isSuccess()) {
                                callback.onSuccess(r.body().getData());
                            } else {
                                callback.onError(new RuntimeException((r.body()!=null?r.body().getError():"Error reservando")));
                            }
                        }
                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            callback.onError(t);
                        }
                    });
                } else {
                    callback.onError(new RuntimeException("No se pudo obtener el usuario actual"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    @Override
    public void cancelarReserva(Long shiftId, SimpleCallback<String> callback) {
        reservationApi.cancelar(shiftId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> r) {
                if (r.isSuccessful() && r.body()!=null && r.body().isSuccess()) {
                    callback.onSuccess(r.body().getData());
                } else {
                    callback.onError(new RuntimeException((r.body()!=null?r.body().getData():"Error cancelando")));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    @Override
    public void getMisReservas(SimpleCallback<List<ReservationDTO>> callback) {
        reservationApi.misReservas().enqueue(new Callback<ApiResponse<List<ReservationDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ReservationDTO>>> call, Response<ApiResponse<List<ReservationDTO>>> r) {
                if (r.isSuccessful() && r.body()!=null && r.body().isSuccess()) {
                    callback.onSuccess(r.body().getData());
                } else {
                    callback.onError(new RuntimeException("Error listando reservas"));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<ReservationDTO>>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }




}
