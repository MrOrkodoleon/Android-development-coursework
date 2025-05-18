package com.example.weatherapp.data.repository

import com.example.weatherapp.data.model.CityResponse
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.network.CityApiService
import com.example.weatherapp.network.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val cityApi: CityApiService,
    private val weatherApi: WeatherApiService
) {
    suspend fun getCityCoordinates(cityName: String): Result<List<CityResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = cityApi.getCityCoordinates(cityName)
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error("API Ninjas Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Network error fetching coordinates: ${e.localizedMessage}", e)
            }
        }
    }

    suspend fun getWeather(latitude: Double, longitude: Double): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApi.getWeather(latitude, longitude)
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error("Open-Meteo Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Network error fetching weather: ${e.localizedMessage}", e)
            }
        }
    }
}