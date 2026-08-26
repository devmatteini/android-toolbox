package com.cosimomatteini.toolbox.currencyrates

import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

data class FrankfurterCurrencyRates(
    val rateDate: LocalDate,
    val rates: Map<CurrencyCode, BigDecimal>
) {
    fun toCurrencyRates(sourceUrl: URI, downloadedAt: Instant): CurrencyRates =
        CurrencyRates.create(
            provider = CurrencyRateProvider(
                CURRENCY_RATES_PROVIDER_ID,
                CURRENCY_RATES_PROVIDER_NAME
            ),
            sourceUrl = sourceUrl,
            downloadedAt = downloadedAt,
            rateDate = rateDate,
            base = CurrencyCode.EUR,
            rates = rates
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
                require(record.base == CurrencyCode.EUR.value) {
                    "Currency rate base must be ${CurrencyCode.EUR.value}"
                }
                val quote = CurrencyCode.parse(record.quote)
                val rate = BigDecimal(record.rate.content)
                require(rate > BigDecimal.ZERO) {
                    "Currency rate for ${record.quote} must be positive"
                }
                quote to rate
            }
            require(rates[CurrencyCode.EUR]?.compareTo(BigDecimal.ONE) == 0) {
                "Currency-rates ${CurrencyCode.EUR.value} rate must be exactly 1"
            }
            return FrankfurterCurrencyRates(
                LocalDate.parse(dates.single()),
                rates.toSortedMap(compareBy(CurrencyCode::value))
            )
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
