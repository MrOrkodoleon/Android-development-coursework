package com.example.weatherapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weatherapp.data.model.UiWeatherData
import com.example.weatherapp.ui.viewmodel.WeatherViewModel
import com.example.weatherapp.R

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var cityNameInput by remember { mutableStateOf("Izhevsk") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CityInputRow(
            cityName = cityNameInput,
            onCityNameChange = { cityNameInput = it },
            onSearchClick = {
                viewModel.fetchWeatherForCity(cityNameInput)
                focusManager.clearFocus()
            },
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(vertical = 20.dp))
        } else {
            uiState.weatherData?.let { data ->
                CurrentWeatherDetails(data = data)
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState.dailyForecast.isNotEmpty()) {
                Text(
                    stringResource(R.string.forecast_daily_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    items(uiState.dailyForecast) { dailyItem ->
                        DailyForecastCard(item = dailyItem)
                    }
                }
            } else if (uiState.weatherData != null && !uiState.isLoading) {
                Text(stringResource(R.string.forecast_not_available), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun CityInputRow(
    cityName: String,
    onCityNameChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    isLoading: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = cityName,
            onValueChange = onCityNameChange,
            label = { Text(stringResource(R.string.enter_city_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { onSearchClick() }),
            modifier = Modifier.weight(1f),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSearchClick,
            enabled = !isLoading && cityName.isNotBlank()
        ) {
            Text(stringResource(R.string.search))
        }
    }
}

@Composable
fun CurrentWeatherDetails(data: UiWeatherData) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${data.cityName}, ${data.country}",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = data.temperature,
            style = MaterialTheme.typography.displayLarge
        )
    }
}