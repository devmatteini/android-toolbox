package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.math.MathContext

enum class TemperatureUnit(override val symbol: String) : ConverterUnit {
    Celsius("°C"),
    Fahrenheit("°F"),
    Kelvin("K");

    override val category = ConverterCategory.Temperature
}

fun convertTemperature(
    value: BigDecimal,
    source: TemperatureUnit,
    target: TemperatureUnit
): BigDecimal {
    val kelvin = when (source) {
        TemperatureUnit.Celsius -> value.add(KELVIN_OFFSET)
        TemperatureUnit.Fahrenheit ->
            value
                .subtract(FAHRENHEIT_OFFSET)
                .multiply(FIVE)
                .divide(NINE, MathContext.DECIMAL128)
                .add(KELVIN_OFFSET)
        TemperatureUnit.Kelvin -> value
    }

    return when (target) {
        TemperatureUnit.Celsius ->
            kelvin.subtract(KELVIN_OFFSET)
        TemperatureUnit.Fahrenheit ->
            kelvin
                .subtract(KELVIN_OFFSET)
                .multiply(NINE)
                .divide(FIVE, MathContext.DECIMAL128)
                .add(FAHRENHEIT_OFFSET)
        TemperatureUnit.Kelvin ->
            kelvin
    }
}

private val KELVIN_OFFSET = BigDecimal("273.15")
private val FAHRENHEIT_OFFSET = BigDecimal("32")
private val FIVE = BigDecimal("5")
private val NINE = BigDecimal("9")
