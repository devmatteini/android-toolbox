package com.cosimomatteini.toolbox.ui

import com.cosimomatteini.toolbox.domain.LengthUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterScreenTest {
    private val unitOptions = listOf(
        LengthUnit.Meter to "Meter",
        LengthUnit.Kilometer to "Kilometer",
        LengthUnit.Foot to "Foot"
    )

    @Test
    fun `filters unit options by label ignoring case`() {
        assertEquals(
            listOf(LengthUnit.Kilometer to "Kilometer"),
            filterUnitOptions(unitOptions, "KILO")
        )
    }

    @Test
    fun `filters unit options by symbol`() {
        assertEquals(
            listOf(LengthUnit.Foot to "Foot"),
            filterUnitOptions(unitOptions, "FT")
        )
    }
}
