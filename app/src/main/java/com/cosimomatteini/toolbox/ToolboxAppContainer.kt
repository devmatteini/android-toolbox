package com.cosimomatteini.toolbox

import com.cosimomatteini.toolbox.features.ConvertLength
import com.cosimomatteini.toolbox.features.ConvertMass
import com.cosimomatteini.toolbox.features.ConvertSpeed
import com.cosimomatteini.toolbox.features.ConvertTemperature
import com.cosimomatteini.toolbox.features.ConvertVolume
import com.cosimomatteini.toolbox.features.Tools

class ToolboxAppContainer {
    val convertLength = ConvertLength()
    val convertMass = ConvertMass()
    val convertSpeed = ConvertSpeed()
    val convertTemperature = ConvertTemperature()
    val convertVolume = ConvertVolume()
    val tools = Tools()
}
