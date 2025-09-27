package com.example.tpo_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.activities.MainActivity;
import com.example.tpo_mobile.data.api.GymApiService;
import com.example.tpo_mobile.data.modelDTO.ApiResponse;
import com.example.tpo_mobile.data.modelDTO.UserDTO;
import com.example.tpo_mobile.repository.SimpleCallback;
import com.example.tpo_mobile.services.GymService;
import com.example.tpo_mobile.services.LogoutService;
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

    @Inject
    LogoutService logoutService;

    @Inject
    GymService gymService;

    private TextView email;
    private TextView firstName;
    private Button logoutButton;
    private Button editName;

    private final View.OnClickListener onEditName = (v ) -> {
        Log.d(TAG, "Actualizando nombre de usuario");
        String name = firstName.getText().toString();

        UserDTO request = new UserDTO();
        request.setName(name);

        this.gymService.actualizarUsuario(request, new SimpleCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO data) {
                loadUsuario();
            }

            @Override
            public void onError(Throwable error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error de conexión: " + error.getMessage());
                        Toast.makeText(requireContext(),
                                "Error al cargar datos del usuario",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    };

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
        setupListeners();
        loadUsuario();
    }

    private void initViews(View view) {
        firstName = view.findViewById(R.id.editText);
        email = view.findViewById(R.id.email_text_view);
        editName = view.findViewById(R.id.editName);
        logoutButton = view.findViewById(R.id.logout_button);
    }

    private void setupListeners() {
        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());
        editName.setOnClickListener(onEditName);
    }

    private void loadUsuario() {
        Log.d(TAG, "Obteniendo datos del usuario");

        // Mostrar el email guardado localmente mientras se cargan los datos
        String savedEmail = tokenManager.getUserEmail();
        if (savedEmail != null) {
            email.setText("Email: " + savedEmail);
        }

        // Mostrar información básica del usuario
        String userInfo = tokenManager.getUserInfo();
        if (userInfo != null) {
            Log.d(TAG, "Info del usuario: " + userInfo);
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

        if (user.getFirstName() != null) {
            firstName.setText(user.getFirstName());
        }
        if (user.getEmail() != null) {
            email.setText("Email: " + user.getEmail());
        }

        // Guardar nombre del usuario para futuras referencias
        if (user.getFullName() != null) {
            tokenManager.saveUserName(user.getFullName());
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> performLogout())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        Log.d(TAG, "Iniciando logout desde perfil");

        // Deshabilitar botón mientras se procesa
        logoutButton.setEnabled(false);
        logoutButton.setText("Cerrando sesión...");

        logoutService.performLogout(new LogoutService.LogoutCallback() {
            @Override
            public void onLogoutSuccess(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "Logout exitoso desde perfil: " + message);
                        Toast.makeText(requireContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    });
                }
            }

            @Override
            public void onLogoutError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.w(TAG, "Error en logout desde perfil: " + error);
                        Toast.makeText(requireContext(), "Sesión cerrada (error de conexión)", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    });
                }
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Verificar que el usuario siga logueado
        if (!tokenManager.isLoggedIn()) {
            Log.w(TAG, "Usuario no logueado, redirigiendo");
            navigateToLogin();
        }
    }
}