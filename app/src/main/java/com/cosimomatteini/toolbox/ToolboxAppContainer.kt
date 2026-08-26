package com.cosimomatteini.toolbox

import android.content.Context
import com.cosimomatteini.toolbox.features.ConvertArea
import com.cosimomatteini.toolbox.features.ConvertLength
import com.cosimomatteini.toolbox.features.ConvertMass
import com.cosimomatteini.toolbox.features.ConvertSpeed
import com.cosimomatteini.toolbox.features.ConvertTemperature
import com.cosimomatteini.toolbox.features.ConvertVolume
import com.cosimomatteini.toolbox.features.LoadCurrencyRates
import com.cosimomatteini.toolbox.features.ObserveCompass
import com.cosimomatteini.toolbox.features.RefreshCurrencyRates
import com.cosimomatteini.toolbox.features.Tools
import com.cosimomatteini.toolbox.infrastructure.AndroidCompassSensor
import com.cosimomatteini.toolbox.infrastructure.FileCurrencyRatesRepository
import com.cosimomatteini.toolbox.infrastructure.FrankfurterCurrencyExchangeRates
import com.cosimomatteini.toolbox.infrastructure.PackagedCurrencyRates

class ToolboxAppContainer(context: Context) {
    val convertArea = ConvertArea()
    private val defaultCurrencyRates = PackagedCurrencyRates(context.assets)
    private val fileCurrencyRates = FileCurrencyRatesRepository(context.filesDir)
    val loadCurrencyRates = LoadCurrencyRates(
        defaultCurrencyRates,
        fileCurrencyRates
    )
    val refreshCurrencyRates =
        RefreshCurrencyRates(fileCurrencyRates, FrankfurterCurrencyExchangeRates())
    val convertLength = ConvertLength()
    val convertMass = ConvertMass()
    val convertSpeed = ConvertSpeed()
    val convertTemperature = ConvertTemperature()
    val convertVolume = ConvertVolume()
    val observeCompass = ObserveCompass(AndroidCompassSensor(context))
    val tools = Tools()
}
