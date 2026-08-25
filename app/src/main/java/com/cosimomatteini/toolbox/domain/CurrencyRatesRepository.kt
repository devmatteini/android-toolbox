package com.cosimomatteini.toolbox.domain

import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile

interface ReadOnlyCurrencyRatesRepository {
    fun load(): CurrencyRatesFile
}

interface CurrencyRatesRepository {
    fun load(): CurrencyRatesFile?

    fun save(rates: CurrencyRatesFile)
}

interface CurrencyExchangeRates {
    suspend fun load(): CurrencyRatesFile
}
