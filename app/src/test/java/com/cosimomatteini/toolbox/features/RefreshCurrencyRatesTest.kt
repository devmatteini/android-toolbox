package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.currencyrates.CurrencyRateProvider
import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.domain.CurrencyExchangeRates
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshCurrencyRatesTest {
    @Test
    fun `refreshes and persists rates`() = runTest {
        val repository = InMemoryCurrencyRatesRepository()
        val refreshCurrencyRates = RefreshCurrencyRates(
            repository,
            InMemoryExchangeRates(rates("1.2"))
        )

        val result = refreshCurrencyRates.refresh()

        assertEquals(RefreshCurrencyResult.Updated(rates("1.2")), result)
        assertEquals(rates("1.2"), repository.rates)
    }

    @Test
    fun `keeps persisted rates when refresh fails`() = runTest {
        val persisted = rates("1.3")
        val repository = InMemoryCurrencyRatesRepository(persisted)
        val refreshCurrencyRates = RefreshCurrencyRates(
            repository,
            InMemoryExchangeRates(error = IllegalStateException())
        )

        assertEquals(RefreshCurrencyResult.Failed, refreshCurrencyRates.refresh())
        assertEquals(persisted, repository.rates)
    }

    private class InMemoryCurrencyRatesRepository(var rates: CurrencyRates? = null) :
        CurrencyRatesRepository {
        override fun load() = rates

        override fun save(rates: CurrencyRates) {
            this.rates = rates
        }
    }

    private class InMemoryExchangeRates(
        private val rates: CurrencyRates? = null,
        private val error: Exception? = null
    ) : CurrencyExchangeRates {
        override suspend fun load(): CurrencyRates {
            error?.let { throw it }
            return checkNotNull(rates)
        }
    }

    private fun rates(rate: String) = CurrencyRates.create(
        provider = CurrencyRateProvider("ECB", "European Central Bank"),
        sourceUrl = URI("https://example.test/rates"),
        downloadedAt = Instant.parse("2026-08-25T12:00:00Z"),
        rateDate = LocalDate.parse("2026-08-25"),
        base = CurrencyCode.EUR,
        rates = mapOf(CurrencyCode.EUR to BigDecimal.ONE, CurrencyCode.USD to BigDecimal(rate))
    )
}
