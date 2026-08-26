package com.cosimomatteini.toolbox.currencyrates

import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyRatesFileCodecTest {
    @Test
    fun `generates packaged schema`() {
        val json = CurrencyRatesFileCodec.encode(rates())

        assertTrue(json.contains("\"schemaVersion\": 1"))
        assertTrue(json.contains("\"id\": \"ECB\""))
        assertTrue(json.contains("\"name\": \"European Central Bank\""))
        assertTrue(json.contains("\"EUR\": \"1\""))
    }

    @Test
    fun `decodes packaged schema into typed rates`() {
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

    private fun rates() = CurrencyRatesFile.create(
        provider = CurrencyRateProvider.EuropeanCentralBank,
        sourceUrl = URI("https://example.test/rates"),
        downloadedAt = Instant.parse("2026-08-25T12:00:00Z"),
        rateDate = LocalDate.parse("2026-08-25"),
        base = CurrencyCode.EUR,
        rates = mapOf(CurrencyCode.EUR to BigDecimal.ONE)
    )
}
