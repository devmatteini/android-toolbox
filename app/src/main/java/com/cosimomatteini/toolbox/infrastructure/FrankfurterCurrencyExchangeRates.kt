package com.cosimomatteini.toolbox.infrastructure

import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_PROVIDER_ID
import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile
import com.cosimomatteini.toolbox.currencyrates.FrankfurterCurrencyRates
import com.cosimomatteini.toolbox.domain.CurrencyExchangeRates
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FrankfurterCurrencyExchangeRates(
    private val clock: Clock = Clock.systemUTC(),
    private val sourceUrl: URI = URI(FRANKFURTER_URL)
) : CurrencyExchangeRates {
    override suspend fun load(): CurrencyRatesFile = withContext(Dispatchers.IO) {
        val connection = sourceUrl.toURL().openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "Could not fetch currency rates: HTTP ${connection.responseCode}"
            }
            val response = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use {
                it.readText()
            }
            FrankfurterCurrencyRates.parse(response).toFile(sourceUrl, clock.instant())
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
        val FRANKFURTER_URL =
            "https://api.frankfurter.dev/v2/rates?base=${CurrencyCode.EUR.value}&providers=$CURRENCY_RATES_PROVIDER_ID"
    }
}
