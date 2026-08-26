package com.cosimomatteini.toolbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.AreaUnit
import com.cosimomatteini.toolbox.features.ConvertArea

@Composable
fun AreaScreen(convertArea: ConvertArea, onBack: () -> Unit) {
    val viewModel = viewModel<ConverterViewModel<AreaUnit>>(
        key = "area",
        factory = viewModelFactory {
            initializer {
                ConverterViewModel(
                    sourceUnit = AreaUnit.SquareMeter,
                    targetUnit = AreaUnit.SquareKilometer,
                    convert = convertArea::invoke
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    ConverterScreen(
        title = stringResource(R.string.tool_area),
        units = convertArea.units,
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

private fun AreaUnit.labelRes(): Int = when (this) {
    AreaUnit.SquareCentimeter -> R.string.area_unit_square_centimeter
    AreaUnit.SquareMeter -> R.string.area_unit_square_meter
    AreaUnit.SquareKilometer -> R.string.area_unit_square_kilometer
    AreaUnit.Hectare -> R.string.area_unit_hectare
    AreaUnit.SquareInch -> R.string.area_unit_square_inch
    AreaUnit.SquareFoot -> R.string.area_unit_square_foot
    AreaUnit.SquareYard -> R.string.area_unit_square_yard
    AreaUnit.SquareMile -> R.string.area_unit_square_mile
    AreaUnit.Acre -> R.string.area_unit_acre
}
