package com.example.weatherapp.ui.viewmodel

import androidx.compose.ui.graphics.vector.ImageVector

data class DailyForecastUiItem(
    val date: String,
    val dayOfWeek: String,
    val weatherDescription: String,
    val weatherIcon: ImageVector,
    val maxTemp: String,
    val minTemp: String
)