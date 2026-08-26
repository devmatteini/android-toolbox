package com.cosimomatteini.toolbox.build

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFileCodec
import com.cosimomatteini.toolbox.currencyrates.FrankfurterCurrencyRates
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyRatesTest {
    @Test
    fun `parses and normalizes ECB rates`() {
        val rates = FrankfurterCurrencyRates.parse(VALID_RESPONSE)

        assertEquals(LocalDate.parse("2026-08-25"), rates.rateDate)
        assertEquals(0, BigDecimal.ONE.compareTo(rates.rates[CurrencyCode.EUR]))
        assertEquals(BigDecimal("1.1662"), rates.rates[CurrencyCode.USD])
    }

    @Test
    fun `generates packaged schema`() {
        val json = CurrencyRatesFileCodec.encode(rates())

        assertTrue(json.contains("\"schemaVersion\": 1"))
        assertTrue(json.contains("\"id\": \"ECB\""))
        assertTrue(json.contains("\"name\": \"European Central Bank\""))
        assertTrue(json.contains("\"EUR\": \"1\""))
    }

    @Test
    fun `generated schema decodes with the shared codec`() {
        val json = CurrencyRatesFileCodec.encode(rates())
        val decoded = CurrencyRatesFileCodec.decode(json)

        assertEquals(CurrencyCode.EUR, decoded.base)
        assertEquals(Instant.parse("2026-08-25T12:00:00Z"), decoded.downloadedAt)
        assertEquals(LocalDate.parse("2026-08-25"), decoded.rateDate)
        assertEquals(BigDecimal.ONE, decoded.rates[CurrencyCode.EUR])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects invalid packaged rate`() {
        CurrencyRatesFileCodec.decode(
            CurrencyRatesFileCodec.encode(rates()).replace("\"EUR\": \"1\"", "\"EUR\": \"0\"")
        )
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

    private fun rates() = FrankfurterCurrencyRates(
        LocalDate.parse("2026-08-25"),
        mapOf(CurrencyCode.EUR to BigDecimal.ONE)
    ).toFile(URI("https://example.test/rates"), Instant.parse("2026-08-25T12:00:00Z"))

    private companion object {
        const val VALID_RESPONSE = """
            [
              {"date":"2026-08-25","base":"EUR","quote":"USD","rate":1.1662},
              {"date":"2026-08-25","base":"EUR","quote":"EUR","rate":1.0}
            ]
        """
    }
}
