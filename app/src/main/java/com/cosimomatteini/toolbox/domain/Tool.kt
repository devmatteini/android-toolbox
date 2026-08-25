package com.cosimomatteini.toolbox.domain

data class Tool(val id: ToolId)

enum class ToolId {
    Length,
    Mass,
    Temperature,
    Speed,
    Volume,
    Area,
    Compass,
    Currency
}
