package com.cosimomatteini.toolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.cosimomatteini.toolbox.ui.SpeedScreen
import com.cosimomatteini.toolbox.ui.TemperatureScreen
import com.cosimomatteini.toolbox.ui.VolumeScreen
import com.cosimomatteini.toolbox.ui.theme.ToolboxTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { ToolboxAppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToolboxTheme {
                var screen by rememberSaveable { mutableStateOf(AppScreen.Home) }
                BackHandler(enabled = screen != AppScreen.Home) { screen = AppScreen.Home }

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
                            }
                        )
                    }

                    AppScreen.Area -> {
                        AreaScreen(
                            convertArea = appContainer.convertArea,
                            onBack = { screen = AppScreen.Home }
                        )
                    }

                    AppScreen.Compass -> {
                        CompassScreen(
                            observeCompass = appContainer.observeCompass,
                            onBack = { screen = AppScreen.Home }
                        )
                    }

                    AppScreen.Currency -> {
                        CurrencyScreen(
                            currencyRates = appContainer.currencyRates,
                            onBack = { screen = AppScreen.Home }
                        )
                    }

                    AppScreen.Length -> {
                        LengthScreen(
                            convertLength = appContainer.convertLength,
                            onBack = { screen = AppScreen.Home }
                        )
                    }

                    AppScreen.Mass -> {
                        MassScreen(
                            convertMass = appContainer.convertMass,
                            onBack = { screen = AppScreen.Home }
                        )
                    }

                    AppScreen.Temperature -> {
                        TemperatureScreen(
                            convertTemperature = appContainer.convertTemperature,
                            onBack = { screen = AppScreen.Home }
                        )
                    }

                    AppScreen.Speed -> {
                        SpeedScreen(
                            convertSpeed = appContainer.convertSpeed,
                            onBack = { screen = AppScreen.Home }
                        )
                    }

                    AppScreen.Volume -> {
                        VolumeScreen(
                            convertVolume = appContainer.convertVolume,
                            onBack = { screen = AppScreen.Home }
                        )
                    }
                }
            }
        }
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
    Volume
}
