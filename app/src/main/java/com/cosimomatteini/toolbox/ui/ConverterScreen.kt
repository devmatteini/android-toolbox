package com.cosimomatteini.toolbox.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.ConverterUnit
import com.cosimomatteini.toolbox.domain.decimalSeparator
import com.cosimomatteini.toolbox.domain.formatDecimal
import com.cosimomatteini.toolbox.domain.parseDecimal
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun <U : ConverterUnit> ConverterScreen(
    title: String,
    units: List<U>,
    uiState: ConverterUiState<U>,
    unitLabel: @Composable (U) -> String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    additionalContent: @Composable ColumnScope.() -> Unit = {},
    onSourceUnitSelected: (U) -> Unit,
    onTargetUnitSelected: (U) -> Unit,
    onDigit: (Int) -> Unit,
    onDecimal: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onSwap: () -> Unit,
    showSignToggle: Boolean = false,
    onToggleSign: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.converter_back)
                        )
                    }
                },
                actions = actions
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ConversionCard(
                label = stringResource(R.string.converter_source_unit),
                unit = uiState.sourceUnit,
                units = units,
                unitLabel = unitLabel,
                value = uiState.sourceValue.formatForDisplay(locale),
                valueDescription = stringResource(R.string.converter_source_value),
                onUnitSelected = onSourceUnitSelected
            )
            ConversionCard(
                label = stringResource(R.string.converter_target_unit),
                unit = uiState.targetUnit,
                units = units,
                unitLabel = unitLabel,
                value = uiState.targetValue.formatForDisplay(locale),
                valueDescription = stringResource(R.string.converter_converted_value),
                onUnitSelected = onTargetUnitSelected
            )
            Text(
                text = stringResource(
                    R.string.converter_equivalence,
                    uiState.sourceUnit.symbol,
                    uiState.equivalenceValue.formatForDisplay(locale),
                    uiState.targetUnit.symbol
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            additionalContent()
            Spacer(modifier = Modifier.height(8.dp))
            Keypad(
                onDigit = onDigit,
                onDecimal = onDecimal,
                onDelete = onDelete,
                onClear = onClear,
                onSwap = onSwap,
                showSignToggle = showSignToggle,
                onToggleSign = onToggleSign
            )
        }
    }
}

private fun String.formatForDisplay(locale: Locale): String {
    val separator = decimalSeparator(locale)
    val formatted = parseDecimal(this, locale)?.let {
        formatDecimal(it, locale, useGrouping = true)
    }
    return when {
        formatted == null -> this
        endsWith(separator) -> "$formatted$separator"
        else -> formatted
    }
}

@Composable
private fun <U : ConverterUnit> ConversionCard(
    label: String,
    unit: U,
    units: List<U>,
    unitLabel: @Composable (U) -> String,
    value: String,
    valueDescription: String,
    onUnitSelected: (U) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            UnitSelector(
                label = label,
                selected = unit,
                units = units,
                unitLabel = unitLabel,
                onUnitSelected = onUnitSelected
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "$valueDescription $value" }
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = unit.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun <U : ConverterUnit> UnitSelector(
    label: String,
    selected: U,
    units: List<U>,
    unitLabel: @Composable (U) -> String,
    onUnitSelected: (U) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedLabel = unitLabel(selected)
    val unitOptions = units.map { unit -> unit to unitLabel(unit) }
    val filteredOptions = filterUnitOptions(unitOptions, query)

    Box {
        TextButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.semantics { contentDescription = "$label $selectedLabel" }
        ) {
            Text(text = selectedLabel, style = MaterialTheme.typography.titleLarge)
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = {
                expanded = false
                query = ""
            },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.converter_select_unit),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.converter_search_units)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                if (filteredOptions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.converter_no_units_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredOptions, key = { it.first.symbol }) { (unit, unitName) ->
                            ListItem(
                                headlineContent = { Text(unitName) },
                                trailingContent = { Text(unit.symbol) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onUnitSelected(unit)
                                        expanded = false
                                        query = ""
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun <U : ConverterUnit> filterUnitOptions(
    unitOptions: List<Pair<U, String>>,
    query: String
): List<Pair<U, String>> {
    val search = query.trim()
    return if (search.isEmpty()) {
        unitOptions
    } else {
        unitOptions.filter { (unit, label) ->
            label.contains(search, ignoreCase = true) ||
                unit.symbol.contains(search, ignoreCase = true)
        }
    }
}

@Composable
private fun Keypad(
    onDigit: (Int) -> Unit,
    onDecimal: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onSwap: () -> Unit,
    showSignToggle: Boolean,
    onToggleSign: () -> Unit
) {
    val swapLabel = stringResource(R.string.converter_swap)
    val clearLabel = stringResource(R.string.converter_clear)
    val deleteLabel = stringResource(R.string.converter_delete)
    val decimalLabel = stringResource(R.string.converter_decimal)
    val signLabel = stringResource(R.string.converter_toggle_sign)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        KeypadRow {
            DigitButton(7, onDigit)
            DigitButton(8, onDigit)
            DigitButton(9, onDigit)
            KeypadButton(onClick = onSwap, contentDescription = swapLabel, emphasized = true) {
                Icon(Icons.Outlined.SwapVert, contentDescription = null)
            }
        }
        KeypadRow {
            DigitButton(4, onDigit)
            DigitButton(5, onDigit)
            DigitButton(6, onDigit)
            KeypadButton(onClick = onClear, contentDescription = clearLabel, emphasized = true) {
                Text("AC", style = MaterialTheme.typography.titleLarge)
            }
        }
        KeypadRow {
            DigitButton(1, onDigit)
            DigitButton(2, onDigit)
            DigitButton(3, onDigit)
            if (showSignToggle) {
                KeypadButton(
                    onClick = onToggleSign,
                    contentDescription = signLabel,
                    emphasized = true
                ) {
                    Text("±", style = MaterialTheme.typography.headlineMedium)
                }
            } else {
                KeypadSpacer()
            }
        }
        KeypadRow {
            DigitButton(0, onDigit)
            KeypadButton(onClick = onDecimal, contentDescription = decimalLabel) {
                Text(
                    decimalSeparator(Locale.getDefault()).toString(),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            KeypadButton(onClick = onDelete, contentDescription = deleteLabel) {
                Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = null)
            }
            KeypadSpacer()
        }
    }
}

@Composable
private fun KeypadRow(content: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
private fun RowScope.KeypadSpacer() {
    Spacer(
        modifier = Modifier
            .weight(1f)
            .height(KEYPAD_BUTTON_HEIGHT)
    )
}

@Composable
private fun RowScope.DigitButton(digit: Int, onDigit: (Int) -> Unit) {
    KeypadButton(onClick = { onDigit(digit) }, contentDescription = digit.toString()) {
        Text(digit.toString(), style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun RowScope.KeypadButton(
    onClick: () -> Unit,
    contentDescription: String,
    emphasized: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val modifier = Modifier
        .weight(1f)
        .height(KEYPAD_BUTTON_HEIGHT)
        .semantics { this.contentDescription = contentDescription }

    if (emphasized) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            contentPadding = PaddingValues(0.dp),
            content = content
        )
    } else {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            contentPadding = PaddingValues(0.dp),
            content = content
        )
    }
}

private val KEYPAD_BUTTON_HEIGHT = 64.dp
