package com.cosimomatteini.toolbox.currencyrates

import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.util.Currency
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val CURRENCY_RATES_SCHEMA_VERSION = 1
const val CURRENCY_RATES_PROVIDER_ID = "ECB"
const val CURRENCY_RATES_PROVIDER_NAME = "European Central Bank"
const val CURRENCY_RATES_BASE = "EUR"
const val CURRENCY_RATES_FILE_NAME = "currency-rates.json"

@Serializable
data class CurrencyRatesFile(
    val schemaVersion: Int,
    val provider: CurrencyRateProvider,
    val sourceUrl: String,
    val downloadedAt: String,
    val rateDate: String,
    val base: String,
    val rates: Map<String, String>
)

@Serializable
data class CurrencyRateProvider(val id: String, val name: String)

object CurrencyRatesFileCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun decode(value: String): CurrencyRatesFile =
        validate(json.decodeFromString<CurrencyRatesFile>(value))

    fun encode(file: CurrencyRatesFile): String = json.encodeToString(validate(file)) + "\n"

    fun validate(file: CurrencyRatesFile): CurrencyRatesFile {
        require(file.schemaVersion == CURRENCY_RATES_SCHEMA_VERSION) {
            "Unsupported currency-rates schema version: ${file.schemaVersion}"
        }
        require(file.provider.id == CURRENCY_RATES_PROVIDER_ID) {
            "Currency-rates provider must be $CURRENCY_RATES_PROVIDER_ID"
        }
        require(file.provider.name == CURRENCY_RATES_PROVIDER_NAME) {
            "Currency-rates provider name is invalid"
        }
        require(file.base == CURRENCY_RATES_BASE) {
            "Currency-rates base must be $CURRENCY_RATES_BASE"
        }
        require(file.rates.isNotEmpty()) { "Currency-rates must contain rates" }
        require(file.rates[CURRENCY_RATES_BASE] == "1") {
            "Currency-rates $CURRENCY_RATES_BASE rate must be exactly 1"
        }
        require(URI(file.sourceUrl).isAbsolute) { "Currency-rates source URL must be absolute" }
        Instant.parse(file.downloadedAt)
        LocalDate.parse(file.rateDate)
        file.rates.forEach { (code, rate) ->
            requireCurrencyCode(code)
            require(DECIMAL.matches(rate) && BigDecimal(rate) > BigDecimal.ZERO) {
                "Currency rate for $code must be a positive decimal string"
            }
        }
        return file
    }

    private fun requireCurrencyCode(code: String) {
        try {
            Currency.getInstance(code)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid ISO 4217 currency code: $code")
        }
    }

    private val DECIMAL = Regex("(?:0|[1-9]\\d*)(?:\\.\\d+)?")
}
