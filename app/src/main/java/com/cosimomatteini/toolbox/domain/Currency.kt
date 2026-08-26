package com.cosimomatteini.toolbox.domain

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import java.math.BigDecimal
import java.math.MathContext

data class CurrencyUnit(val code: CurrencyCode, val eurRate: BigDecimal) : ConverterUnit {
    override val category = ConverterCategory.Currency
    override val symbol = code.value
}

fun convertCurrency(value: BigDecimal, source: CurrencyUnit, target: CurrencyUnit): BigDecimal =
    value.multiply(target.eurRate).divide(source.eurRate, MathContext.DECIMAL128)
