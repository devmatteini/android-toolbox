package com.cosimomatteini.toolbox.build

import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFileCodec
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyRatesTest {
    @Test
    fun `parses and normalizes ECB rates`() {
        val rates = CurrencyRates.parse(VALID_RESPONSE)

        assertEquals(LocalDate.parse("2026-08-25"), rates.rateDate)
        assertEquals(0, BigDecimal.ONE.compareTo(rates.rates["EUR"]))
        assertEquals(BigDecimal("1.1662"), rates.rates["USD"])
    }

    @Test
    fun `generates packaged schema`() {
        val json = CurrencyRates.toJson(
            CurrencyRates(LocalDate.parse("2026-08-25"), mapOf("EUR" to BigDecimal.ONE)),
            "https://example.test/rates",
            Instant.parse("2026-08-25T12:00:00Z")
        )

        assertTrue(json.contains("\"schemaVersion\": 1"))
        assertTrue(json.contains("\"id\": \"ECB\""))
        assertTrue(json.contains("\"name\": \"European Central Bank\""))
        assertTrue(json.contains("\"EUR\": \"1\""))
    }

    @Test
    fun `generated schema decodes with the shared codec`() {
        val json = CurrencyRates.toJson(
            CurrencyRates(LocalDate.parse("2026-08-25"), mapOf("EUR" to BigDecimal.ONE)),
            "https://example.test/rates",
            Instant.parse("2026-08-25T12:00:00Z")
        )

        assertEquals("EUR", CurrencyRatesFileCodec.decode(json).base)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects multiple rate dates`() {
        CurrencyRates.parse(
            VALID_RESPONSE.replace(
                "\"2026-08-25\",\"base\":\"EUR\",\"quote\":\"USD\"",
                "\"2026-08-26\",\"base\":\"EUR\",\"quote\":\"USD\""
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects invalid currency code`() {
        CurrencyRates.parse(VALID_RESPONSE.replace("\"USD\"", "\"US\""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non-positive rate`() {
        CurrencyRates.parse(VALID_RESPONSE.replace("1.1662", "0"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects EUR rate other than one`() {
        CurrencyRates.parse(VALID_RESPONSE.replace("\"EUR\",\"rate\":1.0", "\"EUR\",\"rate\":0.99"))
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
