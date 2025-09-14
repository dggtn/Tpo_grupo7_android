package com.example.tpo_mobile.module;

import com.example.tpo_mobile.repository.GymRepository;
import com.example.tpo_mobile.repository.GymRetrofitRepository;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    /**
     * Proporciona la implementación de GymRepository
     */
    @Binds
    @Singleton
    public abstract GymRepository provideGymRepository(GymRetrofitRepository impl);

    /**
     * AuthRepository ya está configurado con @Inject en su constructor,
     * por lo que Hilt puede crear la instancia automáticamente.
     * No necesita binding adicional aquí.
     */
}