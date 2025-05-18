package com.example.weatherapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val API_NINJAS_BASE_URL = "https://api.api-ninjas.com/"
    private const val OPEN_METEO_BASE_URL = "https://api.open-meteo.com/"

    val cityApi: CityApiService by lazy {
        Retrofit.Builder()
            .baseUrl(API_NINJAS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CityApiService::class.java)
    }

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_METEO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}