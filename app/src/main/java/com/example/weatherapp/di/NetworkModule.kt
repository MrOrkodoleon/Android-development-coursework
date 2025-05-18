package com.example.weatherapp.di

import com.example.weatherapp.network.CityApiService
import com.example.weatherapp.network.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton
import android.app.Application
import com.example.weatherapp.data.AndroidResourceProvider
import com.example.weatherapp.data.ResourceProvider

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val API_NINJAS_BASE_URL = "https://api.api-ninjas.com/"
    private const val OPEN_METEO_BASE_URL = "https://api.open-meteo.com/"

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("ApiNinjasRetrofit")
    fun provideApiNinjasRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_NINJAS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("OpenMeteoRetrofit")
    fun provideOpenMeteoRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OPEN_METEO_BASE_URL)
            .client(okHttpClient) // Can reuse or create a specific one
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCityApiService(@Named("ApiNinjasRetrofit") retrofit: Retrofit): CityApiService {
        return retrofit.create(CityApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(@Named("OpenMeteoRetrofit") retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideResourceProvider(application: Application): ResourceProvider {
        return AndroidResourceProvider(application)
    }
}