package com.example.tpo_mobile.di;

import android.content.Context;

import com.example.tpo_mobile.data.api.GymApiService;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    Cache provideCache(@ApplicationContext Context context) {
        int cacheSize = 10 * 1024 * 1024; // 10 MB
        File cacheDir = new File(context.getCacheDir(), "http-cache");
        return new Cache(cacheDir, cacheSize);
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .cache(cache)
                .addNetworkInterceptor(chain -> {
                    return chain.proceed(chain.request())
                            .newBuilder()
                            .header("Cache-Control", "public, max-age=60") // Cache por 60 segundos
                            .build();
                })
                .build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl("https://6f184276-4286-4ec3-af1a-c0cb45e736e7.mock.pstmn.io")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    GymApiService provideGymApiService(Retrofit retrofit) {
        return retrofit.create(GymApiService.class);
    }
}
