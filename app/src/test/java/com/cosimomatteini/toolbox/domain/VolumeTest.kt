package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeTest {
    @Test
    fun `all conversions round trip`() {
        VolumeUnit.entries.forEach { source ->
            VolumeUnit.entries.forEach { target ->
                val converted = convertVolume(BigDecimal.ONE, source, target)
                val roundTrip = convertVolume(converted, target, source)

                assertRoundTripWithinDecimal128Precision(roundTrip)
            }
        }
    }

    @Test
    fun `millilitres convert to litres`() {
        assertEquals(
            "1",
            formatDecimal(
                convertVolume(BigDecimal("1000"), VolumeUnit.Millilitre, VolumeUnit.Litre),
                Locale.US
            )
        )
    }

    @Test
    fun `litres convert to Imperial gallons`() {
        assertEquals(
            "1",
            formatDecimal(
                convertVolume(
                    BigDecimal("4.54609"),
                    VolumeUnit.Litre,
                    VolumeUnit.ImperialGallon
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
