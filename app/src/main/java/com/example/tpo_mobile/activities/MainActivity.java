package com.example.tpo_mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.services.BiometricAuthService;
import com.example.tpo_mobile.utils.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Inject
    TokenManager tokenManager;

    @Inject
    BiometricAuthService biometricAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si el usuario ya está logueado y tiene biometría configurada
        checkAuthenticationState();

        setContentView(R.layout.main_activity);
    }

    private void checkAuthenticationState() {
        Log.d(TAG, "Verificando estado de autenticación...");

        // Si el usuario está logueado y tiene biometría habilitada, ir directamente al gym
        if (tokenManager.isLoggedIn() && biometricAuthService.isBiometricConfigValidForCurrentUser()) {
            Log.d(TAG, "Usuario logueado con biometría válida, navegando a AppGymActivity");
            navigateToGymApp();
            return;
        }

        // Si está logueado pero no tiene biometría, o la biometría no es válida, ir al login
        if (tokenManager.isLoggedIn()) {
            Log.d(TAG, "Usuario logueado pero sin biometría válida, quedarse en login");
        } else {
            Log.d(TAG, "Usuario no logueado, mostrar pantalla de login");
        }
    }

    private void navigateToGymApp() {
        Intent intent = new Intent(this, AppGymActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "⭐ onStart: La Activity está a punto de hacerse visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "⭐ onResume: La Activity es visible y tiene el foco");

        // Verificar nuevamente por si cambió el estado mientras la app estaba en background
        if (tokenManager.isLoggedIn() && biometricAuthService.isBiometricConfigValidForCurrentUser()) {
            navigateToGymApp();
        }
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