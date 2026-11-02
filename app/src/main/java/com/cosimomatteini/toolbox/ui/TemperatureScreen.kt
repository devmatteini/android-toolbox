package com.cosimomatteini.toolbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.TemperatureUnit
import com.cosimomatteini.toolbox.features.ConvertTemperature

@Composable
fun TemperatureScreen(
    convertTemperature: ConvertTemperature,
    initialUnits: UnitPair<TemperatureUnit>,
    viewModelKey: String,
    onBack: () -> Unit
) {
    val viewModel = viewModel<ConverterViewModel<TemperatureUnit>>(
        key = viewModelKey,
        factory = viewModelFactory {
            initializer {
                ConverterViewModel(
                    sourceUnit = initialUnits.source,
                    targetUnit = initialUnits.target,
                    convert = convertTemperature::invoke
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    ConverterScreen(
        title = stringResource(R.string.tool_temperature),
        units = convertTemperature.units,
        uiState = uiState,
        onLocaleChanged = viewModel::onLocaleChanged,
        unitLabel = { unit -> stringResource(unit.labelRes()) },
        onBack = onBack,
        onSourceUnitSelected = viewModel::onSourceUnitSelected,
        onTargetUnitSelected = viewModel::onTargetUnitSelected,
        onDigit = viewModel::onDigit,
        onDecimal = viewModel::onDecimal,
        onDelete = viewModel::onDelete,
        onClear = viewModel::onClear,
        onSwap = viewModel::onSwap,
        showSignToggle = true,
        onToggleSign = viewModel::onToggleSign
    )
}
