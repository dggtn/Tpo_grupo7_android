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
        resendButton.setOnClickListener(v -> resendCode());
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

    private void resendCode() {
        Toast.makeText(requireContext(), "Funcionalidad de reenvío no implementada", Toast.LENGTH_SHORT).show();
        // Aquí podrías implementar la funcionalidad de reenvío si tu backend lo soporta
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        verifyButton.setEnabled(!show);
        resendButton.setEnabled(!show);
    }
}