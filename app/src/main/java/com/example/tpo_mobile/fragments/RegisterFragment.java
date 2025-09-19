package com.example.tpo_mobile.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.data.modelDTO.RegisterRequest;
import com.example.tpo_mobile.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";

    @Inject
    AuthRepository authRepository;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button sendCodeButton;
    private Button backToLoginButton;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();

        // Si llegamos desde recovery, pre-llenar el email si está disponible
        prefillEmailFromArguments();
    }

    private void initViews(View view) {
        emailEditText = view.findViewById(R.id.editTextTextEmailAddress2);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        confirmPasswordEditText = view.findViewById(R.id.editTextConfirmPassword);
        sendCodeButton = view.findViewById(R.id.buttonEnviarCodigo);
        backToLoginButton = view.findViewById(R.id.buttonIniciaSesion);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        sendCodeButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón Enviar Código presionado");
            performRegistration();
        });
        backToLoginButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón back presionado");
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void prefillEmailFromArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String email = args.getString("email");
            if (email != null && !email.trim().isEmpty()) {
                emailEditText.setText(email);
                // Enfocar en el campo de contraseña para mejor UX
                passwordEditText.requestFocus();
            }
        }
    }

    private void performRegistration() {
        Log.d(TAG, "performRegistration() iniciado");

        if (!validateInput()) {
            Log.d(TAG, "Validación falló");
            return;
        }

        Log.d(TAG, "Validación exitosa, enviando request");
        showLoading(true);

        RegisterRequest request = createRegisterRequest();
        Log.d(TAG, "Request creado para email: " + request.getEmail());

        authRepository.iniciarRegistro(request, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Success response: " + result);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show();

                        // Navegar a fragment de verificación con el email
                        Bundle args = new Bundle();
                        String email = emailEditText.getText().toString().trim();
                        args.putString("email", email);
                        Log.d(TAG, "Navegando a verification con email: " + email);

                        try {
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.action_register_to_verification, args);
                            Log.d(TAG, "Navegación exitosa");
                        } catch (Exception e) {
                            Log.e(TAG, "Error en navegación: " + e.getMessage(), e);
                            Toast.makeText(requireContext(), "Error de navegación", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error response: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);

                        // MEJORADO: Manejo específico de diferentes tipos de errores
                        if (error.contains("ya está registrado") || error.contains("already exists")) {
                            showEmailAlreadyExistsDialog(emailEditText.getText().toString().trim());
                        } else if (error.contains("registro pendiente") || error.contains("pending registration")) {
                            // Caso especial: hay un registro pendiente
                            showPendingRegistrationDialog(emailEditText.getText().toString().trim());
                        } else {
                            Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

        });
    }

    // Diálogo cuando el email ya está registrado
    private void showEmailAlreadyExistsDialog(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Email Ya Registrado")
                .setMessage("El email " + email + " ya está registrado. ¿Qué deseas hacer?")
                .setPositiveButton("Iniciar Sesión", (dialog, which) -> {
                    // Navegar al login y pre-llenar el email
                    Bundle args = new Bundle();
                    args.putString("email", email);
                    Navigation.findNavController(requireView()).navigate(R.id.action_register_to_login, args);
                })
                .setNegativeButton("Recuperar Acceso", (dialog, which) -> {
                    // Navegar a recovery con el email
                    Bundle args = new Bundle();
                    args.putString("email", email);
                    Navigation.findNavController(requireView()).navigate(R.id.action_register_to_recovery, args);
                })
                .setNeutralButton("Usar otro email", (dialog, which) -> {
                    // Limpiar el campo email para que el usuario ingrese otro
                    emailEditText.setText("");
                    emailEditText.requestFocus();
                    dialog.dismiss();
                })
                .show();
    }

    private void showPendingRegistrationDialog(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Registro Pendiente")
                .setMessage("Ya tienes un registro pendiente para " + email + ". ¿Deseas recuperar el acceso para completar la verificación?")
                .setPositiveButton("Sí, recuperar acceso", (dialog, which) -> {
                    Bundle args = new Bundle();
                    args.putString("email", email);
                    Navigation.findNavController(requireView()).navigate(R.id.action_register_to_recovery, args);
                })
                .setNegativeButton("Usar otro email", (dialog, which) -> {
                    emailEditText.setText("");
                    emailEditText.requestFocus();
                    dialog.dismiss();
                })
                .show();
    }

    private RegisterRequest createRegisterRequest() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        return new RegisterRequest(null, null, null, email, password, null, null, null);
    }

    private boolean validateInput() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

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

        if (password.length() < 6) {
            passwordEditText.setError("Contraseña debe tener al menos 6 caracteres");
            passwordEditText.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Las contraseñas no coinciden");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        sendCodeButton.setEnabled(!show);
        backToLoginButton.setEnabled(!show);
        emailEditText.setEnabled(!show);
        passwordEditText.setEnabled(!show);
        confirmPasswordEditText.setEnabled(!show);
    }
}