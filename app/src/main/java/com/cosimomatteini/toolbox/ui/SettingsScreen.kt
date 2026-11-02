package com.cosimomatteini.toolbox.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.AreaUnit
import com.cosimomatteini.toolbox.domain.ConverterCategory
import com.cosimomatteini.toolbox.domain.ConverterUnit
import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.domain.MassUnit
import com.cosimomatteini.toolbox.domain.SpeedUnit
import com.cosimomatteini.toolbox.domain.TemperatureUnit
import com.cosimomatteini.toolbox.domain.VolumeUnit
import com.cosimomatteini.toolbox.infrastructure.DefaultSide

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    uiState: SettingsUiState,
    onSelect: (ConverterCategory, DefaultSide, String) -> Unit,
    onPersistenceErrorShown: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.settings_save_failed)
    val content = uiState as? SettingsUiState.Content
    LaunchedEffect(content?.persistenceFailed) {
        if (content?.persistenceFailed == true) {
            snackbarHostState.showSnackbar(errorMessage)
            onPersistenceErrorShown()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (content == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.settings_loading),
                    modifier = Modifier.padding(top = 72.dp)
                )
            }
        } else {
            SettingsContent(content.defaults, onSelect, Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun SettingsContent(
    defaults: SettingsDefaults,
    onSelect: (ConverterCategory, DefaultSide, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val currencyUnits = orderedCurrencyUnits(defaults.currencyUnits, locale)
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SettingsSection(
            stringResource(R.string.tool_length),
            defaults.length,
            LengthUnit.entries,
            { stringResource(it.labelRes()) },
            ConverterCategory.Length,
            { it.name },
            onSelect
        )
        SettingsSection(
            stringResource(R.string.tool_currency),
            defaults.currency,
            currencyUnits,
            { currencyLabel(it, locale) },
            ConverterCategory.Currency,
            { it.code.value },
            onSelect
        )
        SettingsSection(
            stringResource(R.string.tool_speed),
            defaults.speed,
            SpeedUnit.entries,
            { stringResource(it.labelRes()) },
            ConverterCategory.Speed,
            { it.name },
            onSelect
        )
        SettingsSection(
            stringResource(R.string.tool_temperature),
            defaults.temperature,
            TemperatureUnit.entries,
            { stringResource(it.labelRes()) },
            ConverterCategory.Temperature,
            { it.name },
            onSelect
        )
        SettingsSection(
            stringResource(R.string.tool_mass),
            defaults.mass,
            MassUnit.entries,
            { stringResource(it.labelRes()) },
            ConverterCategory.Mass,
            { it.name },
            onSelect
        )
        SettingsSection(
            stringResource(R.string.tool_volume),
            defaults.volume,
            VolumeUnit.entries,
            { stringResource(it.labelRes()) },
            ConverterCategory.Volume,
            { it.name },
            onSelect
        )
        SettingsSection(
            stringResource(R.string.tool_area),
            defaults.area,
            AreaUnit.entries,
            { stringResource(it.labelRes()) },
            ConverterCategory.Area,
            { it.name },
            onSelect
        )
    }
}

@Composable
private fun <U : ConverterUnit> SettingsSection(
    categoryLabel: String,
    pair: UnitPair<U>,
    units: List<U>,
    unitLabel: @Composable (U) -> String,
    category: ConverterCategory,
    unitId: (U) -> String,
    onSelect: (ConverterCategory, DefaultSide, String) -> Unit
) {
    val sourceDescription = stringResource(R.string.settings_source_selector, categoryLabel)
    val targetDescription = stringResource(R.string.settings_target_selector, categoryLabel)
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(categoryLabel, style = MaterialTheme.typography.titleLarge)
        SettingsSelectors(
            sourceDescription = sourceDescription,
            targetDescription = targetDescription,
            pair = pair,
            units = units,
            unitLabel = unitLabel,
            onSourceSelected = { onSelect(category, DefaultSide.Source, unitId(it)) },
            onTargetSelected = { onSelect(category, DefaultSide.Target, unitId(it)) }
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun <U : ConverterUnit> SettingsSelectors(
    sourceDescription: String,
    targetDescription: String,
    pair: UnitPair<U>,
    units: List<U>,
    unitLabel: @Composable (U) -> String,
    onSourceSelected: (U) -> Unit,
    onTargetSelected: (U) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        maxItemsInEachRow = 3,
        modifier = Modifier.fillMaxWidth()
    ) {
        UnitSelector(
            label = sourceDescription,
            selected = pair.source,
            units = units,
            unitLabel = unitLabel,
            onUnitSelected = onSourceSelected,
            textStyle = MaterialTheme.typography.titleMedium
        )
        Text(
            stringResource(R.string.settings_to),
            modifier = Modifier.padding(top = 12.dp)
        )
        UnitSelector(
            label = targetDescription,
            selected = pair.target,
            units = units,
            unitLabel = unitLabel,
            onUnitSelected = onTargetSelected,
            textStyle = MaterialTheme.typography.titleMedium
        )
    }
}
