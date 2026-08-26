package com.cosimomatteini.toolbox.domain

import com.cosimomatteini.toolbox.currencyrates.CurrencyRates

interface CurrencyExchangeRates {
    suspend fun load(): CurrencyRates
}
