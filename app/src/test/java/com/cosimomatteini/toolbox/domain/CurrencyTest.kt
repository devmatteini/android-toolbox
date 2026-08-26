package com.cosimomatteini.toolbox.domain

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyTest {
    @Test
    fun `converts through EUR using decimal precision`() {
        val usd = CurrencyUnit(CurrencyCode.USD, BigDecimal("1.2"))
        val gbp = CurrencyUnit(CurrencyCode.parse("GBP"), BigDecimal("0.8"))

        val result = convertCurrency(BigDecimal("3"), usd, gbp)

        assertEquals(0, BigDecimal("2").compareTo(result))
    }

    @Test
    fun `converts USD to EUR`() {
        val usd = CurrencyUnit(CurrencyCode.USD, BigDecimal("1.2"))
        val eur = CurrencyUnit(CurrencyCode.EUR, BigDecimal.ONE)

        val result = convertCurrency(BigDecimal("1"), usd, eur)

        assertEquals(0, BigDecimal("0.8333333333333333333333333333333333").compareTo(result))
    }
}
