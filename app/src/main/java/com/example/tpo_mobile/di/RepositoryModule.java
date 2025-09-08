package com.example.tpo_mobile.di;

import com.example.tpo_mobile.Repository.ClasesRepository;
import com.example.tpo_mobile.Repository.GymRetrofitRepository;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    @Singleton
    public abstract ClasesRepository provideClasesRepository(GymRetrofitRepository impl);
}
