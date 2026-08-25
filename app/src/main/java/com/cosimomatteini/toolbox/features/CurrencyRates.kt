package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile
import com.cosimomatteini.toolbox.domain.CurrencyExchangeRates
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import com.cosimomatteini.toolbox.domain.ReadOnlyCurrencyRatesRepository

class CurrencyRates(
    private val defaultCurrencyRates: ReadOnlyCurrencyRatesRepository,
    private val currencyRates: CurrencyRatesRepository,
    private val currencyExchangeRates: CurrencyExchangeRates
) {
    fun load(): CurrencyRatesFile = currencyRates.load() ?: defaultCurrencyRates.load()

    suspend fun refresh(): RefreshCurrencyResult = try {
        val rates = currencyExchangeRates.load()
        currencyRates.save(rates)
        RefreshCurrencyResult.Updated(rates)
    } catch (_: Exception) {
        RefreshCurrencyResult.Failed
    }
}

sealed interface RefreshCurrencyResult {
    data class Updated(val rates: CurrencyRatesFile) : RefreshCurrencyResult

    data object Failed : RefreshCurrencyResult
}
