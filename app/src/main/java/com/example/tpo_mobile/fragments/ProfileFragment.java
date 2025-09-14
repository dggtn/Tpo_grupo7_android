package com.example.tpo_mobile.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.data.api.GymApiService;
import com.example.tpo_mobile.data.modelDTO.ApiResponse;
import com.example.tpo_mobile.data.modelDTO.UserDTO;
import com.example.tpo_mobile.utils.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private static final String TAG = "PerfilFragment";

    @Inject
    GymApiService gymApiService;

    @Inject
    TokenManager tokenManager;

    private TextView email;
    private TextView firstName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate perfil fragment");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadUsuario();
    }

    private void initViews(View view) {
        firstName = view.findViewById(R.id.name_text_view);
        email = view.findViewById(R.id.email_text_view);
        //Seguir completando...

    }

    private void loadUsuario() {
        Log.d(TAG, "Obteniendo datos del usuario");

        // Mostrar el email guardado localmente mientras se cargan los datos
        String savedEmail = tokenManager.getUserEmail();
        if (savedEmail != null) {
            email.setText("Email: " + savedEmail);
        }

        // Intentar obtener datos completos del usuario desde el backend
        gymApiService.obtenerUserActual().enqueue(new Callback<ApiResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDTO>> call, Response<ApiResponse<UserDTO>> response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            UserDTO user = response.body().getData();
                            if (user != null) {
                                updateUserInfo(user);
                            }
                        } else {
                            Log.e(TAG, "Error al cargar usuario: " + response.message());
                            // Mantener la información básica del email
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserDTO>> call, Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error de conexión: " + t.getMessage());
                        Toast.makeText(requireContext(),
                                "Error al cargar datos del usuario",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updateUserInfo(UserDTO user) {

        if (user.getFirstName() != null && user.getLastName() != null) {
            firstName.setText("Nombre: " + user.getFirstName() + " " + user.getLastName());
        } else if (user.getFirstName() != null) {
            firstName.setText("Nombre: " + user.getFirstName());
        }
        if (user.getEmail() != null) {
            email.setText("Email: " + user.getEmail());
        }

        //Seguir completando...

    }
}