package com.cosimomatteini.toolbox.features

import com.cosimomatteini.toolbox.domain.ToolId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolsTest {
    @Test
    fun `tools catalog`() {
        val tools = Tools().catalog()

        assertEquals(
            listOf(
                ToolId.Length,
                ToolId.Mass,
                ToolId.Temperature,
                ToolId.Speed,
                ToolId.Volume,
                ToolId.Area,
                ToolId.Compass
            ),
            tools.map { it.id }
        )
    }

    @Test
    fun `catalog uniqueness`() {
        val tools = Tools().catalog()

        assertTrue(tools.map { it.id }.distinct().size == tools.size)
    }
}
