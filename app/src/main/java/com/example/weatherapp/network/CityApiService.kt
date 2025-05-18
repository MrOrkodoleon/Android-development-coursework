package com.example.weatherapp.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.example.weatherapp.data.model.CityResponse
import retrofit2.Response

interface CityApiService {
    @GET("v1/city")
    suspend fun getCityCoordinates(
        @Query("name") cityName: String,
        @Header("X-Api-Key") apiKey: String = "7rr4Akz2Qh24fVFv9BAgvg==Mh8AjpFlY63IUzKh"
    ): Response<List<CityResponse>>
}