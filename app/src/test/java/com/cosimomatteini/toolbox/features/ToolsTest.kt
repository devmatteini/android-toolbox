package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.ToolId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolsTest {
    @Test
    fun `tools catalog`() {
        val tools = Tools()()

        assertEquals(
            setOf(
                ToolId.Length,
                ToolId.Mass,
                ToolId.Temperature,
                ToolId.Speed,
                ToolId.Volume,
                ToolId.Area,
                ToolId.Compass,
                ToolId.Currency
            ),
            tools.map { it.id }.toSet()
        )
    }

    @Test
    fun `catalog uniqueness`() {
        val tools = Tools()()

        assertTrue(tools.map { it.id }.distinct().size == tools.size)
    }
}
