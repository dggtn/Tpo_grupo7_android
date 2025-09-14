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
        sendCodeButton.setOnClickListener(v -> performRegistration());
        backToLoginButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void performRegistration() {
        if (!validateInput()) {
            return;
        }

        showLoading(true);

        RegisterRequest request = createRegisterRequest();
        authRepository.iniciarRegistro(request, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show();

                        // Navegar a fragment de verificación con el email
                        Bundle args = new Bundle();
                        args.putString("email", emailEditText.getText().toString().trim());
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_register_to_verification, args);
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
    }
}