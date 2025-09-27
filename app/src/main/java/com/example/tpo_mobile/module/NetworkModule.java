package com.example.tpo_mobile.module;

import android.content.Context;

import com.example.tpo_mobile.data.api.AuthApiService;
import com.example.tpo_mobile.data.api.GymApiService;
import com.example.tpo_mobile.data.api.ReservationApiService;
import com.example.tpo_mobile.network.AuthInterceptor;

import java.io.File;
import java.time.Duration;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    // Emulador Android -> PC localhost
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    // Dispositivo físico: usá la IP local de tu PC, ej:
    //private static final String BASE_URL = "http://192.168.100.9:8080/";

    @Provides @Singleton
    Cache provideCache(@ApplicationContext Context context) {
        int cacheSize = 10 * 1024 * 1024; // 10 MB
        File cacheDir = new File(context.getCacheDir(), "http-cache");
        return new Cache(cacheDir, cacheSize);
    }

    @Provides @Singleton
    OkHttpClient provideOkHttpClient(Cache cache, AuthInterceptor authInterceptor) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        // En prod conviene ocultar el Authorization:
        logging.redactHeader("Authorization");

        return new OkHttpClient.Builder()
                .cache(cache)
                .readTimeout(Duration.ofSeconds(30))
                .connectTimeout(Duration.ofSeconds(15))
                .writeTimeout(Duration.ofSeconds(30))
                .retryOnConnectionFailure(true)

                // 1) Auth primero (para que TODAS las req tengan el Bearer)
                .addInterceptor(authInterceptor)

                // 2) Logging después (así ves la request final con headers)
                .addInterceptor(logging)

                // 3) Cache: reescribe encabezados SOLO en RESPUESTAS GET
                .addNetworkInterceptor(chain -> {
                    Response resp = chain.proceed(chain.request());
                    if ("GET".equalsIgnoreCase(chain.request().method())) {
                        return resp.newBuilder()
                                .header("Cache-Control", "public, max-age=60") // cache 60s
                                .build();
                    }
                    return resp;
                })
                .build();
    }

    @Provides @Singleton
    Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides @Singleton
    AuthApiService provideAuthApiService(Retrofit retrofit) {
        return retrofit.create(AuthApiService.class);
    }

    @Provides @Singleton
    GymApiService provideGymApiService(Retrofit retrofit) {
        return retrofit.create(GymApiService.class);
    }

    @Provides @Singleton
    ReservationApiService provideReservationApiService(Retrofit retrofit) {
        return retrofit.create(ReservationApiService.class);
    }
}
