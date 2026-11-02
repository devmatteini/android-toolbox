package com.cosimomatteini.toolbox.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.domain.AreaUnit
import com.cosimomatteini.toolbox.domain.ConverterCategory
import com.cosimomatteini.toolbox.domain.CurrencyUnit
import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.domain.MassUnit
import com.cosimomatteini.toolbox.domain.SpeedUnit
import com.cosimomatteini.toolbox.domain.TemperatureUnit
import com.cosimomatteini.toolbox.domain.VolumeUnit
import com.cosimomatteini.toolbox.features.ConvertCurrency
import com.cosimomatteini.toolbox.features.LoadCurrencyRates
import com.cosimomatteini.toolbox.infrastructure.ConverterDefaults
import com.cosimomatteini.toolbox.infrastructure.ConverterDefaultsStore
import com.cosimomatteini.toolbox.infrastructure.DefaultSide
import com.cosimomatteini.toolbox.infrastructure.initialConverterDefaults
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UnitPair<U>(val source: U, val target: U)

data class SettingsDefaults(
    val length: UnitPair<LengthUnit>,
    val currency: UnitPair<CurrencyUnit>,
    val speed: UnitPair<SpeedUnit>,
    val temperature: UnitPair<TemperatureUnit>,
    val mass: UnitPair<MassUnit>,
    val volume: UnitPair<VolumeUnit>,
    val area: UnitPair<AreaUnit>,
    val currencyUnits: List<CurrencyUnit>
)

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Content(val defaults: SettingsDefaults, val persistenceFailed: Boolean = false) :
        SettingsUiState
}

class SettingsViewModel(
    private val store: ConverterDefaultsStore,
    private val loadCurrencyRates: LoadCurrencyRates,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState = mutableUiState.asStateFlow()
    private var latestDefaults: ConverterDefaults? = null
    private var currencyUnits: List<CurrencyUnit> = emptyList()

    init {
        viewModelScope.launch {
            currencyUnits = loadCurrencyUnits() ?: return@launch
            store.defaults.collect { defaults ->
                latestDefaults = defaults
                publish(defaults, currencyUnits)
            }
        }
    }

    fun onSettingsOpened() {
        if (mutableUiState.value is SettingsUiState.Loading) return
        viewModelScope.launch {
            currencyUnits = loadCurrencyUnits() ?: return@launch
            latestDefaults?.let { publish(it, currencyUnits) }
        }
    }

    fun select(category: ConverterCategory, side: DefaultSide, unitId: String) {
        viewModelScope.launch {
            try {
                store.update(category, side, unitId)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableUiState.update { current ->
                    (current as? SettingsUiState.Content)?.copy(persistenceFailed = true)
                        ?: current
                }
            }
        }
    }

    fun onPersistenceErrorShown() {
        mutableUiState.update { current ->
            (current as? SettingsUiState.Content)?.copy(persistenceFailed = false) ?: current
        }
    }

    private suspend fun loadCurrencyUnits(): List<CurrencyUnit>? = try {
        withContext(dispatcher) { ConvertCurrency(loadCurrencyRates()).units }
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        null
    }

    private fun publish(defaults: ConverterDefaults, currencyUnits: List<CurrencyUnit>) {
        val settings = defaults.toSettingsDefaults(currencyUnits)
        mutableUiState.update { current ->
            SettingsUiState.Content(
                defaults = settings,
                persistenceFailed =
                (current as? SettingsUiState.Content)?.persistenceFailed == true
            )
        }
        repairCurrencyDefaults(defaults, settings.currency)
    }

    private fun repairCurrencyDefaults(defaults: ConverterDefaults, pair: UnitPair<CurrencyUnit>) {
        val saved = defaults[ConverterCategory.Currency]
        if (saved.source != pair.source.code.value) {
            select(ConverterCategory.Currency, DefaultSide.Source, pair.source.code.value)
        }
        if (saved.target != pair.target.code.value) {
            select(ConverterCategory.Currency, DefaultSide.Target, pair.target.code.value)
        }
    }
}

private fun ConverterDefaults.toSettingsDefaults(
    currencyUnits: List<CurrencyUnit>
): SettingsDefaults {
    val currencyByCode = currencyUnits.associateBy { it.code.value }
    val eur = currencyByCode.getValue(CurrencyCode.EUR.value)
    val usd = currencyByCode[CurrencyCode.USD.value] ?: eur
    val currencyIds = this[ConverterCategory.Currency]
    return SettingsDefaults(
        length = enumPair(ConverterCategory.Length),
        currency = UnitPair(
            currencyByCode[currencyIds.source] ?: eur,
            currencyByCode[currencyIds.target] ?: usd
        ),
        speed = enumPair(ConverterCategory.Speed),
        temperature = enumPair(ConverterCategory.Temperature),
        mass = enumPair(ConverterCategory.Mass),
        volume = enumPair(ConverterCategory.Volume),
        area = enumPair(ConverterCategory.Area),
        currencyUnits = currencyUnits
    )
}

private inline fun <reified U : Enum<U>> ConverterDefaults.enumPair(
    category: ConverterCategory
): UnitPair<U> {
    val ids = this[category]
    val initial = initialConverterDefaults[category]
    val values = enumValues<U>().associateBy { it.name }
    return UnitPair(
        source = values[ids.source] ?: values.getValue(initial.source),
        target = values[ids.target] ?: values.getValue(initial.target)
    )
}
