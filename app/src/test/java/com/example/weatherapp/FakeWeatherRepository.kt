package com.example.weatherapp

import com.example.weatherapp.data.model.CityResponse
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.Result
import com.example.weatherapp.data.repository.WeatherRepository
import io.mockk.mockk

class FakeWeatherRepository : WeatherRepository(
    mockk(),
    mockk()
) {
    private var cityCoordinatesResult: Result<List<CityResponse>> = Result.Success(emptyList())
    private var cityCoordinatesShouldThrowException: Exception? = null
    var lastSearchedCityName: String? = null
        private set

    fun setCityCoordinatesResponse(result: Result<List<CityResponse>>) {
        this.cityCoordinatesResult = result
        this.cityCoordinatesShouldThrowException = null
    }

    fun setCityCoordinatesException(exception: Exception) {
        this.cityCoordinatesShouldThrowException = exception
    }

    override suspend fun getCityCoordinates(cityName: String): Result<List<CityResponse>> {
        lastSearchedCityName = cityName
        cityCoordinatesShouldThrowException?.let { throw it }
        return cityCoordinatesResult
    }

    // --- For Weather Data ---
    private var weatherDataResult: Result<WeatherResponse> = Result.Error("Weather not set in fake")
    private var weatherDataShouldThrowException: Exception? = null
    var lastCoordsForWeather: Pair<Double, Double>? = null
        private set

    fun setWeatherResponse(result: Result<WeatherResponse>) {
        this.weatherDataResult = result
        this.weatherDataShouldThrowException = null
    }

    fun setWeatherException(exception: Exception) {
        this.weatherDataShouldThrowException = exception
    }

    override suspend fun getWeather(latitude: Double, longitude: Double): Result<WeatherResponse> {
        lastCoordsForWeather = Pair(latitude, longitude)
        weatherDataShouldThrowException?.let { throw it }
        return weatherDataResult
    }

    // Helper to reset state if needed between tests
    fun reset() {
        cityCoordinatesResult = Result.Success(emptyList())
        cityCoordinatesShouldThrowException = null
        lastSearchedCityName = null
        weatherDataResult = Result.Error("Weather not set in fake")
        weatherDataShouldThrowException = null
        lastCoordsForWeather = null
    }
}