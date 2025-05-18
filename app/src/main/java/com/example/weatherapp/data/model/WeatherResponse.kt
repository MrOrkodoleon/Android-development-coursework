package com.example.weatherapp.data.model

import com.example.weatherapp.data.model.HourlyData
import com.example.weatherapp.data.model.HourlyUnits
import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("generationtime_ms")
    val generationTimeMs: Double?,
    @SerializedName("utc_offset_seconds")
    val utcOffsetSeconds: Int?,
    val timezone: String?,
    @SerializedName("timezone_abbreviation")
    val timezoneAbbreviation: String?,
    val elevation: Double?,
    @SerializedName("hourly_units")
    val hourlyUnits: HourlyUnits?,
    val hourly: HourlyData?,

    @SerializedName("daily_units")
    val dailyUnits: DailyUnits?,
    val daily: DailyData?
)