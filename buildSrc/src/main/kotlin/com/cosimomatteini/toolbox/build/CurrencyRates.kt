package com.cosimomatteini.toolbox.build

import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_BASE
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_PROVIDER_ID
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_PROVIDER_NAME
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_SCHEMA_VERSION
import com.cosimomatteini.toolbox.currencyrates.CurrencyRateProvider
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFileCodec
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.LocalDate
import java.util.Currency
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

private const val CURRENCY_RATES_FILE_NAME = "currency-rates.json"

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
        fun parse(response: String): CurrencyRates {
            val records = json.decodeFromString<List<FrankfurterRateRecord>>(response)
            require(records.isNotEmpty()) { "Currency-rates response must contain rates" }

            val dates = records.map(FrankfurterRateRecord::date).distinct()
            require(dates.size == 1) { "Currency-rates response must contain one rate date" }
            require(records.map(FrankfurterRateRecord::quote).distinct().size == records.size) {
                "Currency-rates response must not contain duplicate currencies"
            }

            val rates = records.map(FrankfurterRateRecord::toFrankfurterRate)
                .associate { it.targetCurrency to it.conversionRate }
            require(rates[CURRENCY_RATES_BASE]?.compareTo(BigDecimal.ONE) == 0) {
                "Currency-rates $CURRENCY_RATES_BASE rate must be exactly 1"
            }
            return CurrencyRates(LocalDate.parse(dates.single()), rates.toSortedMap())
        }

        fun toJson(rates: CurrencyRates, sourceUrl: String, downloadedAt: Instant): String =
            CurrencyRatesFileCodec.encode(
                CurrencyRatesFile(
                    schemaVersion = CURRENCY_RATES_SCHEMA_VERSION,
                    provider = CurrencyRateProvider(
                        CURRENCY_RATES_PROVIDER_ID,
                        CURRENCY_RATES_PROVIDER_NAME
                    ),
                    sourceUrl = sourceUrl,
                    downloadedAt = downloadedAt.toString(),
                    rateDate = rates.rateDate.toString(),
                    base = CURRENCY_RATES_BASE,
                    rates = rates.rates.mapValues { (_, rate) ->
                        rate.stripTrailingZeros().toPlainString()
                    }
                )
            )

        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }
}

@kotlinx.serialization.Serializable
private data class FrankfurterRateRecord(
    val date: String,
    val base: String,
    val quote: String,
    val rate: JsonPrimitive
) {
    fun toFrankfurterRate(): FrankfurterRate {
        require(base == CURRENCY_RATES_BASE) {
            "Currency rate base must be $CURRENCY_RATES_BASE"
        }
        return FrankfurterRate(quote, BigDecimal(rate.content))
    }
}

private data class FrankfurterRate(val targetCurrency: String, val conversionRate: BigDecimal) {
    init {
        requireCurrencyCode(targetCurrency)
        require(conversionRate > BigDecimal.ZERO) {
            "Currency rate for $targetCurrency must be positive"
        }
    }
}

private fun requireCurrencyCode(code: String) {
    try {
        Currency.getInstance(code)
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid ISO 4217 currency code: $code")
    }
}
