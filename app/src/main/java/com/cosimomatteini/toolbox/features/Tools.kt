package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.Tool
import com.cosimomatteini.toolbox.domain.ToolId

class Tools {
    fun catalog(): List<Tool> = catalog

    private companion object {
        val catalog = listOf(
            Tool(ToolId.Compass),
            Tool(ToolId.Length),
            Tool(ToolId.Currency),
            Tool(ToolId.Speed),
            Tool(ToolId.Temperature),
            Tool(ToolId.Mass),
            Tool(ToolId.Volume),
            Tool(ToolId.Area)
        )
    }
}
