package com.example.tpo_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.services.BiometricAuthService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BiometricSettingsFragment extends Fragment {
    private static final String TAG = "BiometricSettings";

    @Inject
    BiometricAuthService biometricAuthService;

    private Switch biometricSwitch;
    private TextView biometricStatusText;
    private Button testBiometricButton;
    private TextView deviceCapabilityText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_biometric_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViews();
        setupListeners();
    }

    private void initViews(View view) {
        biometricSwitch = view.findViewById(R.id.biometric_switch);
        biometricStatusText = view.findViewById(R.id.biometric_status_text);
        testBiometricButton = view.findViewById(R.id.test_biometric_button);
        deviceCapabilityText = view.findViewById(R.id.device_capability_text);
    }

    private void setupViews() {
        // Configurar estado inicial
        biometricSwitch.setChecked(biometricAuthService.isBiometricAuthEnabled());
        updateStatusText();
        updateDeviceCapabilityText();

        // Habilitar/deshabilitar controles según capacidad del dispositivo
        boolean isCapable = biometricAuthService.isDeviceBiometricCapable();
        biometricSwitch.setEnabled(isCapable);
        testBiometricButton.setEnabled(isCapable && biometricAuthService.isBiometricAuthEnabled());
    }

    private void setupListeners() {
        biometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableBiometric();
            } else {
                disableBiometric();
            }
        });

        testBiometricButton.setOnClickListener(v -> testBiometric());
    }

    private void enableBiometric() {
        if (!biometricAuthService.isDeviceBiometricCapable()) {
            biometricSwitch.setChecked(false);
            Toast.makeText(requireContext(), "Dispositivo no compatible con biometría", Toast.LENGTH_LONG).show();
            return;
        }

        if (getActivity() instanceof androidx.appcompat.app.AppCompatActivity) {
            biometricAuthService.authenticateWithBiometric(
                    (androidx.appcompat.app.AppCompatActivity) getActivity(),
                    new BiometricAuthService.BiometricAuthCallback() {
                        @Override
                        public void onBiometricAuthSuccess() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    biometricAuthService.enableBiometricAuth();
                                    updateStatusText();
                                    testBiometricButton.setEnabled(true);
                                    Toast.makeText(requireContext(), "Autenticación biométrica activada", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onBiometricAuthError(String error) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    biometricSwitch.setChecked(false);
                                    Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        }

                        @Override
                        public void onBiometricNotAvailable(String reason) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    biometricSwitch.setChecked(false);
                                    Toast.makeText(requireContext(), "No disponible: " + reason, Toast.LENGTH_LONG).show();
                                    updateDeviceCapabilityText();
                                });
                            }
                        }

                        @Override
                        public void onBiometricAuthFailed() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    biometricSwitch.setChecked(false);
                                    Toast.makeText(requireContext(), "Biometría no reconocida", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    },
                    "Confirmar activación de biometría"
            );
        }
    }

    private void disableBiometric() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Desactivar Biometría")
                .setMessage("¿Estás seguro de que quieres desactivar la autenticación biométrica? Tendrás que usar email y contraseña para iniciar sesión.")
                .setPositiveButton("Sí, desactivar", (dialog, which) -> {
                    biometricAuthService.disableBiometricAuth();
                    updateStatusText();
                    testBiometricButton.setEnabled(false);
                    Toast.makeText(requireContext(), "Autenticación biométrica desactivada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    biometricSwitch.setChecked(true);
                })
                .show();
    }

    private void testBiometric() {
        if (getActivity() instanceof androidx.appcompat.app.AppCompatActivity) {
            biometricAuthService.authenticateWithBiometric(
                    (androidx.appcompat.app.AppCompatActivity) getActivity(),
                    new BiometricAuthService.BiometricAuthCallback() {
                        @Override
                        public void onBiometricAuthSuccess() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "✅ Prueba exitosa", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onBiometricAuthError(String error) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "❌ Error: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        }

                        @Override
                        public void onBiometricNotAvailable(String reason) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "⚠️ No disponible: " + reason, Toast.LENGTH_LONG).show();
                                });
                            }
                        }

                        @Override
                        public void onBiometricAuthFailed() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "👆 Biometría no reconocida", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    },
                    "Prueba de biometría"
            );
        }
    }

    private void updateStatusText() {
        String status;
        if (biometricAuthService.isBiometricAuthEnabled()) {
            String userEmail = biometricAuthService.getBiometricUserEmail();
            status = "✅ Activada para: " + (userEmail != null ? userEmail : "usuario actual");

            if (biometricAuthService.hasValidRecentBiometricAuth()) {
                status += "\n🔒 Sesión biométrica activa";
            }
        } else {
            status = "❌ Desactivada";
        }

        biometricStatusText.setText(status);
    }

    private void updateDeviceCapabilityText() {
        String capability = biometricAuthService.getBiometricCapabilityStatus();
        String deviceStatus;

        if (biometricAuthService.isDeviceBiometricCapable()) {
            deviceStatus = "✅ " + capability;
        } else {
            deviceStatus = "❌ " + capability;
        }

        deviceCapabilityText.setText("Estado del dispositivo: " + deviceStatus);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Actualizar estado cuando se vuelve al fragment
        setupViews();
    }
}
