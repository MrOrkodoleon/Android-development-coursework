package com.example.weatherapp.ui.viewmodel

import com.example.weatherapp.data.model.UiWeatherData

data class WeatherScreenState(
    val weatherData: UiWeatherData? = null,
    val dailyForecast: List<DailyForecastUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)