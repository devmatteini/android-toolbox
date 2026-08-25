package com.cosimomatteini.toolbox.build

import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_BASE
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_FILE_NAME
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_PROVIDER_ID
import com.cosimomatteini.toolbox.currencyrates.FrankfurterCurrencyRates
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.LocalDate
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateCurrencyRatesTask : DefaultTask() {
    @get:Input
    abstract val sourceUrl: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:LocalState
    abstract val temporaryDirectory: DirectoryProperty

    init {
        sourceUrl.convention(FRANKFURTER_URL)
        outputs.cacheIf { false }
        outputs.upToDateWhen { System.getenv("CI").isNullOrBlank() }
    }

    @TaskAction
    fun generate() {
        val output = outputDirectory.file(CURRENCY_RATES_FILE_NAME).get().asFile.toPath()
        Files.deleteIfExists(output)

        val response = fetch(sourceUrl.get())
        val rates = CurrencyRates.parse(response)
        val generated = CurrencyRates.toJson(rates, sourceUrl.get(), Instant.now())
        val temporary = temporaryDirectory.file(CURRENCY_RATES_FILE_NAME).get().asFile.toPath()

        Files.createDirectories(temporary.parent)
        Files.write(temporary, generated.toByteArray(StandardCharsets.UTF_8))
        Files.createDirectories(output.parent)
        try {
            Files.move(
                temporary,
                output,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun fetch(url: String): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")

            check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "Could not fetch currency rates: HTTP ${connection.responseCode}"
            }
            connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val FRANKFURTER_URL =
            "https://api.frankfurter.dev/v2/rates?base=$CURRENCY_RATES_BASE&providers=$CURRENCY_RATES_PROVIDER_ID"
    }
}

data class CurrencyRates(val rateDate: LocalDate, val rates: Map<String, BigDecimal>) {
    companion object {
        fun parse(response: String): CurrencyRates = FrankfurterCurrencyRates.parse(response).let {
            CurrencyRates(it.rateDate, it.rates)
        }

        fun toJson(rates: CurrencyRates, sourceUrl: String, downloadedAt: Instant): String =
            FrankfurterCurrencyRates(rates.rateDate, rates.rates).toJson(sourceUrl, downloadedAt)
    }
}
