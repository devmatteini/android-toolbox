package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_BASE
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_PROVIDER_ID
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_PROVIDER_NAME
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_SCHEMA_VERSION
import com.cosimomatteini.toolbox.currencyrates.CurrencyRateProvider
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile
import com.cosimomatteini.toolbox.domain.CurrencyExchangeRates
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import com.cosimomatteini.toolbox.domain.ReadOnlyCurrencyRatesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyRatesTest {
    @Test
    fun `loads currency rates`() {
        val currencyRates =
            currencyRates(currencyRates = InMemoryCurrencyRatesRepository(rates = rates("2")))

        assertEquals("2", currencyRates.load().rates["USD"])
    }

    @Test
    fun `no currency rates, fallback to default rates`() {
        val currencyRates = currencyRates(currencyRates = InMemoryCurrencyRatesRepository())

        assertEquals("1.1", currencyRates.load().rates["USD"])
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
    ): CurrencyRatesFile = CurrencyRatesFile(
        schemaVersion = CURRENCY_RATES_SCHEMA_VERSION,
        provider = CurrencyRateProvider(
            CURRENCY_RATES_PROVIDER_ID,
            CURRENCY_RATES_PROVIDER_NAME
        ),
        sourceUrl = "https://example.test/rates",
        downloadedAt = downloadedAt,
        rateDate = "2026-08-25",
        base = CURRENCY_RATES_BASE,
        rates = mapOf("EUR" to "1", "USD" to rate)
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
