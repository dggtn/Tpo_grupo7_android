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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RecoveryFragment extends Fragment {

    private static final String TAG = "RecoveryFragment";

    @Inject
    AuthRepository authRepository;

    private EditText emailEditText;
    private Button checkEmailButton;
    private Button backToLoginButton;
    private ProgressBar progressBar;
    private TextView instructionText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        emailEditText = view.findViewById(R.id.editTextEmail);
        checkEmailButton = view.findViewById(R.id.buttonCheckEmail);
        backToLoginButton = view.findViewById(R.id.buttonBackToLogin);
        progressBar = view.findViewById(R.id.progressBar);
        instructionText = view.findViewById(R.id.textViewInstructions);
    }

    private void setupListeners() {
        checkEmailButton.setOnClickListener(v -> checkForPendingRegistration());
        backToLoginButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón back presionado");
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void checkForPendingRegistration() {
        String email = emailEditText.getText().toString().trim();

        if (!validateEmail(email)) {
            return;
        }

        showLoading(true);

        // Primero verificar si existe un registro pendiente
        authRepository.verificarEmailPendiente(email, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Registro pendiente encontrado: " + result);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        // Mostrar diálogo de confirmación para reenviar código
                        showResendCodeDialog(email);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error verificando email: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(),
                                "No hay registro pendiente para este email. Puedes registrarte normalmente.",
                                Toast.LENGTH_LONG).show();

                        // Opcional: navegar al registro
                        Navigation.findNavController(requireView()).navigate(R.id.action_recovery_to_register);
                    });
                }
            }
        });
    }

    private void showResendCodeDialog(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Registro Pendiente Encontrado")
                .setMessage("Tienes un registro pendiente de verificación para " + email +
                        ". ¿Deseas que te enviemos un nuevo código de verificación?")
                .setPositiveButton("Sí, enviar código", (dialog, which) -> {
                    resendVerificationCode(email);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void resendVerificationCode(String email) {
        showLoading(true);

        authRepository.reenviarCodigo(email, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Código reenviado exitosamente: " + result);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show();

                        // Navegar al fragment de verificación con el email
                        Bundle args = new Bundle();
                        args.putString("email", email);
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_recovery_to_verification, args);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error reenviando código: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private boolean validateEmail(String email) {
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

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        checkEmailButton.setEnabled(!show);
        backToLoginButton.setEnabled(!show);
        emailEditText.setEnabled(!show);
    }
}