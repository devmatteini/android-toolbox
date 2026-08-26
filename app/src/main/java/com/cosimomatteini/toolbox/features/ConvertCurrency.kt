package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.domain.CurrencyUnit
import com.cosimomatteini.toolbox.domain.convertCurrency
import java.math.BigDecimal

class ConvertCurrency(rates: CurrencyRates) {
    val units = rates.rates.map { (code, rate) -> CurrencyUnit(code, rate) }
    val providerName = rates.provider.name
    val rateDate = rates.rateDate

    operator fun invoke(value: BigDecimal, source: CurrencyUnit, target: CurrencyUnit): BigDecimal =
        convertCurrency(value, source, target)
}
