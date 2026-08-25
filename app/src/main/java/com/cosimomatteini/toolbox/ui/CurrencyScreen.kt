package com.cosimomatteini.toolbox.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_BASE
import com.cosimomatteini.toolbox.domain.CurrencyUnit
import com.cosimomatteini.toolbox.features.ConvertCurrency
import java.text.Collator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

@Composable
fun CurrencyScreen(convertCurrency: ConvertCurrency, onBack: () -> Unit) {
    val sourceUnit = convertCurrency.units.single { it.code == CURRENCY_RATES_BASE }
    val targetUnit = convertCurrency.units.single { it.code == USD }
    val locale = Locale.getDefault()
    val units = orderedCurrencyUnits(convertCurrency.units, locale)
    val viewModel = viewModel<ConverterViewModel<CurrencyUnit>>(
        key = "currency",
        factory = viewModelFactory {
            initializer {
                ConverterViewModel(
                    sourceUnit = sourceUnit,
                    targetUnit = targetUnit,
                    convert = convertCurrency::convert
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    ConverterScreen(
        title = stringResource(R.string.tool_currency),
        units = units,
        uiState = uiState,
        unitLabel = { unit -> currencyLabel(unit, locale) },
        onBack = onBack,
        onSourceUnitSelected = viewModel::onSourceUnitSelected,
        onTargetUnitSelected = viewModel::onTargetUnitSelected,
        onDigit = viewModel::onDigit,
        onDecimal = viewModel::onDecimal,
        onDelete = viewModel::onDelete,
        onClear = viewModel::onClear,
        onSwap = viewModel::onSwap,
        additionalContent = {
            Text(
                text = stringResource(
                    R.string.currency_rate_source,
                    convertCurrency.providerName,
                    formatSourceDate(convertCurrency.rateDate, locale)
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    )
}

internal fun currencyLabel(unit: CurrencyUnit, locale: Locale): String =
    "${currencyName(unit.code, locale)} (${unit.code})"

internal fun orderedCurrencyUnits(units: List<CurrencyUnit>, locale: Locale): List<CurrencyUnit> {
    val collator = Collator.getInstance(locale)
    return units.sortedWith(compareBy(collator) { currencyName(it.code, locale) })
}

internal fun formatSourceDate(value: String, locale: Locale): String =
    LocalDate.parse(value).format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    )

private fun currencyName(code: String, locale: Locale): String = try {
    Currency.getInstance(code).getDisplayName(locale)
} catch (_: IllegalArgumentException) {
    code
}

private const val USD = "USD"
