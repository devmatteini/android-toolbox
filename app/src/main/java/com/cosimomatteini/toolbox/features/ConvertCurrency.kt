package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile
import com.cosimomatteini.toolbox.domain.CurrencyUnit
import com.cosimomatteini.toolbox.domain.convertCurrency
import java.math.BigDecimal

class ConvertCurrency(rates: CurrencyRatesFile) {
    val units = rates.rates.map { (code, rate) -> CurrencyUnit(code, rate) }
    val providerName = rates.provider.displayName
    val rateDate = rates.rateDate

    fun convert(value: BigDecimal, source: CurrencyUnit, target: CurrencyUnit): BigDecimal =
        convertCurrency(value, source, target)
}
