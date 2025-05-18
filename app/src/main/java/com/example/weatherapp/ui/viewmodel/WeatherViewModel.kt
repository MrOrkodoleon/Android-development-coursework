package com.example.weatherapp.ui.viewmodel

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.R
import com.example.weatherapp.data.ResourceProvider
import com.example.weatherapp.data.model.UiWeatherData
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repository.Result
import com.example.weatherapp.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherScreenState())
    val uiState: StateFlow<WeatherScreenState> = _uiState.asStateFlow()

    fun fetchWeatherForCity(cityName: String) {
        if (cityName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = resourceProvider.getString(R.string.city_name_cannot_be_empty)
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, dailyForecast = emptyList())

        viewModelScope.launch {
            when (val cityResult = repository.getCityCoordinates(cityName)) {
                is Result.Success -> {
                    val cities = cityResult.data
                    if (cities.isNotEmpty()) {
                        val firstCity = cities[0]
                        if (firstCity.latitude != null && firstCity.longitude != null) {
                            fetchWeather(
                                firstCity.name ?: resourceProvider.getString(R.string.unknown_name),
                                firstCity.country ?: resourceProvider.getString(R.string.n_a),
                                firstCity.latitude,
                                firstCity.longitude
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = resourceProvider.getString(R.string.could_not_get_coordinates, cityName)
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resourceProvider.getString(R.string.city_not_found, cityName)
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = cityResult.message
                    )
                }
            }
        }
    }

    private suspend fun fetchWeather(cityName: String, country: String, latitude: Double, longitude: Double) {
        Log.d("WeatherVM", "Fetching weather for $cityName at lat: $latitude, lon: $longitude")
        when (val weatherResult = repository.getWeather(latitude, longitude)) {
            is Result.Success -> {
                val weatherResponse = weatherResult.data
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    weatherData = formatCurrentWeatherData(cityName, country, weatherResponse),
                    dailyForecast = formatDailyForecastData(weatherResponse),
                    error = null
                )
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = weatherResult.message
                )
            }
        }
    }

    private fun formatCurrentWeatherData(cityName: String, country: String, response: WeatherResponse): UiWeatherData {
        val currentTemperatureValue = response.hourly?.temperature2m?.firstOrNull()
        val tempUnit = response.hourlyUnits?.temperature2m ?: "°C"

        val currentTemperatureString = if (currentTemperatureValue != null) {
            "%.1f %s".format(currentTemperatureValue, tempUnit)
        } else {
            resourceProvider.getString(R.string.current_temp_na)
        }
        return UiWeatherData(
            cityName = cityName,
            country = country,
            temperature = currentTemperatureString,
            hourlyForecastSummary = ""
        )
    }

    private fun formatDailyForecastData(response: WeatherResponse): List<DailyForecastUiItem> {
        val dailyItems = mutableListOf<DailyForecastUiItem>()
        val dailyData = response.daily ?: return emptyList()
        val dailyUnits = response.dailyUnits ?: return emptyList()

        val dates = dailyData.time ?: return emptyList()
        val weatherCodes = dailyData.weatherCode ?: return emptyList()
        val maxTemps = dailyData.temperature2mMax ?: return emptyList()
        val minTemps = dailyData.temperature2mMin ?: return emptyList()

        val tempUnit = dailyUnits.temperature2mMax ?: "°C"

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        val outputDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())


        for (i in dates.indices) {
            val dateStr = dates.getOrNull(i)
            val code = weatherCodes.getOrNull(i)
            val maxT = maxTemps.getOrNull(i)
            val minT = minTemps.getOrNull(i)

            if (dateStr != null && code != null && maxT != null && minT != null) {
                try {
                    val localDate = LocalDate.parse(dateStr, formatter)
                    val weatherInfo = getWeatherInfoFromCode(code)
                    dailyItems.add(
                        DailyForecastUiItem(
                            date = localDate.format(outputDateFormatter),
                            dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                            weatherDescription = weatherInfo.description,
                            weatherIcon = weatherInfo.icon,
                            maxTemp = "%.0f%s".format(maxT, tempUnit),
                            minTemp = "%.0f%s".format(minT, tempUnit)
                        )
                    )
                } catch (e: Exception) {
                    Log.e("WeatherVM", "Error parsing daily data for index $i: ${e.message}")
                    // Skip this item or add a placeholder
                }
            }
        }
        return dailyItems
    }

    private data class WeatherInfo(val description: String, val icon: ImageVector)

    private fun getWeatherInfoFromCode(code: Int): WeatherInfo {
        val (descResId, icon) = when (code) {
            0 -> Pair(R.string.weather_description_clear_sky, Icons.Filled.WbSunny)
            1 -> Pair(R.string.weather_description_mainly_clear, Icons.Filled.WbSunny)
            2 -> Pair(R.string.weather_description_partly_cloudy, Icons.Filled.WbCloudy)
            3 -> Pair(R.string.weather_description_overcast, Icons.Filled.Cloud)
            45, 48 -> Pair(R.string.weather_description_fog, Icons.Filled.CloudQueue)
            51, 53, 55 -> Pair(R.string.weather_description_drizzle, Icons.Filled.Grain)
            56, 57 -> Pair(R.string.weather_description_freezing_drizzle, Icons.Filled.AcUnit)
            61, 63, 65 -> Pair(R.string.weather_description_rain, Icons.Filled.Grain)
            66, 67 -> Pair(R.string.weather_description_freezing_rain, Icons.Filled.SevereCold)
            71, 73, 75 -> Pair(R.string.weather_description_snow_fall, Icons.Filled.AcUnit)
            77 -> Pair(R.string.weather_description_snow_grains, Icons.Filled.AcUnit)
            80, 81, 82 -> Pair(R.string.weather_description_rain_showers, Icons.Filled.Grain)
            85, 86 -> Pair(R.string.weather_description_snow_showers, Icons.Filled.AcUnit)
            95 -> Pair(R.string.weather_description_thunderstorm, Icons.Filled.Thunderstorm)
            96, 99 -> Pair(R.string.weather_description_thunderstorm_hail, Icons.Filled.Thunderstorm)
            else -> Pair(R.string.weather_description_unknown, Icons.Filled.QuestionMark)
        }
        return WeatherInfo(resourceProvider.getString(descResId), icon)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}