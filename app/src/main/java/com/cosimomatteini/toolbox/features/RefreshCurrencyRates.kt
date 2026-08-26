package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.domain.CurrencyExchangeRates
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository

class RefreshCurrencyRates(
    private val currencyRates: CurrencyRatesRepository,
    private val currencyExchangeRates: CurrencyExchangeRates
) {
    suspend fun refresh(): RefreshCurrencyResult = try {
        val rates = currencyExchangeRates.load()
        currencyRates.save(rates)
        RefreshCurrencyResult.Updated(rates)
    } catch (_: Exception) {
        RefreshCurrencyResult.Failed
    }
}

sealed interface RefreshCurrencyResult {
    data class Updated(val rates: CurrencyRates) : RefreshCurrencyResult

    data object Failed : RefreshCurrencyResult
}
