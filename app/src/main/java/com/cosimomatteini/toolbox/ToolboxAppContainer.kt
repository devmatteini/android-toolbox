package com.cosimomatteini.toolbox

import android.content.Context
import com.cosimomatteini.toolbox.features.ConvertArea
import com.cosimomatteini.toolbox.features.ConvertLength
import com.cosimomatteini.toolbox.features.ConvertMass
import com.cosimomatteini.toolbox.features.ConvertSpeed
import com.cosimomatteini.toolbox.features.ConvertTemperature
import com.cosimomatteini.toolbox.features.ConvertVolume
import com.cosimomatteini.toolbox.features.ObserveCompass
import com.cosimomatteini.toolbox.features.Tools
import com.cosimomatteini.toolbox.infrastructure.AndroidCompassSensor

class ToolboxAppContainer(context: Context) {
    val convertArea = ConvertArea()
    val convertLength = ConvertLength()
    val convertMass = ConvertMass()
    val convertSpeed = ConvertSpeed()
    val convertTemperature = ConvertTemperature()
    val convertVolume = ConvertVolume()
    val observeCompass = ObserveCompass(AndroidCompassSensor(context))
    val tools = Tools()
}
