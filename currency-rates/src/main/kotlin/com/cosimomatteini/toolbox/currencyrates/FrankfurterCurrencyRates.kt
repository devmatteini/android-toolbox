package com.cosimomatteini.toolbox.currencyrates

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.Currency
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

data class FrankfurterCurrencyRates(val rateDate: LocalDate, val rates: Map<String, BigDecimal>) {
    fun toJson(sourceUrl: String, downloadedAt: Instant): String = CurrencyRatesFileCodec.encode(
        CurrencyRatesFile(
            schemaVersion = CURRENCY_RATES_SCHEMA_VERSION,
            provider = CurrencyRateProvider(
                CURRENCY_RATES_PROVIDER_ID,
                CURRENCY_RATES_PROVIDER_NAME
            ),
            sourceUrl = sourceUrl,
            downloadedAt = downloadedAt.toString(),
            rateDate = rateDate.toString(),
            base = CURRENCY_RATES_BASE,
            rates = rates.mapValues { (_, rate) -> rate.stripTrailingZeros().toPlainString() }
        )
    )

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun parse(response: String): FrankfurterCurrencyRates {
            val records = json.decodeFromString<List<FrankfurterRateRecord>>(response)
            require(records.isNotEmpty()) { "Currency-rates response must contain rates" }

            val dates = records.map(FrankfurterRateRecord::date).distinct()
            require(dates.size == 1) { "Currency-rates response must contain one rate date" }
            require(records.map(FrankfurterRateRecord::quote).distinct().size == records.size) {
                "Currency-rates response must not contain duplicate currencies"
            }

            val rates = records.associate { record ->
                require(record.base == CURRENCY_RATES_BASE) {
                    "Currency rate base must be $CURRENCY_RATES_BASE"
                }
                requireCurrencyCode(record.quote)
                val rate = BigDecimal(record.rate.content)
                require(rate > BigDecimal.ZERO) {
                    "Currency rate for ${record.quote} must be positive"
                }
                record.quote to rate
            }
            require(rates[CURRENCY_RATES_BASE]?.compareTo(BigDecimal.ONE) == 0) {
                "Currency-rates $CURRENCY_RATES_BASE rate must be exactly 1"
            }
            return FrankfurterCurrencyRates(LocalDate.parse(dates.single()), rates.toSortedMap())
        }
    }
}

@Serializable
private data class FrankfurterRateRecord(
    val date: String,
    val base: String,
    val quote: String,
    val rate: JsonPrimitive
)

private fun requireCurrencyCode(code: String) {
    try {
        Currency.getInstance(code)
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid ISO 4217 currency code: $code")
    }
}
