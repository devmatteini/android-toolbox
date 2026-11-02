package com.cosimomatteini.toolbox.ui

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.currencyrates.CurrencyRateProvider
import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.domain.ConverterCategory
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.domain.ReadOnlyCurrencyRatesRepository
import com.cosimomatteini.toolbox.features.LoadCurrencyRates
import com.cosimomatteini.toolbox.infrastructure.ConverterDefaults
import com.cosimomatteini.toolbox.infrastructure.ConverterDefaultsStore
import com.cosimomatteini.toolbox.infrastructure.DefaultSide
import com.cosimomatteini.toolbox.infrastructure.DefaultUnitIds
import com.cosimomatteini.toolbox.infrastructure.initialConverterDefaults
import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `loads settings and persists a selection`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val store = FakeStore()
            val viewModel = SettingsViewModel(store, loadRates(rates()), dispatcher)
            assertEquals(SettingsUiState.Loading, viewModel.uiState.value)

            advanceUntilIdle()
            val defaults = (viewModel.uiState.value as SettingsUiState.Content).defaults
            assertEquals(LengthUnit.Meter, defaults.length.source)
            assertEquals(LengthUnit.Kilometer, defaults.length.target)

            viewModel.select(ConverterCategory.Length, DefaultSide.Source, LengthUnit.Foot.name)
            advanceUntilIdle()

            assertEquals(
                Triple(ConverterCategory.Length, DefaultSide.Source, LengthUnit.Foot.name),
                store.updates.last()
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `reports persistence failure`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val store = FakeStore(failUpdates = true)
            val viewModel = SettingsViewModel(store, loadRates(rates()), dispatcher)
            advanceUntilIdle()

            viewModel.select(ConverterCategory.Length, DefaultSide.Source, LengthUnit.Foot.name)
            advanceUntilIdle()
            assertTrue((viewModel.uiState.value as SettingsUiState.Content).persistenceFailed)

            viewModel.onPersistenceErrorShown()
            assertFalse((viewModel.uiState.value as SettingsUiState.Content).persistenceFailed)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `repairs unavailable currencies to EUR`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val invalid = initialConverterDefaults.values.toMutableMap().apply {
                this[ConverterCategory.Currency] = DefaultUnitIds("GBP", "USD")
            }
            val store = FakeStore(ConverterDefaults(invalid))
            val viewModel =
                SettingsViewModel(store, loadRates(rates(includeUsd = false)), dispatcher)
            advanceUntilIdle()

            val pair = (viewModel.uiState.value as SettingsUiState.Content).defaults.currency
            assertEquals(CurrencyCode.EUR, pair.source.code)
            assertEquals(CurrencyCode.EUR, pair.target.code)
            assertTrue(store.updates.any { it.second == DefaultSide.Source && it.third == "EUR" })
            assertTrue(store.updates.any { it.second == DefaultSide.Target && it.third == "EUR" })
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `falls back from invalid non-currency identifiers`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val invalid = initialConverterDefaults.values.toMutableMap().apply {
                this[ConverterCategory.Length] = DefaultUnitIds("removed", "unknown")
            }
            val viewModel = SettingsViewModel(
                FakeStore(ConverterDefaults(invalid)),
                loadRates(rates()),
                dispatcher
            )
            advanceUntilIdle()

            val length = (viewModel.uiState.value as SettingsUiState.Content).defaults.length
            assertEquals(UnitPair(LengthUnit.Meter, LengthUnit.Kilometer), length)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `reloads currency options when settings opens`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val runtimeRates = MutableRatesRepository()
            val viewModel = SettingsViewModel(
                FakeStore(),
                LoadCurrencyRates(DefaultRatesRepository(rates()), runtimeRates),
                dispatcher
            )
            advanceUntilIdle()

            val gbp = CurrencyCode.parse("GBP")
            runtimeRates.rates = rates(extraRates = mapOf(gbp to BigDecimal("0.86")))
            viewModel.onSettingsOpened()
            advanceUntilIdle()

            val currencies =
                (viewModel.uiState.value as SettingsUiState.Content).defaults.currencyUnits
            assertTrue(currencies.any { it.code == gbp })
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeStore(
        defaults: ConverterDefaults = initialConverterDefaults,
        private val failUpdates: Boolean = false
    ) : ConverterDefaultsStore {
        override val defaults = MutableStateFlow(defaults)
        val updates = mutableListOf<Triple<ConverterCategory, DefaultSide, String>>()

        override suspend fun update(
            category: ConverterCategory,
            side: DefaultSide,
            unitId: String
        ) {
            if (failUpdates) error("disk full")
            updates += Triple(category, side, unitId)
        }
    }

    private class DefaultRatesRepository(private val rates: CurrencyRates) :
        ReadOnlyCurrencyRatesRepository {
        override fun load() = rates
    }

    private class MutableRatesRepository(var rates: CurrencyRates? = null) :
        CurrencyRatesRepository {
        override fun load() = rates
        override fun save(rates: CurrencyRates) {
            this.rates = rates
        }
    }

    private fun loadRates(rates: CurrencyRates) = LoadCurrencyRates(
        object : ReadOnlyCurrencyRatesRepository {
            override fun load() = rates
        },
        object : CurrencyRatesRepository {
            override fun load(): CurrencyRates? = null
            override fun save(rates: CurrencyRates) = Unit
        }
    )

    private fun rates(
        includeUsd: Boolean = true,
        extraRates: Map<CurrencyCode, BigDecimal> = emptyMap()
    ) = CurrencyRates.create(
        provider = CurrencyRateProvider("ECB", "European Central Bank"),
        sourceUrl = URI("https://example.test/rates"),
        downloadedAt = Instant.parse("2026-08-25T12:00:00Z"),
        rateDate = LocalDate.parse("2026-08-25"),
        base = CurrencyCode.EUR,
        rates = buildMap {
            put(CurrencyCode.EUR, BigDecimal.ONE)
            if (includeUsd) put(CurrencyCode.USD, BigDecimal("1.1"))
            putAll(extraRates)
        }
    )
}
