package com.cosimomatteini.toolbox.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.domain.CurrencyUnit
import com.cosimomatteini.toolbox.features.ConvertCurrency
import com.cosimomatteini.toolbox.features.LoadCurrencyRates
import com.cosimomatteini.toolbox.features.RefreshCurrencyRates
import java.text.Collator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

@Composable
fun CurrencyScreen(
    loadCurrencyRates: LoadCurrencyRates,
    refreshCurrencyRates: RefreshCurrencyRates,
    onBack: () -> Unit
) {
    val ratesViewModel = viewModel<CurrencyRatesViewModel>(
        key = "currencyRates",
        factory = viewModelFactory {
            initializer { CurrencyRatesViewModel(loadCurrencyRates, refreshCurrencyRates) }
        }
    )
    val ratesUiState by ratesViewModel.uiState.collectAsState()
    val rates = ratesUiState.rates
    if (rates == null) {
        CurrencyLoadingScreen(onBack)
        return
    }
    val context = LocalContext.current
    val refreshMessage = ratesUiState.message?.let { stringResource(it.stringRes) }
    val convertCurrency = remember(rates) { ConvertCurrency(rates) }
    val sourceUnit = convertCurrency.units.single { it.code == CurrencyCode.EUR }
    val targetUnit = convertCurrency.units.singleOrNull { it.code == CurrencyCode.USD }
        ?: sourceUnit
    val locale = LocalConfiguration.current.locales[0]
    val units = orderedCurrencyUnits(convertCurrency.units, locale)
    val viewModel = viewModel<ConverterViewModel<CurrencyUnit>>(
        key = "currency",
        factory = viewModelFactory {
            initializer {
                ConverterViewModel(
                    sourceUnit = sourceUnit,
                    targetUnit = targetUnit,
                    convert = convertCurrency::invoke
                )
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(convertCurrency) {
        viewModel.onConverterUpdated(convertCurrency::invoke) { unit ->
            convertCurrency.units.singleOrNull { it.code == unit.code } ?: unit
        }
    }
    LaunchedEffect(ratesUiState.message) {
        refreshMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            ratesViewModel.onRefreshMessageShown()
        }
    }

    ConverterScreen(
        title = stringResource(R.string.tool_currency),
        units = units,
        uiState = uiState,
        onLocaleChanged = viewModel::onLocaleChanged,
        unitLabel = { unit -> currencyLabel(unit, locale) },
        onBack = onBack,
        actions = {
            IconButton(
                onClick = ratesViewModel::onRefreshRequested,
                enabled = !ratesUiState.isRefreshing
            ) {
                if (ratesUiState.isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.padding(12.dp))
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = stringResource(R.string.currency_refresh),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
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
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CurrencyLoadingScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tool_currency)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.converter_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CircularProgressIndicator()
        }
    }
}

internal fun currencyLabel(unit: CurrencyUnit, locale: Locale): String =
    currencyName(unit.code, locale).replaceFirstChar { character ->
        character.titlecase(locale)
    }

internal fun orderedCurrencyUnits(units: List<CurrencyUnit>, locale: Locale): List<CurrencyUnit> {
    val collator = Collator.getInstance(locale)
    return units.sortedWith(compareBy(collator) { currencyName(it.code, locale) })
}

internal fun formatSourceDate(value: LocalDate, locale: Locale): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)
        .format(value)

private fun currencyName(code: CurrencyCode, locale: Locale): String =
    Currency.getInstance(code.value).getDisplayName(locale)

private val CurrencyRefreshMessage.stringRes: Int
    get() = when (this) {
        CurrencyRefreshMessage.Succeeded -> R.string.currency_refresh_succeeded
        CurrencyRefreshMessage.Failed -> R.string.currency_refresh_failed
    }
