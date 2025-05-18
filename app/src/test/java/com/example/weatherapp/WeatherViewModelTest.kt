package com.example.weatherapp.ui.viewmodel

import app.cash.turbine.test
import com.example.weatherapp.data.model.*
import com.example.weatherapp.FakeWeatherRepository
import com.example.weatherapp.data.repository.Result
import com.example.weatherapp.data.FakeResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class WeatherViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeRepository: FakeWeatherRepository
    private lateinit var fakeResourceProvider: FakeResourceProvider
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
        fakeResourceProvider = FakeResourceProvider()
        viewModel = WeatherViewModel(fakeRepository, fakeResourceProvider)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        fakeRepository.reset()
    }

    @Test
    fun `fetchWeatherForCity with blank city name sets error state`() = runTest {
        viewModel.uiState.test {
            viewModel.fetchWeatherForCity(" ")
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            assertEquals("CITY_NAME_CANNOT_BE_EMPTY_FAKE", state.error)
            assertFalse(state.isLoading)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchWeatherForCity when city coordinates not found sets error state`() = runTest {
        val cityName = "NonExistentCity"
        fakeRepository.setCityCoordinatesResponse(Result.Success(emptyList()))

        viewModel.uiState.test {
            viewModel.fetchWeatherForCity(cityName)
            awaitItem()
            val loadingState = awaitItem(); assertTrue(loadingState.isLoading)
            val errorState = awaitItem()

            assertEquals("CITY_NOT_FOUND_FAKE: $cityName", errorState.error)
            assertFalse(errorState.isLoading)
            assertEquals(cityName, fakeRepository.lastSearchedCityName)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchWeatherForCity successful data fetch updates UI state correctly`() = runTest(testDispatcher) {
        val userInputCityName = "London"
        val countryCode = "GB"
        val latitude = 51.5
        val longitude = -0.12


        val fakeCityFromApi = CityResponse(name = "London", latitude = latitude, longitude = longitude, country = countryCode, isCapital = true)
        fakeRepository.setCityCoordinatesResponse(Result.Success(listOf(fakeCityFromApi)))

        val fakeWeatherResponse = WeatherResponse(
            latitude = latitude, longitude = longitude, generationTimeMs = 0.0, utcOffsetSeconds = 0,
            timezone = "Europe/London", timezoneAbbreviation = "GMT", elevation = 35.0,
            hourlyUnits = HourlyUnits(time = "iso", temperature2m = "°C"),
            hourly = HourlyData(time = listOf("2024-05-20T12:00"), temperature2m = listOf(18.5)),
            dailyUnits = DailyUnits(time = "iso", weatherCode = "", temperature2mMax = "°C", temperature2mMin = "°C"),
            daily = DailyData(
                time = listOf(LocalDate.now().toString()), weatherCode = listOf(0),
                temperature2mMax = listOf(22.0), temperature2mMin = listOf(12.0)
            )
        )
        fakeRepository.setWeatherResponse(Result.Success(fakeWeatherResponse))
        fakeResourceProvider.setString(com.example.weatherapp.R.string.weather_description_clear_sky, "CLEAR_SKY_FAKE_DESC")


        viewModel.uiState.test {
            viewModel.fetchWeatherForCity(userInputCityName)

            awaitItem()
            val loadingState = awaitItem(); assertTrue(loadingState.isLoading)
            val successState = awaitItem()

            assertFalse(successState.isLoading)
            assertNull(successState.error)

            assertNotNull(successState.weatherData)
            assertEquals(userInputCityName, successState.weatherData?.cityName)
            assertNotNull(successState.weatherData?.country)

            assertEquals("18.5 °C", successState.weatherData?.temperature)

            assertFalse(successState.dailyForecast.isEmpty())
            assertEquals("CLEAR_SKY_FAKE_DESC", successState.dailyForecast[0].weatherDescription)
            assertEquals("22°C", successState.dailyForecast[0].maxTemp)

            assertEquals(userInputCityName, fakeRepository.lastSearchedCityName) // No transliteration
            assertEquals(Pair(latitude, longitude), fakeRepository.lastCoordsForWeather)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchWeatherForCity when repository throws exception for city coordinates`() = runTest {
        val cityName = "ExceptionCity"
        val exception = RuntimeException("Fake DB error for city")
        val expectedRepoErrorMessage = "Network error fetching coordinates: ${exception.message}"

        fakeRepository.setCityCoordinatesException(exception)

        viewModel.uiState.test {
            viewModel.fetchWeatherForCity(cityName)
            awaitItem();
            val loadingState = awaitItem(); assertTrue(loadingState.isLoading);
            val errorState = awaitItem()

            assertEquals(expectedRepoErrorMessage, errorState.error)
            assertFalse(errorState.isLoading)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchWeatherForCity when repository returns error for city coordinates`() = runTest {
        val cityName = "ApiErrorCity"
        val repoErrorMessage = "API Ninjas Error: 404 Not Found"
        fakeRepository.setCityCoordinatesResponse(Result.Error(repoErrorMessage))

        viewModel.uiState.test {
            viewModel.fetchWeatherForCity(cityName)
            awaitItem()
            val loadingState = awaitItem(); assertTrue(loadingState.isLoading);
            val errorState = awaitItem()

            assertEquals(repoErrorMessage, errorState.error)
            assertFalse(errorState.isLoading)
            cancelAndConsumeRemainingEvents()
        }
    }


    @Test
    fun `fetchWeatherForCity when repository returns error for weather data`() = runTest {
        val cityName = "London"
        val latitude = 51.5
        val longitude = -0.12
        val repoErrorMessage = "Open-Meteo API Error: 500 Server Error"

        val fakeCityFromApi = CityResponse(name = "London", latitude = latitude, longitude = longitude, country = "GB", isCapital = true)
        fakeRepository.setCityCoordinatesResponse(Result.Success(listOf(fakeCityFromApi)))
        fakeRepository.setWeatherResponse(Result.Error(repoErrorMessage))

        viewModel.uiState.test {
            viewModel.fetchWeatherForCity(cityName)
            awaitItem();
            val loadingState = awaitItem(); assertTrue(loadingState.isLoading);
            val errorState = awaitItem()

            assertEquals(repoErrorMessage, errorState.error)
            assertFalse(errorState.isLoading)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchWeatherForCity when repository throws exception for weather data`() = runTest {
        val cityName = "London"
        val latitude = 51.5
        val longitude = -0.12
        val exception = RuntimeException("Fake network error for weather")
        val expectedRepoErrorMessage = "Network error fetching weather: ${exception.message}"


        val fakeCityFromApi = CityResponse(name = "London", latitude = latitude, longitude = longitude, country = "GB", isCapital = true)
        fakeRepository.setCityCoordinatesResponse(Result.Success(listOf(fakeCityFromApi)))
        fakeRepository.setWeatherException(exception)


        viewModel.uiState.test {
            viewModel.fetchWeatherForCity(cityName)
            awaitItem();
            val loadingState = awaitItem(); assertTrue(loadingState.isLoading);
            val errorState = awaitItem()

            assertEquals(expectedRepoErrorMessage, errorState.error)
            assertFalse(errorState.isLoading)
            cancelAndConsumeRemainingEvents()
        }
    }


    @Test
    fun `clearError sets error to null`() = runTest {
        fakeRepository.setCityCoordinatesResponse(Result.Error("INITIAL_ERROR_FAKE"))
        viewModel.fetchWeatherForCity("SomeCity")

        viewModel.uiState.test {
            var currentState = awaitItem()
            while(currentState.error == null && !currentState.isLoading) { currentState = awaitItem() }
            while(currentState.isLoading) { currentState = awaitItem() }
            assertNotNull(currentState.error)

            viewModel.clearError()
            val clearedState = awaitItem()
            assertNull(clearedState.error)
            cancelAndConsumeRemainingEvents()
        }
    }
}