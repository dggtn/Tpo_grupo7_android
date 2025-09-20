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
import com.example.tpo_mobile.services.LogoutService;
import com.example.tpo_mobile.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppGymActivity extends AppCompatActivity {

    private static final String TAG = "AppGymActivity";

    @Inject
    TokenManager tokenManager;

    @Inject
    LogoutService logoutService;

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
        } catch (Exception e) {
            Log.w(TAG, "Error obteniendo info de usuario: " + e.getMessage());
        }

        setContentView(R.layout.gym_app);
        setupNavigation();
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

    // Metodo alternativo para logout rápido (sin confirmación)
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
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "⭐ onRestoreInstanceState: Restaurando el estado guardado de la Activity");
    }

}