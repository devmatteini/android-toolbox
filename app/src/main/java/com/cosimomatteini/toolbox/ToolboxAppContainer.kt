package com.cosimomatteini.toolbox

import com.cosimomatteini.toolbox.features.ConvertLength
import com.cosimomatteini.toolbox.features.ConvertMass
import com.cosimomatteini.toolbox.features.Tools

class ToolboxAppContainer {
    val convertLength = ConvertLength()
    val convertMass = ConvertMass()
    val tools = Tools()
}
