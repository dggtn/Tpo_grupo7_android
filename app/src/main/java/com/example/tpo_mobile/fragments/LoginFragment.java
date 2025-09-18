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

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private Button recoverAccessButton; // NUEVO BOTÓN
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
    }

    private void initViews(View view) {
        emailEditText = view.findViewById(R.id.editTextTextEmailAddress2);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        loginButton = view.findViewById(R.id.buttonIniciaSesion);
        registerButton = view.findViewById(R.id.button_register);
        recoverAccessButton = view.findViewById(R.id.button_acceso); // NUEVO
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        registerButton.setOnClickListener(v -> navigateToRegister());

        // NUEVO: Listener para recuperar acceso
        recoverAccessButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón recuperar acceso presionado");
            navigateToRecovery();
        });
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
                        navigateToHome();
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

    // NUEVO: Navegación a recuperar acceso
    private void navigateToRecovery() {
        Navigation.findNavController(requireView()).navigate(R.id.action_login_to_recovery);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }
}