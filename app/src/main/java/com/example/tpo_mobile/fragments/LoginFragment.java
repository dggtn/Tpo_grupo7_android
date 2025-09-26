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

        // Si ya está logueado, verificar si debe ir al home directamente
        if (tokenManager.isLoggedIn()) {
            // Si tiene biometría configurada y válida, ir directamente
            if (biometricAuthService.isBiometricConfigValidForCurrentUser()) {
                navigateToHome();
            }
            // Si está logueado pero sin biometría válida, quedarse en login para reconfigurar
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

            Log.d(TAG, "Botón biométrico configurado para usuario: " + biometricEmail);
        } else {
            biometricButton.setVisibility(View.GONE);
            Log.d(TAG, "Botón biométrico oculto - Capaz: " + biometricAuthService.isDeviceBiometricCapable() +
                    ", Tiene config: " + biometricAuthService.hasBiometricConfiguration());
        }
    }

    private void checkForAutoBiometricAuth() {
        // Auto-activar biometría si está configurada, disponible y el usuario no está aún logueado
        if (!tokenManager.isLoggedIn() && biometricAuthService.shouldRequestBiometricAuth()) {
            // Delay pequeño para que la UI se estabilice
            new android.os.Handler().postDelayed(() -> {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    Log.d(TAG, "Solicitando biometría automática");
                    performBiometricAuth();
                }
            }, 1000);
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
                                Log.d(TAG, "Autenticación biométrica exitosa");
                                showLoading(false);

                                // Verificar que el usuario esté realmente logueado
                                if (tokenManager.isLoggedIn()) {
                                    Toast.makeText(requireContext(), "Acceso biométrico exitoso", Toast.LENGTH_SHORT).show();
                                    navigateToHome();
                                } else {
                                    // Si la biometría fue exitosa pero no hay token, hay inconsistencia
                                    Log.w(TAG, "Biometría exitosa pero no hay token válido");
                                    biometricAuthService.clearBiometricConfiguration();
                                    Toast.makeText(requireContext(), "Sesión expirada, inicia sesión nuevamente", Toast.LENGTH_LONG).show();
                                    setupBiometricButton(); // Reconfigurar UI
                                }
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

                        Log.d(TAG, "Login exitoso para: " + email);
                        Toast.makeText(requireContext(), "Login exitoso", Toast.LENGTH_SHORT).show();

                        // Verificar si debe habilitar/actualizar biometría
                        handleBiometricAfterLogin(email);
                        //navigateToHome();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Error en login: " + error);
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void handleBiometricAfterLogin(String email) {
        // Si el dispositivo no soporta biometría, ir directamente al home
        if (!biometricAuthService.isDeviceBiometricCapable()) {
            navigateToHome();
            return;
        }

        // Si ya tiene biometría habilitada para este usuario, actualizar y continuar
        if (biometricAuthService.isBiometricAuthEnabled()) {
            String biometricEmail = biometricAuthService.getBiometricUserEmail();
            if (email.equals(biometricEmail)) {
                // Mismo usuario, solo actualizar último uso y continuar
                biometricAuthService.updateBiometricUserIfNeeded();
                navigateToHome();
                return;
            } else {
                // Usuario diferente, preguntar si quiere actualizar la configuración biométrica
                showBiometricUpdateDialog(email);
                return;
            }
        }

        // No tiene biometría habilitada, preguntar si quiere activarla
        showBiometricEnrollmentDialog(email);
    }

    private void showBiometricEnrollmentDialog(String email) {
        if (getActivity() == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Activar Autenticación Biométrica")
                .setMessage("¿Quieres activar el acceso rápido con huella digital o reconocimiento facial para futuros inicios de sesión?\n\nEsto te permitirá acceder más rápido la próxima vez.")
                .setPositiveButton("Sí, activar", (dialog, which) -> {
                    testAndEnableBiometric(email);
                })
                .setNegativeButton("Ahora no", (dialog, which) -> {
                    navigateToHome();
                })
                .setCancelable(false)
                .show();
    }

    private void showBiometricUpdateDialog(String email) {
        if (getActivity() == null) return;

        String currentBiometricEmail = biometricAuthService.getBiometricUserEmail();

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Actualizar Autenticación Biométrica")
                .setMessage("La autenticación biométrica está configurada para otro usuario (" + currentBiometricEmail + ").\n\n¿Quieres actualizarla para tu cuenta actual (" + email + ")?")
                .setPositiveButton("Sí, actualizar", (dialog, which) -> {
                    testAndEnableBiometric(email);
                })
                .setNegativeButton("No, continuar sin biometría", (dialog, which) -> {
                    navigateToHome();
                })
                .setNeutralButton("Desactivar biometría", (dialog, which) -> {
                    biometricAuthService.clearBiometricConfiguration();
                    Toast.makeText(requireContext(), "Biometría desactivada", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                })
                .setCancelable(false)
                .show();
    }

    private void testAndEnableBiometric(String email) {
        Log.d(TAG, "Probando y habilitando biometría para: " + email);

        biometricAuthService.authenticateWithBiometric(
                (androidx.appcompat.app.AppCompatActivity) getActivity(),
                new BiometricAuthService.BiometricAuthCallback() {
                    @Override
                    public void onBiometricAuthSuccess() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // Habilitar biometría para este usuario
                                biometricAuthService.enableBiometricAuth();

                                Log.d(TAG, "Biometría habilitada exitosamente para: " + email);
                                Toast.makeText(requireContext(), "Autenticación biométrica activada exitosamente", Toast.LENGTH_SHORT).show();

                                navigateToHome();
                            });
                        }
                    }

                    @Override
                    public void onBiometricAuthError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.e(TAG, "Error activando biometría: " + error);
                                Toast.makeText(requireContext(), "No se pudo activar biometría: " + error, Toast.LENGTH_LONG).show();
                                navigateToHome();
                            });
                        }
                    }

                    @Override
                    public void onBiometricNotAvailable(String reason) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.w(TAG, "Biometría no disponible para activación: " + reason);
                                Toast.makeText(requireContext(), "Biometría no disponible: " + reason, Toast.LENGTH_LONG).show();
                                navigateToHome();
                            });
                        }
                    }

                    @Override
                    public void onBiometricAuthFailed() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.w(TAG, "Fallo al activar biometría");
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // Debug: mostrar estado biométrico
        biometricAuthService.logBiometricStatus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}