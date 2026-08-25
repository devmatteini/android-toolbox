package com.cosimomatteini.toolbox.infrastructure

import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_BASE
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_PROVIDER_ID
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_PROVIDER_NAME
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_SCHEMA_VERSION
import com.cosimomatteini.toolbox.currencyrates.CurrencyRateProvider
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FileCurrencyRatesRepositoryTest {
    @Test
    fun `invalid runtime JSON falls back to packaged data`() = withDirectory { directory ->
        File(directory, "currency-rates.json").writeText("not JSON")

        assertNull(FileCurrencyRatesRepository(directory).load())
    }

    @Test
    fun `failed atomic update retains the existing rates`() = withDirectory { directory ->
        val original = rates("1.1")
        FileCurrencyRatesRepository(directory).save(original)

        val failingStore = FileCurrencyRatesRepository(directory) { _, _ ->
            throw IllegalStateException("disk full")
        }

        runCatching { failingStore.save(rates("1.2")) }

        assertEquals(original, failingStore.load())
    }

    private fun rates(rate: String) = CurrencyRatesFile(
        schemaVersion = CURRENCY_RATES_SCHEMA_VERSION,
        provider = CurrencyRateProvider(CURRENCY_RATES_PROVIDER_ID, CURRENCY_RATES_PROVIDER_NAME),
        sourceUrl = "https://example.test/rates",
        downloadedAt = "2026-08-25T12:00:00Z",
        rateDate = "2026-08-25",
        base = CURRENCY_RATES_BASE,
        rates = mapOf("EUR" to "1", "USD" to rate)
    )

    private fun withDirectory(block: (File) -> Unit) {
        val directory = Files.createTempDirectory("currency-rates-test").toFile()
        try {
            block(directory)
        } finally {
            directory.deleteRecursively()
        }
    }
}
