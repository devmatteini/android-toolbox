package com.cosimomatteini.toolbox.infrastructure

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.currencyrates.CurrencyRateProvider
import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import java.io.File
import java.math.BigDecimal
import java.net.URI
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDate
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

    private fun rates(rate: String) = CurrencyRates.create(
        provider = CurrencyRateProvider("ECB", "European Central Bank"),
        sourceUrl = URI("https://example.test/rates"),
        downloadedAt = Instant.parse("2026-08-25T12:00:00Z"),
        rateDate = LocalDate.parse("2026-08-25"),
        base = CurrencyCode.EUR,
        rates = mapOf(
            CurrencyCode.EUR to BigDecimal.ONE,
            CurrencyCode.USD to BigDecimal(rate)
        )
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
