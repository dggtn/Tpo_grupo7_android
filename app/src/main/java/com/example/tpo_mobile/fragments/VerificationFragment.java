package com.example.tpo_mobile.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.tpo_mobile.data.modelDTO.VerificationRequest;
import com.example.tpo_mobile.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VerificationFragment extends Fragment {

    private static final String TAG = "VerificationFragment";

    @Inject
    AuthRepository authRepository;

    private EditText codeEditText;
    private Button verifyButton;
    private Button resendButton;
    private ProgressBar progressBar;
    private TextView emailTextView;

    private String email;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            email = args.getString("email");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        displayEmail();
    }

    private void initViews(View view) {
        codeEditText = view.findViewById(R.id.editTextCode);
        verifyButton = view.findViewById(R.id.buttonVerify);
        resendButton = view.findViewById(R.id.buttonResend);
        progressBar = view.findViewById(R.id.progressBar);
        emailTextView = view.findViewById(R.id.textViewEmail);
    }

    private void setupListeners() {
        verifyButton.setOnClickListener(v -> performVerification());

        // ACTUALIZADO: Implementar reenvío de código real
        resendButton.setOnClickListener(v -> {
            Log.d(TAG, "Botón reenviar código presionado");
            resendVerificationCode();
        });
    }

    private void displayEmail() {
        if (email != null) {
            emailTextView.setText("Código enviado a: " + email);
        }
    }

    private void performVerification() {
        String code = codeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            codeEditText.setError("Código requerido");
            codeEditText.requestFocus();
            return;
        }

        if (code.length() != 4) {
            codeEditText.setError("El código debe tener 4 dígitos");
            codeEditText.requestFocus();
            return;
        }

        showLoading(true);

        VerificationRequest request = new VerificationRequest(email, code);
        authRepository.finalizarRegistro(request, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Registro completado exitosamente", Toast.LENGTH_SHORT).show();

                        // Navegar de vuelta al login
                        Navigation.findNavController(requireView()).navigate(R.id.action_verification_to_login);
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

    // NUEVO: Implementación real del reenvío de código
    private void resendVerificationCode() {
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Error: Email no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        authRepository.reenviarCodigo(email, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Código reenviado exitosamente: " + result);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show();

                        // Limpiar el campo de código para que el usuario ingrese el nuevo
                        codeEditText.setText("");
                        codeEditText.requestFocus();
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

                        // Si el registro ha expirado completamente, ofrecer volver al registro
                        if (error.contains("expirado completamente") || error.contains("iniciar el proceso")) {
                            showExpiredRegistrationDialog();
                        }
                    });
                }
            }
        });
    }

    // NUEVO: Diálogo cuando el registro ha expirado completamente
    private void showExpiredRegistrationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Registro Expirado")
                .setMessage("Tu registro ha expirado completamente. ¿Deseas iniciar un nuevo registro?")
                .setPositiveButton("Sí, registrarme", (dialog, which) -> {
                    // Navegar al registro con el email pre-llenado si es posible
                    Navigation.findNavController(requireView()).navigate(R.id.action_verification_to_register);
                })
                .setNegativeButton("Volver al Login", (dialog, which) -> {
                    Navigation.findNavController(requireView()).navigate(R.id.action_verification_to_login);
                })
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        verifyButton.setEnabled(!show);
        resendButton.setEnabled(!show);
        codeEditText.setEnabled(!show);
    }
}