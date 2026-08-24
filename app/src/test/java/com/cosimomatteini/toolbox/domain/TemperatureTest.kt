package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class TemperatureTest {
    @Test
    fun `freezing point converts between scales`() {
        assertValue(
            BigDecimal("32"),
            convertTemperature(
                BigDecimal.ZERO,
                TemperatureUnit.Celsius,
                TemperatureUnit.Fahrenheit
            )
        )
        assertValue(
            BigDecimal("273.15"),
            convertTemperature(
                BigDecimal("32"),
                TemperatureUnit.Fahrenheit,
                TemperatureUnit.Kelvin
            )
        )
    }

    @Test
    fun `boiling point converts between scales`() {
        assertValue(
            BigDecimal("212"),
            convertTemperature(
                BigDecimal("100"),
                TemperatureUnit.Celsius,
                TemperatureUnit.Fahrenheit
            )
        )
        assertValue(
            BigDecimal("373.15"),
            convertTemperature(
                BigDecimal("100"),
                TemperatureUnit.Celsius,
                TemperatureUnit.Kelvin
            )
        )
    }

    @Test
    fun `negative temperature converts between Celsius and Fahrenheit`() {
        assertValue(
            BigDecimal("-40"),
            convertTemperature(
                BigDecimal("-40"),
                TemperatureUnit.Celsius,
                TemperatureUnit.Fahrenheit
            )
        )
    }

    @Test
    fun `zero Kelvin converts to absolute zero in other scales`() {
        assertValue(
            BigDecimal("-273.15"),
            convertTemperature(
                BigDecimal.ZERO,
                TemperatureUnit.Kelvin,
                TemperatureUnit.Celsius
            )
        )
        assertValue(
            BigDecimal("-459.67"),
            convertTemperature(
                BigDecimal.ZERO,
                TemperatureUnit.Kelvin,
                TemperatureUnit.Fahrenheit
            )
        )
    }

    private fun assertValue(expected: BigDecimal, actual: BigDecimal) {
        assertEquals(0, expected.compareTo(actual))
    }
}
