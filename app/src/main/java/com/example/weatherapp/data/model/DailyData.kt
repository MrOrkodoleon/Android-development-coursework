package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class DailyData(
    val time: List<String?>?,
    @SerializedName("weathercode")
    val weatherCode: List<Int?>?,
    @SerializedName("temperature_2m_max")
    val temperature2mMax: List<Double?>?,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: List<Double?>?
)