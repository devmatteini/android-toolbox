package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import com.cosimomatteini.toolbox.domain.ReadOnlyCurrencyRatesRepository

class LoadCurrencyRates(
    private val defaultCurrencyRates: ReadOnlyCurrencyRatesRepository,
    private val currencyRates: CurrencyRatesRepository
) {
    fun load(): CurrencyRates = currencyRates.load() ?: defaultCurrencyRates.load()
}
