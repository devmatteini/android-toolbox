package com.cosimomatteini.toolbox.domain

import com.cosimomatteini.toolbox.currencyrates.CurrencyRates

interface ReadOnlyCurrencyRatesRepository {
    fun load(): CurrencyRates
}

interface CurrencyRatesRepository {
    fun load(): CurrencyRates?

    fun save(rates: CurrencyRates)
}
