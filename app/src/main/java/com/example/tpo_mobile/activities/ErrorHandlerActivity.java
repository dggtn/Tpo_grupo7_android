package com.example.tpo_mobile.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.services.LogoutService;
import com.example.tpo_mobile.utils.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ErrorHandlerActivity extends AppCompatActivity {

    private static final String TAG = "ErrorHandlerActivity";

    // Extras para el Intent
    public static final String EXTRA_ERROR_TYPE = "error_type";
    public static final String EXTRA_ERROR_MESSAGE = "error_message";
    public static final String EXTRA_ERROR_DETAILS = "error_details";
    public static final String EXTRA_STACK_TRACE = "stack_trace";
    public static final String EXTRA_SOURCE_ACTIVITY = "source_activity";

    // Tipos de errores
    public static final String ERROR_TYPE_CRASH = "crash";
    public static final String ERROR_TYPE_NETWORK = "network";
    public static final String ERROR_TYPE_AUTH = "auth";
    public static final String ERROR_TYPE_FUNCTIONAL = "functional";
    public static final String ERROR_TYPE_UNKNOWN = "unknown";

    @Inject
    TokenManager tokenManager;

    @Inject
    LogoutService logoutService;

    private TextView tvErrorTitle;
    private TextView tvErrorMessage;
    private TextView tvErrorDetails;
    private Button btnRetry;
    private Button btnGoHome;
    private Button btnRestart;
    private Button btnContactSupport;

    private String errorType;
    private String errorMessage;
    private String errorDetails;
    private String stackTrace;
    private String sourceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_handler);

        initializeViews();
        extractErrorInfo();
        setupUI();
        setupButtons();

        Log.e(TAG, "ErrorHandlerActivity iniciada - Tipo: " + errorType + ", Mensaje: " + errorMessage);
    }

    private void initializeViews() {
        tvErrorTitle = findViewById(R.id.tv_error_title);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        tvErrorDetails = findViewById(R.id.tv_error_details);
        btnRetry = findViewById(R.id.btn_retry);
        btnGoHome = findViewById(R.id.btn_go_home);
        btnRestart = findViewById(R.id.btn_restart);
        btnContactSupport = findViewById(R.id.btn_contact_support);
    }

    private void extractErrorInfo() {
        Intent intent = getIntent();
        errorType = intent.getStringExtra(EXTRA_ERROR_TYPE);
        errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE);
        errorDetails = intent.getStringExtra(EXTRA_ERROR_DETAILS);
        stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE);
        sourceActivity = intent.getStringExtra(EXTRA_SOURCE_ACTIVITY);

        // Valores por defecto si no se proporcionaron
        if (errorType == null) errorType = ERROR_TYPE_UNKNOWN;
        if (errorMessage == null) errorMessage = "Ha ocurrido un error inesperado";
        if (sourceActivity == null) sourceActivity = "Desconocido";
    }

    private void setupUI() {
        // Configurar título según el tipo de error
        switch (errorType) {
            case ERROR_TYPE_CRASH:
                tvErrorTitle.setText("Error Crítico");
                break;
            case ERROR_TYPE_NETWORK:
                tvErrorTitle.setText("Error de Conexión");
                break;
            case ERROR_TYPE_AUTH:
                tvErrorTitle.setText("Error de Autenticación");
                break;
            case ERROR_TYPE_FUNCTIONAL:
                tvErrorTitle.setText("Error Funcional");
                break;
            default:
                tvErrorTitle.setText("Error de Aplicación");
                break;
        }

        // Configurar mensaje principal
        tvErrorMessage.setText(getFormattedErrorMessage());

        // Configurar detalles (opcional)
        if (errorDetails != null && !errorDetails.trim().isEmpty()) {
            tvErrorDetails.setText("Detalles: " + errorDetails);
            tvErrorDetails.setVisibility(View.VISIBLE);
        } else {
            tvErrorDetails.setVisibility(View.GONE);
        }

        // Configurar visibilidad de botones según el tipo de error
        configureButtonsVisibility();
    }

    private String getFormattedErrorMessage() {
        StringBuilder message = new StringBuilder();
        message.append(errorMessage);

        if (sourceActivity != null && !sourceActivity.equals("Desconocido")) {
            message.append("\n\nOcurrió en: ").append(sourceActivity);
        }

        // Agregar sugerencias según el tipo de error
        switch (errorType) {
            case ERROR_TYPE_NETWORK:
                message.append("\n\n• Verifica tu conexión a internet\n• Intenta nuevamente en unos momentos");
                break;
            case ERROR_TYPE_AUTH:
                message.append("\n\n• Tu sesión puede haber expirado\n• Necesitarás iniciar sesión nuevamente");
                break;
            case ERROR_TYPE_FUNCTIONAL:
                message.append("\n\n• Intenta la operación nuevamente\n• Si persiste, contacta soporte");
                break;
            case ERROR_TYPE_CRASH:
                message.append("\n\n• La aplicación se reiniciará\n• Tus datos están seguros");
                break;
        }

        return message.toString();
    }

    private void configureButtonsVisibility() {
        // Configurar botones según el tipo de error
        switch (errorType) {
            case ERROR_TYPE_NETWORK:
                btnRetry.setVisibility(View.VISIBLE);
                btnGoHome.setVisibility(View.VISIBLE);
                btnRestart.setVisibility(View.GONE);
                break;

            case ERROR_TYPE_AUTH:
                btnRetry.setVisibility(View.GONE);
                btnGoHome.setVisibility(View.GONE);
                btnRestart.setVisibility(View.VISIBLE);
                btnRestart.setText("Ir al Login");
                break;

            case ERROR_TYPE_CRASH:
                btnRetry.setVisibility(View.GONE);
                btnGoHome.setVisibility(View.GONE);
                btnRestart.setVisibility(View.VISIBLE);
                btnRestart.setText("Reiniciar App");
                break;

            case ERROR_TYPE_FUNCTIONAL:
                btnRetry.setVisibility(View.VISIBLE);
                btnGoHome.setVisibility(View.VISIBLE);
                btnRestart.setVisibility(View.GONE);
                break;

            default:
                btnRetry.setVisibility(View.VISIBLE);
                btnGoHome.setVisibility(View.VISIBLE);
                btnRestart.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupButtons() {
        btnRetry.setOnClickListener(v -> handleRetry());
        btnGoHome.setOnClickListener(v -> handleGoHome());
        btnRestart.setOnClickListener(v -> handleRestart());
        btnContactSupport.setOnClickListener(v -> handleContactSupport());
    }

    private void handleRetry() {
        Log.d(TAG, "Usuario eligió reintentar");
        Toast.makeText(this, "Reintentando...", Toast.LENGTH_SHORT).show();

        try {
            // Si hay una actividad de origen válida, intentar volver a ella
            if (sourceActivity != null && !sourceActivity.equals("Desconocido")) {
                // Intentar crear intent para la actividad de origen
                if (sourceActivity.contains("AppGymActivity")) {
                    navigateToGymApp();
                } else if (sourceActivity.contains("MainActivity")) {
                    navigateToLogin();
                } else {
                    // Por defecto, ir al home
                    navigateToGymApp();
                }
            } else {
                navigateToGymApp();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en retry: " + e.getMessage());
            navigateToLogin();
        }
    }

    private void handleGoHome() {
        Log.d(TAG, "Usuario eligió ir al home");
        Toast.makeText(this, "Volviendo al inicio...", Toast.LENGTH_SHORT).show();

        if (tokenManager.isLoggedIn()) {
            navigateToGymApp();
        } else {
            navigateToLogin();
        }
    }

    private void handleRestart() {
        Log.d(TAG, "Usuario eligió reiniciar");

        if (errorType.equals(ERROR_TYPE_AUTH)) {
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            // Cerrar sesión y ir al login
            logoutService.logoutLocalAndNavigateToLogin();
            navigateToLogin();
        } else {
            Toast.makeText(this, "Reiniciando aplicación...", Toast.LENGTH_SHORT).show();
            // Reiniciar completamente la app
            restartApplication();
        }
    }

    private void handleContactSupport() {
        Log.d(TAG, "Usuario eligió contactar soporte");

        // Crear mensaje de soporte con información del error
        StringBuilder supportMessage = new StringBuilder();
        supportMessage.append("Error en Gym App:\n");
        supportMessage.append("Tipo: ").append(errorType).append("\n");
        supportMessage.append("Mensaje: ").append(errorMessage).append("\n");
        supportMessage.append("Pantalla: ").append(sourceActivity).append("\n");

        if (errorDetails != null) {
            supportMessage.append("Detalles: ").append(errorDetails).append("\n");
        }

        // Intent para compartir o enviar email
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Error en Gym App - Soporte");
        shareIntent.putExtra(Intent.EXTRA_TEXT, supportMessage.toString());

        try {
            startActivity(Intent.createChooser(shareIntent, "Contactar Soporte"));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la aplicación de soporte", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToGymApp() {
        Intent intent = new Intent(this, AppGymActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void restartApplication() {
        // Obtener el intent launcher de la aplicación
        Intent restartIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (restartIntent != null) {
            restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(restartIntent);
        }

        // Cerrar esta actividad
        finishAffinity();
        System.exit(0);
    }

    /**
     * Método estático para mostrar la pantalla de error desde cualquier parte de la app
     */
    public static void showError(Context context, String errorType, String message, String details, String sourceActivity) {
        Intent intent = new Intent(context, ErrorHandlerActivity.class);
        intent.putExtra(EXTRA_ERROR_TYPE, errorType);
        intent.putExtra(EXTRA_ERROR_MESSAGE, message);
        intent.putExtra(EXTRA_ERROR_DETAILS, details);
        intent.putExtra(EXTRA_SOURCE_ACTIVITY, sourceActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Método para manejar crashes no capturados
     */
    public static void showCrashError(Context context, Throwable throwable, String sourceActivity) {
        String stackTrace = Log.getStackTraceString(throwable);
        String message = "La aplicación ha encontrado un error inesperado y necesita cerrarse.";

        showError(context, ERROR_TYPE_CRASH, message, throwable.getMessage(), sourceActivity);
    }

    /**
     * Método para errores de red
     */
    public static void showNetworkError(Context context, String details, String sourceActivity) {
        String message = "No se pudo conectar con el servidor. Por favor, verifica tu conexión a internet.";
        showError(context, ERROR_TYPE_NETWORK, message, details, sourceActivity);
    }

    /**
     * Método para errores de autenticación
     */
    public static void showAuthError(Context context, String details, String sourceActivity) {
        String message = "Tu sesión ha expirado o no tienes permisos para realizar esta acción.";
        showError(context, ERROR_TYPE_AUTH, message, details, sourceActivity);
    }

    /**
     * Método para errores funcionales
     */
    public static void showFunctionalError(Context context, String message, String details, String sourceActivity) {
        showError(context, ERROR_TYPE_FUNCTIONAL, message, details, sourceActivity);
    }

    @Override
    public void onBackPressed() {
        // Prevenir que el usuario salga de la pantalla de error sin manejar el problema
        handleGoHome();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ErrorHandlerActivity destruida");
    }
}