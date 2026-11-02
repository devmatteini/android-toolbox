package com.cosimomatteini.toolbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.features.ConvertLength

@Composable
fun LengthScreen(
    convertLength: ConvertLength,
    initialUnits: UnitPair<LengthUnit>,
    viewModelKey: String,
    onBack: () -> Unit
) {
    val viewModel = viewModel<ConverterViewModel<LengthUnit>>(
        key = viewModelKey,
        factory = viewModelFactory {
            initializer {
                ConverterViewModel(
                    sourceUnit = initialUnits.source,
                    targetUnit = initialUnits.target,
                    convert = convertLength::invoke
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    ConverterScreen(
        title = stringResource(R.string.tool_length),
        units = convertLength.units,
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
        onSwap = viewModel::onSwap
    )
}
