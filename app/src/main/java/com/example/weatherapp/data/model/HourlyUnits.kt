package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class HourlyUnits(
    val time: String?,
    @SerializedName("temperature_2m")
    val temperature2m: String?
)