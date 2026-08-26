package com.cosimomatteini.toolbox.currencyrates

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class FrankfurterCurrencyRatesTest {
    @Test
    fun `parses and normalizes ECB rates`() {
        val rates = FrankfurterCurrencyRates.parse(VALID_RESPONSE)

        assertEquals(LocalDate.parse("2026-08-25"), rates.rateDate)
        assertEquals(0, BigDecimal.ONE.compareTo(rates.rates[CurrencyCode.EUR]))
        assertEquals(BigDecimal("1.1662"), rates.rates[CurrencyCode.USD])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects multiple rate dates`() {
        FrankfurterCurrencyRates.parse(
            VALID_RESPONSE.replace(
                "\"2026-08-25\",\"base\":\"EUR\",\"quote\":\"USD\"",
                "\"2026-08-26\",\"base\":\"EUR\",\"quote\":\"USD\""
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects invalid currency code`() {
        FrankfurterCurrencyRates.parse(VALID_RESPONSE.replace("\"USD\"", "\"US\""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non-positive rate`() {
        FrankfurterCurrencyRates.parse(VALID_RESPONSE.replace("1.1662", "0"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects EUR rate other than one`() {
        FrankfurterCurrencyRates.parse(
            VALID_RESPONSE.replace("\"EUR\",\"rate\":1.0", "\"EUR\",\"rate\":0.99")
        )
    }

    private companion object {
        const val VALID_RESPONSE = """
            [
              {"date":"2026-08-25","base":"EUR","quote":"USD","rate":1.1662},
              {"date":"2026-08-25","base":"EUR","quote":"EUR","rate":1.0}
            ]
        """
    }
}
