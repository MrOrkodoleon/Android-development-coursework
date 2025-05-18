package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class DailyUnits(
    val time: String?,
    @SerializedName("weathercode")
    val weatherCode: String?,
    @SerializedName("temperature_2m_max")
    val temperature2mMax: String?,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: String?
)