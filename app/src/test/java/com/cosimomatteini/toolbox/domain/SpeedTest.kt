package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedTest {
    @Test
    fun `all conversions round trip`() {
        SpeedUnit.entries.forEach { source ->
            SpeedUnit.entries.forEach { target ->
                val converted = convertSpeed(BigDecimal.ONE, source, target)
                val roundTrip = convertSpeed(converted, target, source)

                assertRoundTripWithinDecimal128Precision(roundTrip)
            }
        }
    }

    @Test
    fun `kilometers per hour convert to meters per second`() {
        assertEquals(
            "10",
            formatDecimal(
                convertSpeed(
                    BigDecimal("36"),
                    SpeedUnit.KilometersPerHour,
                    SpeedUnit.MetersPerSecond
                ),
                Locale.US
            )
        )
    }

    @Test
    fun `miles per hour convert to feet per second`() {
        assertEquals(
            "88",
            formatDecimal(
                convertSpeed(
                    BigDecimal("60"),
                    SpeedUnit.MilesPerHour,
                    SpeedUnit.FeetPerSecond
                ),
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
