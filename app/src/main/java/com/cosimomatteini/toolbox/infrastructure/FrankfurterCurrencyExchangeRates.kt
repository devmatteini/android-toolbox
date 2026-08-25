package com.cosimomatteini.toolbox.infrastructure

import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_BASE
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_PROVIDER_ID
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFileCodec
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
    private val sourceUrl: String = FRANKFURTER_URL
) : CurrencyExchangeRates {
    override suspend fun load(): CurrencyRatesFile = withContext(Dispatchers.IO) {
        val connection = URI(sourceUrl).toURL().openConnection() as HttpURLConnection
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
            CurrencyRatesFileCodec.decode(
                FrankfurterCurrencyRates.parse(response).toJson(sourceUrl, clock.instant())
            )
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
        const val FRANKFURTER_URL =
            "https://api.frankfurter.dev/v2/rates?base=$CURRENCY_RATES_BASE&providers=$CURRENCY_RATES_PROVIDER_ID"
    }
}
