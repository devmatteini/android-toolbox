package com.cosimomatteini.toolbox.ui

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.currencyrates.CurrencyRateProvider
import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.domain.CurrencyExchangeRates
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import com.cosimomatteini.toolbox.domain.ReadOnlyCurrencyRatesRepository
import com.cosimomatteini.toolbox.features.LoadCurrencyRates
import com.cosimomatteini.toolbox.features.RefreshCurrencyRates
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyRatesViewModelTest {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `loads rates after exposing the initial loading state`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val rates = rates()
            val viewModel = CurrencyRatesViewModel(
                loadCurrencyRates = LoadCurrencyRates(
                    DefaultRatesRepository(rates),
                    RatesRepository()
                ),
                refreshCurrencyRates = RefreshCurrencyRates(
                    RatesRepository(),
                    ExchangeRates(rates)
                ),
                dispatcher = dispatcher
            )

            assertTrue(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.rates)

            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(rates, viewModel.uiState.value.rates)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class DefaultRatesRepository(private val rates: CurrencyRates) :
        ReadOnlyCurrencyRatesRepository {
        override fun load() = rates
    }

    private class RatesRepository : CurrencyRatesRepository {
        override fun load(): CurrencyRates? = null

        override fun save(rates: CurrencyRates) = Unit
    }

    private class ExchangeRates(private val rates: CurrencyRates) : CurrencyExchangeRates {
        override suspend fun load() = rates
    }

    private fun rates() = CurrencyRates.create(
        provider = CurrencyRateProvider("ECB", "European Central Bank"),
        sourceUrl = URI("https://example.test/rates"),
        downloadedAt = Instant.parse("2026-08-25T12:00:00Z"),
        rateDate = LocalDate.parse("2026-08-25"),
        base = CurrencyCode.EUR,
        rates = mapOf(CurrencyCode.EUR to BigDecimal.ONE, CurrencyCode.USD to BigDecimal("1.1"))
    )
}
