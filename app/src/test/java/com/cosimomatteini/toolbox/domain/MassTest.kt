package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MassTest {
    @Test
    fun `all conversions round trip`() {
        MassUnit.entries.forEach { source ->
            MassUnit.entries.forEach { target ->
                val converted = convertMass(BigDecimal.ONE, source, target)
                val roundTrip = convertMass(converted, target, source)

                assertRoundTripWithinDecimal128Precision(roundTrip)
            }
        }
    }

    @Test
    fun `kilograms convert to pounds`() {
        assertEquals(
            "2.20462",
            formatDecimal(
                convertMass(BigDecimal.ONE, MassUnit.Kilogram, MassUnit.Pound),
                Locale.US
            )
        )
    }

    @Test
    fun `ounces convert to grams`() {
        assertEquals(
            BigDecimal("28.349523125"),
            convertMass(BigDecimal.ONE, MassUnit.Ounce, MassUnit.Gram)
        )
    }

    private fun assertRoundTripWithinDecimal128Precision(value: BigDecimal) {
        assertTrue(value.subtract(BigDecimal.ONE).abs() < ROUND_TRIP_TOLERANCE)
    }

    private companion object {
        val ROUND_TRIP_TOLERANCE = BigDecimal("0.000000000000000000000000000001")
    }
}
