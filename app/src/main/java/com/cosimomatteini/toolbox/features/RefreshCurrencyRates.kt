package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.domain.CurrencyExchangeRates
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import kotlinx.coroutines.CancellationException

class RefreshCurrencyRates(
    private val currencyRates: CurrencyRatesRepository,
    private val currencyExchangeRates: CurrencyExchangeRates
) {
    suspend operator fun invoke(): RefreshCurrencyResult = try {
        val rates = currencyExchangeRates.load()
        currencyRates.save(rates)
        RefreshCurrencyResult.Updated(rates)
    } catch (exception: Exception) {
        if (exception is CancellationException) throw exception
        RefreshCurrencyResult.Failed
    }
}

sealed interface RefreshCurrencyResult {
    data class Updated(val rates: CurrencyRates) : RefreshCurrencyResult

    data object Failed : RefreshCurrencyResult
}
