package com.cosimomatteini.toolbox.infrastructure

import android.content.res.AssetManager
import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_FILE_NAME
import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesCodec
import com.cosimomatteini.toolbox.domain.ReadOnlyCurrencyRatesRepository

class PackagedCurrencyRates(private val assets: AssetManager) : ReadOnlyCurrencyRatesRepository {
    override fun load(): CurrencyRates =
        assets.open(CURRENCY_RATES_FILE_NAME).bufferedReader().use { reader ->
            CurrencyRatesCodec.decode(reader.readText())
        }
}
