package com.cosimomatteini.toolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosimomatteini.toolbox.domain.ToolId
import com.cosimomatteini.toolbox.ui.AreaScreen
import com.cosimomatteini.toolbox.ui.CompassScreen
import com.cosimomatteini.toolbox.ui.CurrencyScreen
import com.cosimomatteini.toolbox.ui.HomeScreen
import com.cosimomatteini.toolbox.ui.HomeViewModel
import com.cosimomatteini.toolbox.ui.LengthScreen
import com.cosimomatteini.toolbox.ui.MassScreen
import com.cosimomatteini.toolbox.ui.SettingsScreen
import com.cosimomatteini.toolbox.ui.SettingsUiState
import com.cosimomatteini.toolbox.ui.SettingsViewModel
import com.cosimomatteini.toolbox.ui.SpeedScreen
import com.cosimomatteini.toolbox.ui.TemperatureScreen
import com.cosimomatteini.toolbox.ui.VolumeScreen
import com.cosimomatteini.toolbox.ui.theme.ToolboxTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { ToolboxAppContainer(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToolboxTheme {
                var screen by rememberSaveable { mutableStateOf(AppScreen.Home) }
                var converterEntry by rememberSaveable { mutableStateOf(0) }
                BackHandler(enabled = screen != AppScreen.Home) { screen = AppScreen.Home }
                val settingsViewModel = viewModel<SettingsViewModel>(
                    factory = viewModelFactory {
                        initializer {
                            SettingsViewModel(
                                appContainer.converterDefaultsStore,
                                appContainer.loadCurrencyRates
                            )
                        }
                    }
                )
                val settingsUiState by settingsViewModel.uiState.collectAsState()
                val defaults = (settingsUiState as? SettingsUiState.Content)?.defaults

                when (screen) {
                    AppScreen.Home -> {
                        val homeViewModel = viewModel<HomeViewModel>(
                            factory = viewModelFactory {
                                initializer { HomeViewModel(appContainer.tools) }
                            }
                        )
                        val uiState by homeViewModel.uiState.collectAsState()

                        HomeScreen(
                            uiState = uiState,
                            onToolClick = { tool ->
                                screen = when (tool.id) {
                                    ToolId.Area -> AppScreen.Area
                                    ToolId.Length -> AppScreen.Length
                                    ToolId.Mass -> AppScreen.Mass
                                    ToolId.Temperature -> AppScreen.Temperature
                                    ToolId.Speed -> AppScreen.Speed
                                    ToolId.Volume -> AppScreen.Volume
                                    ToolId.Compass -> AppScreen.Compass
                                    ToolId.Currency -> AppScreen.Currency
                                }
                                converterEntry++
                            },
                            onSettingsClick = {
                                settingsViewModel.onSettingsOpened()
                                screen = AppScreen.Settings
                            }
                        )
                    }

                    AppScreen.Area -> {
                        defaults?.let {
                            AreaScreen(
                                convertArea = appContainer.convertArea,
                                initialUnits = it.area,
                                viewModelKey = "area-$converterEntry",
                                onBack = { screen = AppScreen.Home }
                            )
                        } ?: DefaultsLoading()
                    }

                    AppScreen.Compass -> {
                        CompassScreen(
                            observeCompass = appContainer.observeCompass,
                            onBack = { screen = AppScreen.Home }
                        )
                    }

                    AppScreen.Currency -> {
                        defaults?.let {
                            CurrencyScreen(
                                loadCurrencyRates = appContainer.loadCurrencyRates,
                                refreshCurrencyRates = appContainer.refreshCurrencyRates,
                                initialUnitCodes = it.currency,
                                viewModelKey = "currency-$converterEntry",
                                onBack = { screen = AppScreen.Home }
                            )
                        } ?: DefaultsLoading()
                    }

                    AppScreen.Length -> {
                        defaults?.let {
                            LengthScreen(
                                convertLength = appContainer.convertLength,
                                initialUnits = it.length,
                                viewModelKey = "length-$converterEntry",
                                onBack = { screen = AppScreen.Home }
                            )
                        } ?: DefaultsLoading()
                    }

                    AppScreen.Mass -> {
                        defaults?.let {
                            MassScreen(
                                convertMass = appContainer.convertMass,
                                initialUnits = it.mass,
                                viewModelKey = "mass-$converterEntry",
                                onBack = { screen = AppScreen.Home }
                            )
                        } ?: DefaultsLoading()
                    }

                    AppScreen.Temperature -> {
                        defaults?.let {
                            TemperatureScreen(
                                convertTemperature = appContainer.convertTemperature,
                                initialUnits = it.temperature,
                                viewModelKey = "temperature-$converterEntry",
                                onBack = { screen = AppScreen.Home }
                            )
                        } ?: DefaultsLoading()
                    }

                    AppScreen.Speed -> {
                        defaults?.let {
                            SpeedScreen(
                                convertSpeed = appContainer.convertSpeed,
                                initialUnits = it.speed,
                                viewModelKey = "speed-$converterEntry",
                                onBack = { screen = AppScreen.Home }
                            )
                        } ?: DefaultsLoading()
                    }

                    AppScreen.Volume -> {
                        defaults?.let {
                            VolumeScreen(
                                convertVolume = appContainer.convertVolume,
                                initialUnits = it.volume,
                                viewModelKey = "volume-$converterEntry",
                                onBack = { screen = AppScreen.Home }
                            )
                        } ?: DefaultsLoading()
                    }

                    AppScreen.Settings -> {
                        SettingsScreen(
                            uiState = settingsUiState,
                            onSelect = settingsViewModel::select,
                            onPersistenceErrorShown = settingsViewModel::onPersistenceErrorShown,
                            onBack = { screen = AppScreen.Home }
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun DefaultsLoading() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator()
    }
}

private enum class AppScreen {
    Home,
    Area,
    Compass,
    Currency,
    Length,
    Mass,
    Temperature,
    Speed,
    Volume,
    Settings
}
