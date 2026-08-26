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
fun LengthScreen(convertLength: ConvertLength, onBack: () -> Unit) {
    val viewModel = viewModel<ConverterViewModel<LengthUnit>>(
        key = "length",
        factory = viewModelFactory {
            initializer {
                ConverterViewModel(
                    sourceUnit = LengthUnit.Meter,
                    targetUnit = LengthUnit.Kilometer,
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

private fun LengthUnit.labelRes(): Int = when (this) {
    LengthUnit.Millimeter -> R.string.length_unit_millimeter
    LengthUnit.Centimeter -> R.string.length_unit_centimeter
    LengthUnit.Meter -> R.string.length_unit_meter
    LengthUnit.Kilometer -> R.string.length_unit_kilometer
    LengthUnit.Inch -> R.string.length_unit_inch
    LengthUnit.Foot -> R.string.length_unit_foot
    LengthUnit.Yard -> R.string.length_unit_yard
    LengthUnit.Mile -> R.string.length_unit_mile
}
