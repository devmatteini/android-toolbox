package com.cosimomatteini.toolbox.ui

import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.AreaUnit
import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.domain.MassUnit
import com.cosimomatteini.toolbox.domain.SpeedUnit
import com.cosimomatteini.toolbox.domain.TemperatureUnit
import com.cosimomatteini.toolbox.domain.VolumeUnit

internal fun LengthUnit.labelRes(): Int = when (this) {
    LengthUnit.Millimeter -> R.string.length_unit_millimeter
    LengthUnit.Centimeter -> R.string.length_unit_centimeter
    LengthUnit.Meter -> R.string.length_unit_meter
    LengthUnit.Kilometer -> R.string.length_unit_kilometer
    LengthUnit.Inch -> R.string.length_unit_inch
    LengthUnit.Foot -> R.string.length_unit_foot
    LengthUnit.Yard -> R.string.length_unit_yard
    LengthUnit.Mile -> R.string.length_unit_mile
}

internal fun MassUnit.labelRes(): Int = when (this) {
    MassUnit.Milligram -> R.string.mass_unit_milligram
    MassUnit.Gram -> R.string.mass_unit_gram
    MassUnit.Kilogram -> R.string.mass_unit_kilogram
    MassUnit.Ounce -> R.string.mass_unit_ounce
    MassUnit.Pound -> R.string.mass_unit_pound
    MassUnit.Stone -> R.string.mass_unit_stone
}

internal fun SpeedUnit.labelRes(): Int = when (this) {
    SpeedUnit.MetersPerSecond -> R.string.speed_unit_meters_per_second
    SpeedUnit.KilometersPerHour -> R.string.speed_unit_kilometers_per_hour
    SpeedUnit.FeetPerSecond -> R.string.speed_unit_feet_per_second
    SpeedUnit.MilesPerHour -> R.string.speed_unit_miles_per_hour
}

internal fun TemperatureUnit.labelRes(): Int = when (this) {
    TemperatureUnit.Celsius -> R.string.temperature_unit_celsius
    TemperatureUnit.Fahrenheit -> R.string.temperature_unit_fahrenheit
    TemperatureUnit.Kelvin -> R.string.temperature_unit_kelvin
}

internal fun VolumeUnit.labelRes(): Int = when (this) {
    VolumeUnit.Millilitre -> R.string.volume_unit_millilitre
    VolumeUnit.Litre -> R.string.volume_unit_litre
    VolumeUnit.CubicMeter -> R.string.volume_unit_cubic_meter
    VolumeUnit.FluidOunce -> R.string.volume_unit_fluid_ounce
    VolumeUnit.Pint -> R.string.volume_unit_pint
    VolumeUnit.Quart -> R.string.volume_unit_quart
    VolumeUnit.ImperialGallon -> R.string.volume_unit_imperial_gallon
}

internal fun AreaUnit.labelRes(): Int = when (this) {
    AreaUnit.SquareCentimeter -> R.string.area_unit_square_centimeter
    AreaUnit.SquareMeter -> R.string.area_unit_square_meter
    AreaUnit.SquareKilometer -> R.string.area_unit_square_kilometer
    AreaUnit.Hectare -> R.string.area_unit_hectare
    AreaUnit.SquareInch -> R.string.area_unit_square_inch
    AreaUnit.SquareFoot -> R.string.area_unit_square_foot
    AreaUnit.SquareYard -> R.string.area_unit_square_yard
    AreaUnit.SquareMile -> R.string.area_unit_square_mile
    AreaUnit.Acre -> R.string.area_unit_acre
}
