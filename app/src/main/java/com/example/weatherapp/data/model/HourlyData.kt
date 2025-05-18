package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class HourlyData(
    val time: List<String?>?,
    @SerializedName("temperature_2m")
    val temperature2m: List<Double?>?
)