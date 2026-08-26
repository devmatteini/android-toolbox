package com.cosimomatteini.toolbox.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.VolumeUnit
import com.cosimomatteini.toolbox.features.ConvertVolume

@Composable
fun VolumeScreen(convertVolume: ConvertVolume, onBack: () -> Unit) {
    val viewModel = viewModel<ConverterViewModel<VolumeUnit>>(
        key = "volume",
        factory = viewModelFactory {
            initializer {
                ConverterViewModel(
                    sourceUnit = VolumeUnit.Millilitre,
                    targetUnit = VolumeUnit.Litre,
                    convert = convertVolume::invoke
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    ConverterScreen(
        title = stringResource(R.string.tool_volume),
        units = convertVolume.units,
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

private fun VolumeUnit.labelRes(): Int = when (this) {
    VolumeUnit.Millilitre -> R.string.volume_unit_millilitre
    VolumeUnit.Litre -> R.string.volume_unit_litre
    VolumeUnit.CubicMeter -> R.string.volume_unit_cubic_meter
    VolumeUnit.FluidOunce -> R.string.volume_unit_fluid_ounce
    VolumeUnit.Pint -> R.string.volume_unit_pint
    VolumeUnit.Quart -> R.string.volume_unit_quart
    VolumeUnit.ImperialGallon -> R.string.volume_unit_imperial_gallon
}
