package com.cosimomatteini.toolbox.domain

import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile

interface CurrencyExchangeRates {
    suspend fun load(): CurrencyRatesFile
}
