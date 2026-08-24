package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaTest {
    @Test
    fun `all conversions round trip`() {
        AreaUnit.entries.forEach { source ->
            AreaUnit.entries.forEach { target ->
                val converted = convertArea(BigDecimal.ONE, source, target)
                val roundTrip = convertArea(converted, target, source)

                assertRoundTripWithinDecimal128Precision(roundTrip)
            }
        }
    }

    @Test
    fun `square kilometers convert to square meters`() {
        assertEquals(
            "1000000",
            formatDecimal(
                convertArea(BigDecimal.ONE, AreaUnit.SquareKilometer, AreaUnit.SquareMeter),
                Locale.US
            )
        )
    }

    @Test
    fun `square yards convert to square feet`() {
        assertEquals(
            "9",
            formatDecimal(
                convertArea(BigDecimal.ONE, AreaUnit.SquareYard, AreaUnit.SquareFoot),
                Locale.US
            )
        )
    }

    @Test
    fun `hectares convert to acres`() {
        assertEquals(
            "2.47105",
            formatDecimal(
                convertArea(BigDecimal.ONE, AreaUnit.Hectare, AreaUnit.Acre),
                Locale.US
            )
        )
    }

    private fun assertRoundTripWithinDecimal128Precision(value: BigDecimal) {
        assertTrue(value.subtract(BigDecimal.ONE).abs() < ROUND_TRIP_TOLERANCE)
    }

    private companion object {
        val ROUND_TRIP_TOLERANCE = BigDecimal("0.000000000000000000000000000001")
    }
}
