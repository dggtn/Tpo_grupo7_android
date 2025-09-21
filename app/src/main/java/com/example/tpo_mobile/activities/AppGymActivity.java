package com.example.tpo_mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.services.BiometricAuthService;
import com.example.tpo_mobile.services.LogoutService;
import com.example.tpo_mobile.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppGymActivity extends AppCompatActivity {

    private static final String TAG = "AppGymActivity";

    // Flag para controlar si ya se mostró la biometría en esta sesión
    private boolean biometricShownInThisSession = false;

    @Inject
    TokenManager tokenManager;

    @Inject
    LogoutService logoutService;

    @Inject
    BiometricAuthService biometricAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si está autenticado
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        try {
            Log.d(TAG, "Usuario logueado: " + tokenManager.getUserInfo());
            biometricAuthService.logBiometricStatus(); // Debug
        } catch (Exception e) {
            Log.w(TAG, "Error obteniendo info de usuario: " + e.getMessage());
        }

        setContentView(R.layout.gym_app);
        setupNavigation();

        // Si el usuario tiene biometría configurada y es válida, solicitarla al entrar
        checkBiometricOnEntry();
    }

    private void checkBiometricOnEntry() {
        // Solo mostrar biometría si:
        // 1. No se ha mostrado ya en esta sesión
        // 2. El usuario tiene biometría habilitada y configurada correctamente
        // 3. El dispositivo soporta biometría
        if (!biometricShownInThisSession &&
                biometricAuthService.shouldRequestBiometricAuth() &&
                biometricAuthService.isDeviceBiometricCapable()) {

            Log.d(TAG, "Solicitando autenticación biométrica al entrar a la app");

            // Delay pequeño para que la UI se estabilice
            new android.os.Handler().postDelayed(this::performBiometricAuth, 800);
        }
    }

    private void performBiometricAuth() {
        biometricShownInThisSession = true; // Marcar que ya se mostró

        biometricAuthService.authenticateWithBiometric(
                this,
                new BiometricAuthService.BiometricAuthCallback() {
                    @Override
                    public void onBiometricAuthSuccess() {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Autenticación biométrica exitosa en AppGymActivity");
                            Toast.makeText(AppGymActivity.this, "Acceso verificado", Toast.LENGTH_SHORT).show();

                            // Aquí la app continúa normalmente ya que el usuario está autenticado
                        });
                    }

                    @Override
                    public void onBiometricAuthError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error biométrico: " + error);
                            handleBiometricFailure("Error de autenticación: " + error);
                        });
                    }

                    @Override
                    public void onBiometricNotAvailable(String reason) {
                        runOnUiThread(() -> {
                            Log.w(TAG, "Biometría no disponible: " + reason);
                            // Si la biometría no está disponible, continuar normalmente
                            // ya que el usuario ya está logueado
                            Toast.makeText(AppGymActivity.this, "Continuando sin verificación biométrica", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onBiometricAuthFailed() {
                        runOnUiThread(() -> {
                            Log.w(TAG, "Autenticación biométrica fallida");
                            handleBiometricFailure("No se pudo verificar la identidad");
                        });
                    }
                },
                "Verificar identidad para continuar"
        );
    }

    private void handleBiometricFailure(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Verificación de identidad")
                .setMessage(message + "\n\n¿Qué deseas hacer?")
                .setPositiveButton("Cerrar sesión", (dialog, which) -> {
                    // Cerrar sesión y volver al login
                    logoutService.performLogout(new LogoutService.LogoutCallback() {
                        @Override
                        public void onLogoutSuccess(String message) {
                            runOnUiThread(() -> {
                                Toast.makeText(AppGymActivity.this, "Sesión cerrada por seguridad", Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                            });
                        }

                        @Override
                        public void onLogoutError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(AppGymActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                            });
                        }
                    }, false); // No limpiar biometría para que pueda volver a intentar
                })
                .setNegativeButton("Continuar sin verificar", (dialog, which) -> {
                    // Permitir continuar pero mostrar advertencia
                    Toast.makeText(AppGymActivity.this, "Continuando sin verificación biométrica", Toast.LENGTH_LONG).show();
                })
                .setNeutralButton("Reintentar", (dialog, which) -> {
                    // Permitir reintentar la biometría
                    biometricShownInThisSession = false; // Reset flag
                    performBiometricAuth();
                })
                .setCancelable(false)
                .show();
    }

    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle() != null && item.getTitle().equals("Salir")) {
            showLogoutConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar salida")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí, salir", (dialog, which) -> performLogout())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        Log.d(TAG, "Iniciando proceso de logout");

        // Mostrar mensaje de loading (opcional)
        Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();

        logoutService.performLogout(new LogoutService.LogoutCallback() {
            @Override
            public void onLogoutSuccess(String message) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Logout exitoso: " + message);
                    Toast.makeText(AppGymActivity.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            }

            @Override
            public void onLogoutError(String error) {
                runOnUiThread(() -> {
                    Log.w(TAG, "Error en logout: " + error);
                    Toast.makeText(AppGymActivity.this, "Sesión cerrada (error de conexión)", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            }
        });
    }

    // Método alternativo para logout rápido (sin confirmación)
    private void performQuickLogout() {
        Log.d(TAG, "Realizando logout rápido");
        logoutService.logoutLocalAndNavigateToLogin();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "⭐ onStart: La Activity está a punto de hacerse visible");

        // Verificar sesión al volver a la app
        if (!tokenManager.isLoggedIn()) {
            Log.w(TAG, "Sesión perdida, redirigiendo al login");
            navigateToLogin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "⭐ onResume: La Activity es visible y tiene el foco");

        // Si volvemos de background y el usuario tiene biometría configurada,
        // pero no se ha verificado en esta sesión, solicitar nuevamente
        // (esto es opcional, puedes comentarlo si no quieres verificación al volver)
        /*
        if (!biometricShownInThisSession &&
            biometricAuthService.shouldRequestBiometricAuth() &&
            biometricAuthService.isDeviceBiometricCapable()) {

            new android.os.Handler().postDelayed(this::performBiometricAuth, 500);
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⭐ onPause: La Activity está perdiendo el foco");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "⭐ onStop: La Activity ya no es visible");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "⭐ onRestart: La Activity está volviendo a empezar después de detenerse");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "⭐ onDestroy: La Activity está siendo destruida");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "⭐ onSaveInstanceState: Guardando el estado de la Activity");

        // Guardar el estado de la verificación biométrica
        outState.putBoolean("biometric_shown", biometricShownInThisSession);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "⭐ onRestoreInstanceState: Restaurando el estado guardado de la Activity");

        // Restaurar el estado de la verificación biométrica
        if (savedInstanceState != null) {
            biometricShownInThisSession = savedInstanceState.getBoolean("biometric_shown", false);
        }
    }
}