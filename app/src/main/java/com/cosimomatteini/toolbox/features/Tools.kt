package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.Tool
import com.cosimomatteini.toolbox.domain.ToolId

class Tools {
    fun catalog(): List<Tool> = catalog

    private companion object {
        val catalog = listOf(
            Tool(ToolId.Length),
            Tool(ToolId.Mass),
            Tool(ToolId.Temperature),
            Tool(ToolId.Speed),
            Tool(ToolId.Volume),
            Tool(ToolId.Area),
            Tool(ToolId.Compass)
        )
    }
}
