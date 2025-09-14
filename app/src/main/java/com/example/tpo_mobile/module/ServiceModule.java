package com.example.tpo_mobile.module;

import com.example.tpo_mobile.services.GymService;
import com.example.tpo_mobile.services.GymServiceImpl;

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
    public abstract GymService provideClaseService(GymServiceImpl impl);

}
