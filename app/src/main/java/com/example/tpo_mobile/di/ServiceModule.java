package com.example.tpo_mobile.di;

import com.example.tpo_mobile.services.ClaseService;
import com.example.tpo_mobile.services.ClaseServiceImpl;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class ServiceModule {

    @Binds
    @Singleton
    public abstract ClaseService provideClaseService(ClaseServiceImpl impl);

}
