package com.example.tpo_mobile.module;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class BiometricModule {

    // BiometricAuthService está marcado con @Inject en su constructor,
    // por lo que Hilt puede crear la instancia automáticamente.
    // No necesita binding adicional aquí.

    // Si necesitáramos configuraciones específicas, las agregaríamos aquí:
    // @Provides
    // @Singleton
    // public BiometricConfiguration provideBiometricConfig() {
    //     return new BiometricConfiguration.Builder()
    //         .setTimeoutMinutes(30)
    //         .setMaxRetries(3)
    //         .build();
    // }
}
