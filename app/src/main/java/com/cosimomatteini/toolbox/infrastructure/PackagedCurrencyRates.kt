package com.cosimomatteini.toolbox.infrastructure

import android.content.res.AssetManager
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_FILE_NAME
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFile
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesFileCodec
import com.cosimomatteini.toolbox.domain.ReadOnlyCurrencyRatesRepository

class PackagedCurrencyRates(private val assets: AssetManager) : ReadOnlyCurrencyRatesRepository {
    override fun load(): CurrencyRatesFile =
        assets.open(CURRENCY_RATES_FILE_NAME).bufferedReader().use { reader ->
            CurrencyRatesFileCodec.decode(reader.readText())
        }
}
