package com.cosimomatteini.toolbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.SpeedUnit
import com.cosimomatteini.toolbox.features.ConvertSpeed

@Composable
fun SpeedScreen(convertSpeed: ConvertSpeed, onBack: () -> Unit) {
    val viewModel = viewModel<ConverterViewModel<SpeedUnit>>(
        key = "speed",
        factory = viewModelFactory {
            initializer {
                ConverterViewModel(
                    sourceUnit = SpeedUnit.MetersPerSecond,
                    targetUnit = SpeedUnit.KilometersPerHour,
                    convert = convertSpeed::convert
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    ConverterScreen(
        title = stringResource(R.string.tool_speed),
        units = convertSpeed.units,
        uiState = uiState,
        unitLabel = { unit -> stringResource(unit.labelRes()) },
        onBack = onBack,
        onSourceUnitSelected = viewModel::onSourceUnitSelected,
        onTargetUnitSelected = viewModel::onTargetUnitSelected,
        onDigit = viewModel::onDigit,
        onDecimal = viewModel::onDecimal,
        onDelete = viewModel::onDelete,
        onClear = viewModel::onClear,
        onSwap = viewModel::onSwap
    )
}

private fun SpeedUnit.labelRes(): Int = when (this) {
    SpeedUnit.MetersPerSecond -> R.string.speed_unit_meters_per_second
    SpeedUnit.KilometersPerHour -> R.string.speed_unit_kilometers_per_hour
    SpeedUnit.FeetPerSecond -> R.string.speed_unit_feet_per_second
    SpeedUnit.MilesPerHour -> R.string.speed_unit_miles_per_hour
}
