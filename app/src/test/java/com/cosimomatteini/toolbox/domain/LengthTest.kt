package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LengthTest {
    @Test
    fun `all conversions round trip`() {
        LengthUnit.entries.forEach { source ->
            LengthUnit.entries.forEach { target ->
                val converted = convertLength(BigDecimal.ONE, source, target)
                val roundTrip = convertLength(converted, target, source)

                assertRoundTripWithinDecimal128Precision(roundTrip)
            }
        }
    }

    @Test
    fun `kilometers convert to miles`() {
        assertEquals(
            "0.62137",
            formatDecimal(
                convertLength(BigDecimal.ONE, LengthUnit.Kilometer, LengthUnit.Mile),
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
