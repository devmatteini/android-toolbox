package com.cosimomatteini.toolbox.tasks

import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_FILE_NAME
import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesCodec
import com.cosimomatteini.toolbox.currencyrates.FrankfurterCurrencyRates
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
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

        val rates = FrankfurterCurrencyRates.parse(fetch(sourceUrl.get()))
        val generated = CurrencyRatesCodec.encode(
            rates.toCurrencyRates(URI(sourceUrl.get()), Instant.now())
        )
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
        val FRANKFURTER_URL =
            "https://api.frankfurter.dev/v2/rates?base=${CurrencyCode.EUR.value}&providers=ECB"
    }
}
