package com.cosimomatteini.toolbox.currencyrates

import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.util.Collections
import java.util.Currency
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val CURRENCY_RATES_PROVIDER_ID = "ECB"
const val CURRENCY_RATES_PROVIDER_NAME = "European Central Bank"
const val CURRENCY_RATES_FILE_NAME = "currency-rates.json"

@JvmInline
value class CurrencyCode private constructor(val value: String) {
    companion object {
        val EUR = CurrencyCode("EUR")
        val USD = CurrencyCode("USD")

        fun parse(value: String): CurrencyCode = try {
            CurrencyCode(Currency.getInstance(value).currencyCode)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid ISO 4217 currency code: $value")
        }
    }
}

enum class CurrencyRateProvider(val id: String, val displayName: String) {
    EuropeanCentralBank(CURRENCY_RATES_PROVIDER_ID, CURRENCY_RATES_PROVIDER_NAME)
}

@ConsistentCopyVisibility
data class CurrencyRatesFile private constructor(
    val provider: CurrencyRateProvider,
    val sourceUrl: URI,
    val downloadedAt: Instant,
    val rateDate: LocalDate,
    val base: CurrencyCode,
    val rates: Map<CurrencyCode, BigDecimal>
) {
    companion object {
        fun create(
            provider: CurrencyRateProvider,
            sourceUrl: URI,
            downloadedAt: Instant,
            rateDate: LocalDate,
            base: CurrencyCode,
            rates: Map<CurrencyCode, BigDecimal>
        ): CurrencyRatesFile {
            require(provider == CurrencyRateProvider.EuropeanCentralBank) {
                "Currency-rates provider must be $CURRENCY_RATES_PROVIDER_ID"
            }
            require(base == CurrencyCode.EUR) {
                "Currency-rates base must be ${CurrencyCode.EUR.value}"
            }
            require(rates.isNotEmpty()) { "Currency-rates must contain rates" }
            require(rates[base]?.compareTo(BigDecimal.ONE) == 0) {
                "Currency-rates ${base.value} rate must be exactly 1"
            }
            require(sourceUrl.isAbsolute) { "Currency-rates source URL must be absolute" }
            rates.forEach { (code, rate) ->
                require(rate > BigDecimal.ZERO) {
                    "Currency rate for ${code.value} must be positive"
                }
            }
            return CurrencyRatesFile(
                provider = provider,
                sourceUrl = sourceUrl,
                downloadedAt = downloadedAt,
                rateDate = rateDate,
                base = base,
                rates = Collections.unmodifiableMap(
                    rates.toSortedMap(compareBy(CurrencyCode::value))
                )
            )
        }
    }
}

object CurrencyRatesFileCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun decode(value: String): CurrencyRatesFile =
        json.decodeFromString<CurrencyRatesFileDto>(value).toFile()

    fun encode(file: CurrencyRatesFile): String = json.encodeToString(file.toDto()) + "\n"

    private fun CurrencyRatesFileDto.toFile(): CurrencyRatesFile {
        require(schemaVersion == SCHEMA_VERSION) {
            "Unsupported currency-rates schema version: $schemaVersion"
        }
        require(provider.id == CURRENCY_RATES_PROVIDER_ID) {
            "Currency-rates provider must be $CURRENCY_RATES_PROVIDER_ID"
        }
        require(provider.name == CURRENCY_RATES_PROVIDER_NAME) {
            "Currency-rates provider name is invalid"
        }
        return CurrencyRatesFile.create(
            provider = CurrencyRateProvider.EuropeanCentralBank,
            sourceUrl = URI(sourceUrl),
            downloadedAt = Instant.parse(downloadedAt),
            rateDate = LocalDate.parse(rateDate),
            base = CurrencyCode.parse(base),
            rates = rates.map { (code, rate) ->
                require(DECIMAL.matches(rate)) {
                    "Currency rate for $code must be a positive decimal string"
                }
                CurrencyCode.parse(code) to BigDecimal(rate)
            }.toMap()
        )
    }

    private fun CurrencyRatesFile.toDto(): CurrencyRatesFileDto = CurrencyRatesFileDto(
        schemaVersion = SCHEMA_VERSION,
        provider = CurrencyRateProviderDto(provider.id, provider.displayName),
        sourceUrl = sourceUrl.toString(),
        downloadedAt = downloadedAt.toString(),
        rateDate = rateDate.toString(),
        base = base.value,
        rates = rates.mapKeys { it.key.value }.mapValues { (_, rate) ->
            rate.stripTrailingZeros().toPlainString()
        }
    )

    private val DECIMAL = Regex("(?:0|[1-9]\\d*)(?:\\.\\d+)?")

    private const val SCHEMA_VERSION = 1
}

@Serializable
private data class CurrencyRatesFileDto(
    val schemaVersion: Int,
    val provider: CurrencyRateProviderDto,
    val sourceUrl: String,
    val downloadedAt: String,
    val rateDate: String,
    val base: String,
    val rates: Map<String, String>
)

@Serializable
private data class CurrencyRateProviderDto(val id: String, val name: String)
