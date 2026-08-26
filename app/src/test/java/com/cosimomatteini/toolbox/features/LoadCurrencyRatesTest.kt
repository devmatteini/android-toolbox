package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.currencyrates.CurrencyRateProvider
import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import com.cosimomatteini.toolbox.domain.ReadOnlyCurrencyRatesRepository
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCurrencyRatesTest {
    @Test
    fun `loads persisted currency rates`() {
        val loadCurrencyRates = LoadCurrencyRates(
            DefaultCurrencyRatesRepository(rates("1.1")),
            InMemoryCurrencyRatesRepository(rates("2"))
        )

        assertEquals(BigDecimal("2"), loadCurrencyRates.load().rates[CurrencyCode.USD])
    }

    @Test
    fun `falls back to default rates`() {
        val loadCurrencyRates = LoadCurrencyRates(
            DefaultCurrencyRatesRepository(rates("1.1")),
            InMemoryCurrencyRatesRepository()
        )

        assertEquals(BigDecimal("1.1"), loadCurrencyRates.load().rates[CurrencyCode.USD])
    }

    private class DefaultCurrencyRatesRepository(private val rates: CurrencyRates) :
        ReadOnlyCurrencyRatesRepository {
        override fun load() = rates
    }

    private class InMemoryCurrencyRatesRepository(private val rates: CurrencyRates? = null) :
        CurrencyRatesRepository {
        override fun load() = rates

        override fun save(rates: CurrencyRates) = Unit
    }

    private fun rates(rate: String) = CurrencyRates.create(
        provider = CurrencyRateProvider.EuropeanCentralBank,
        sourceUrl = URI("https://example.test/rates"),
        downloadedAt = Instant.parse("2026-08-25T12:00:00Z"),
        rateDate = LocalDate.parse("2026-08-25"),
        base = CurrencyCode.EUR,
        rates = mapOf(CurrencyCode.EUR to BigDecimal.ONE, CurrencyCode.USD to BigDecimal(rate))
    )
}
