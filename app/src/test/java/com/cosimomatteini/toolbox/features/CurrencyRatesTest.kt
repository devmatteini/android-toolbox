package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.currencyrates.CurrencyRateProvider
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile
import com.cosimomatteini.toolbox.domain.CurrencyExchangeRates
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import com.cosimomatteini.toolbox.domain.ReadOnlyCurrencyRatesRepository
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyRatesTest {
    @Test
    fun `loads currency rates`() {
        val currencyRates =
            currencyRates(currencyRates = InMemoryCurrencyRatesRepository(rates = rates("2")))

        assertEquals(BigDecimal("2"), currencyRates.load().rates[CurrencyCode.USD])
    }

    @Test
    fun `no currency rates, fallback to default rates`() {
        val currencyRates = currencyRates(currencyRates = InMemoryCurrencyRatesRepository())

        assertEquals(BigDecimal("1.1"), currencyRates.load().rates[CurrencyCode.USD])
    }

    @Test
    fun `refresh rates`() = runTest {
        val repo = InMemoryCurrencyRatesRepository()
        val currencyRates =
            currencyRates(
                currencyRates = repo,
                currencyExchangeRates = InMemoryExchangeRates(rates("1.2"))
            )

        val result = currencyRates.refresh()

        assertEquals(RefreshCurrencyResult.Updated(rates("1.2")), result)
        assertEquals(rates("1.2"), repo.rates)
    }

    @Test
    fun `failed refresh`() = runTest {
        val persisted = rates("1.3")
        val repo = InMemoryCurrencyRatesRepository(rates = persisted)
        val currencyRates =
            currencyRates(
                currencyRates = repo,
                currencyExchangeRates = InMemoryExchangeRates(error = IllegalStateException())
            )

        assertEquals(RefreshCurrencyResult.Failed, currencyRates.refresh())
        assertEquals(persisted, currencyRates.load())
    }

    private fun currencyRates(
        currencyRates: InMemoryCurrencyRatesRepository = InMemoryCurrencyRatesRepository(),
        currencyExchangeRates: InMemoryExchangeRates = InMemoryExchangeRates(rates("1.2"))
    ): CurrencyRates = CurrencyRates(
        defaultCurrencyRates = DefaultCurrencyRatesRepository(
            rates("1.1", downloadedAt = "2026-08-25T12:00:00Z")
        ),
        currencyRates = currencyRates,
        currencyExchangeRates = currencyExchangeRates
    )

    private fun rates(
        rate: String,
        downloadedAt: String = "2026-08-25T12:00:00Z"
    ): CurrencyRatesFile = CurrencyRatesFile.create(
        provider = CurrencyRateProvider.EuropeanCentralBank,
        sourceUrl = URI("https://example.test/rates"),
        downloadedAt = Instant.parse(downloadedAt),
        rateDate = LocalDate.parse("2026-08-25"),
        base = CurrencyCode.EUR,
        rates = mapOf(
            CurrencyCode.EUR to BigDecimal.ONE,
            CurrencyCode.USD to BigDecimal(rate)
        )
    )

    private class DefaultCurrencyRatesRepository(private val rates: CurrencyRatesFile) :
        ReadOnlyCurrencyRatesRepository {
        override fun load() = rates
    }

    private class InMemoryCurrencyRatesRepository(var rates: CurrencyRatesFile? = null) :
        CurrencyRatesRepository {
        override fun load() = rates

        override fun save(rates: CurrencyRatesFile) {
            this.rates = rates
        }
    }

    private class InMemoryExchangeRates(
        private val rates: CurrencyRatesFile? = null,
        private val error: Exception? = null
    ) : CurrencyExchangeRates {
        var calls = 0

        override suspend fun load(): CurrencyRatesFile {
            calls++
            error?.let { throw it }
            return checkNotNull(rates)
        }
    }
}
