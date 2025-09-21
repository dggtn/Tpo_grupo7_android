package com.example.tpo_mobile.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.activities.AppGymActivity;
import com.example.tpo_mobile.data.modelDTO.AuthRequest;
import com.example.tpo_mobile.data.modelDTO.AuthResponse;
import com.example.tpo_mobile.repository.AuthRepository;
import com.example.tpo_mobile.services.BiometricAuthService;
import com.example.tpo_mobile.utils.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    @Inject
    AuthRepository authRepository;

    @Inject
    TokenManager tokenManager;

    @Inject
    BiometricAuthService biometricAuthService;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private Button recoverAccessButton;
    private ImageButton biometricButton;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Si ya está logueado, ir directamente al home
        if (tokenManager.isLoggedIn()) {
            navigateToHome();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        setupBiometricButton();

        // Pre-llenar email si viene desde recovery u otro fragment
        prefillEmailFromArguments();

        // Verificar si debe mostrar autenticación biométrica automáticamente
        checkForAutoBiometricAuth();
    }

    private void prefillEmailFromArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String email = args.getString("email");
            if (email != null && !email.trim().isEmpty()) {
                emailEditText.setText(email);
                passwordEditText.requestFocus();
            }
        }
    }

    private void initViews(View view) {
        emailEditText = view.findViewById(R.id.editTextTextEmailAddress2);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        loginButton = view.findViewById(R.id.buttonIniciaSesion);
        registerButton = view.findViewById(R.id.button_register);
        recoverAccessButton = view.findViewById(R.id.button_acceso);
        biometricButton = view.findViewById(R.id.button_biometric);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        registerButton.setOnClickListener(v -> navigateToRegister());
        recoverAccessButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón recuperar acceso presionado");
            navigateToRecovery();
        });
    }

    private void setupBiometricButton() {
        // Configurar visibilidad del botón biométrico
        if (biometricAuthService.isDeviceBiometricCapable() && biometricAuthService.hasBiometricConfiguration()) {
            biometricButton.setVisibility(View.VISIBLE);
            biometricButton.setOnClickListener(v -> performBiometricAuth());

            // Prellenar email si hay configuración biométrica
            String biometricEmail = biometricAuthService.getBiometricUserEmail();
            if (biometricEmail != null && emailEditText.getText().toString().trim().isEmpty()) {
                emailEditText.setText(biometricEmail);
            }
        } else {
            biometricButton.setVisibility(View.GONE);
        }
    }

    private void checkForAutoBiometricAuth() {
        // Auto-activar biometría si está configurada y disponible
        if (biometricAuthService.shouldRequestBiometricAuth()) {
            // Delay pequeño para que la UI se estabilice
            new android.os.Handler().postDelayed(() -> {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    performBiometricAuth();
                }
            }, 500);
        }
    }

    private void performBiometricAuth() {
        if (getActivity() == null || !(getActivity() instanceof androidx.appcompat.app.AppCompatActivity)) {
            Log.e(TAG, "Activity no es AppCompatActivity, no se puede usar biometría");
            return;
        }

        Log.d(TAG, "Iniciando autenticación biométrica");
        showLoading(true);

        biometricAuthService.authenticateWithBiometric(
                (androidx.appcompat.app.AppCompatActivity) getActivity(),
                new BiometricAuthService.BiometricAuthCallback() {
                    @Override
                    public void onBiometricAuthSuccess() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.d(TAG, "Autenticación biométrica exitosa, navegando al home");
                                showLoading(false);
                                Toast.makeText(requireContext(), "Autenticación biométrica exitosa", Toast.LENGTH_SHORT).show();
                                navigateToHome();
                            });
                        }
                    }

                    @Override
                    public void onBiometricAuthError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                Log.e(TAG, "Error en autenticación biométrica: " + error);
                                Toast.makeText(requireContext(), "Error biométrico: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                    }

                    @Override
                    public void onBiometricNotAvailable(String reason) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                Log.w(TAG, "Biometría no disponible: " + reason);
                                Toast.makeText(requireContext(), "Biometría no disponible: " + reason, Toast.LENGTH_LONG).show();
                                // Ocultar botón biométrico si no está disponible
                                biometricButton.setVisibility(View.GONE);
                            });
                        }
                    }

                    @Override
                    public void onBiometricAuthFailed() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                Log.w(TAG, "Autenticación biométrica fallida");
                                Toast.makeText(requireContext(), "Biometría no reconocida, intenta nuevamente", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                },
                "Acceso rápido con biometría"
        );
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        showLoading(true);

        AuthRequest request = new AuthRequest(email, password);
        authRepository.authenticate(request, new AuthRepository.AuthCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        tokenManager.saveToken(result.getAccessToken());
                        tokenManager.saveUserEmail(email);

                        Toast.makeText(requireContext(), "Login exitoso", Toast.LENGTH_SHORT).show();

                        // Preguntar si quiere habilitar biometría (solo si el dispositivo lo soporta)
                        if (biometricAuthService.isDeviceBiometricCapable() &&
                                !biometricAuthService.isBiometricAuthEnabled()) {
                            showBiometricEnrollmentDialog();
                        } else {
                            navigateToHome();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void showBiometricEnrollmentDialog() {
        if (getActivity() == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Activar Autenticación Biométrica")
                .setMessage("¿Quieres activar el acceso rápido con huella digital o reconocimiento facial para futuros inicios de sesión?")
                .setPositiveButton("Sí, activar", (dialog, which) -> {
                    // Test biométrico para verificar que funciona
                    if (getActivity() instanceof androidx.appcompat.app.AppCompatActivity) {
                        testAndEnableBiometric();
                    }
                })
                .setNegativeButton("Ahora no", (dialog, which) -> {
                    navigateToHome();
                })
                .setCancelable(false)
                .show();
    }

    private void testAndEnableBiometric() {
        biometricAuthService.authenticateWithBiometric(
                (androidx.appcompat.app.AppCompatActivity) getActivity(),
                new BiometricAuthService.BiometricAuthCallback() {
                    @Override
                    public void onBiometricAuthSuccess() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                biometricAuthService.enableBiometricAuth();
                                Toast.makeText(requireContext(), "Autenticación biométrica activada", Toast.LENGTH_SHORT).show();
                                navigateToHome();
                            });
                        }
                    }

                    @Override
                    public void onBiometricAuthError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "No se pudo activar biometría: " + error, Toast.LENGTH_LONG).show();
                                navigateToHome();
                            });
                        }
                    }

                    @Override
                    public void onBiometricNotAvailable(String reason) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Biometría no disponible: " + reason, Toast.LENGTH_LONG).show();
                                navigateToHome();
                            });
                        }
                    }

                    @Override
                    public void onBiometricAuthFailed() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "No se reconoció la biometría", Toast.LENGTH_SHORT).show();
                                navigateToHome();
                            });
                        }
                    }
                },
                "Activar autenticación biométrica"
        );
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email requerido");
            emailEditText.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email inválido");
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Contraseña requerida");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        registerButton.setEnabled(!show);
        recoverAccessButton.setEnabled(!show);
        biometricButton.setEnabled(!show);
    }

    private void navigateToHome() {
        Intent intent = new Intent(getActivity(), AppGymActivity.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void navigateToRegister() {
        Navigation.findNavController(requireView()).navigate(R.id.action_login_to_register);
    }

    private void navigateToRecovery() {
        Navigation.findNavController(requireView()).navigate(R.id.action_login_to_recovery);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        // Reconfigurar botón biométrico cuando vuelve el fragment
        setupBiometricButton();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}