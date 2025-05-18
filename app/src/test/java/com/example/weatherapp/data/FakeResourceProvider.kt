package com.example.weatherapp.data

import com.example.weatherapp.R

class FakeResourceProvider : ResourceProvider {
    // Store expected strings for tests
    private val stringMap = mutableMapOf<Int, String>()
    private val stringFormatMap = mutableMapOf<Int, (Array<out Any>) -> String>()


    init {
        stringMap[R.string.city_name_cannot_be_empty] = "CITY_NAME_CANNOT_BE_EMPTY_FAKE"
        stringFormatMap[R.string.city_not_found] = { args -> "CITY_NOT_FOUND_FAKE: ${args[0]}" }
        stringFormatMap[R.string.could_not_get_coordinates] = { args -> "COULD_NOT_GET_COORDINATES_FAKE: ${args[0]}" }
        stringMap[R.string.current_temp_na] = "N/A"
        stringMap[R.string.weather_description_rain] = "Rain"
        stringMap[R.string.weather_description_thunderstorm] = "Thunderstorm"
        stringMap[R.string.weather_description_clear_sky] = "Clear sky"
    }

    override fun getString(resId: Int): String {
        return stringMap[resId] ?: "MissingFakeStringRes:$resId"
    }

    override fun getString(resId: Int, vararg formatArgs: Any): String {
        return stringFormatMap[resId]?.invoke(formatArgs)
            ?: "MissingFakeFormatStringRes:$resId ${formatArgs.joinToString()}"
    }

    fun setString(resId: Int, value: String) {
        stringMap[resId] = value
    }
    fun setFormatString(resId: Int, formatter: (Array<out Any>) -> String) {
        stringFormatMap[resId] = formatter
    }
}