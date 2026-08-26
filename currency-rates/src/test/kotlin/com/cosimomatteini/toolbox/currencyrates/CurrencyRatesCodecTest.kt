package com.cosimomatteini.toolbox.currencyrates

import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyRatesCodecTest {
    @Test
    fun `generates packaged schema`() {
        val json = CurrencyRatesCodec.encode(rates())

        assertTrue(json.contains("\"schemaVersion\": 1"))
        assertTrue(json.contains("\"id\": \"ECB\""))
        assertTrue(json.contains("\"name\": \"European Central Bank\""))
        assertTrue(json.contains("\"EUR\": \"1\""))
    }

    @Test
    fun `decodes packaged schema into typed rates`() {
        val json = CurrencyRatesCodec.encode(rates())
        val decoded = CurrencyRatesCodec.decode(json)

        assertEquals(CurrencyRateProvider("ECB", "European Central Bank"), decoded.provider)
        assertEquals(CurrencyCode.EUR, decoded.base)
        assertEquals(Instant.parse("2026-08-25T12:00:00Z"), decoded.downloadedAt)
        assertEquals(LocalDate.parse("2026-08-25"), decoded.rateDate)
        assertEquals(BigDecimal.ONE, decoded.rates[CurrencyCode.EUR])
    }

    @Test
    fun `decodes arbitrary provider metadata`() {
        val json = CurrencyRatesCodec.encode(rates())
            .replace("\"id\": \"ECB\"", "\"id\": \"test\"")
            .replace("\"name\": \"European Central Bank\"", "\"name\": \"Test provider\"")

        assertEquals(
            CurrencyRateProvider("test", "Test provider"),
            CurrencyRatesCodec.decode(json).provider
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects invalid packaged rate`() {
        CurrencyRatesCodec.decode(
            CurrencyRatesCodec.encode(rates()).replace("\"EUR\": \"1\"", "\"EUR\": \"0\"")
        )
    }

    private fun rates() = CurrencyRates.create(
        provider = CurrencyRateProvider(
            CURRENCY_RATES_PROVIDER_ID,
            CURRENCY_RATES_PROVIDER_NAME
        ),
        sourceUrl = URI("https://example.test/rates"),
        downloadedAt = Instant.parse("2026-08-25T12:00:00Z"),
        rateDate = LocalDate.parse("2026-08-25"),
        base = CurrencyCode.EUR,
        rates = mapOf(CurrencyCode.EUR to BigDecimal.ONE)
    )
}
