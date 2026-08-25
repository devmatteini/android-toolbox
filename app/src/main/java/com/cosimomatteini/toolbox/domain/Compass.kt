package com.cosimomatteini.toolbox.domain

import kotlin.math.roundToInt
import kotlin.math.sqrt

data class CompassHeading(val degrees: Int) {
    init {
        require(degrees in MIN_HEADING..MAX_HEADING)
    }
}

enum class CardinalDirection {
    North,
    NorthEast,
    East,
    SouthEast,
    South,
    SouthWest,
    West,
    NorthWest
}

fun normalizeHeading(degrees: Float): CompassHeading =
    CompassHeading(degrees.roundToInt().mod(FULL_CIRCLE_DEGREES))

fun cardinalDirection(heading: CompassHeading): CardinalDirection = when (
    (heading.degrees + COMPASS_SECTOR_OFFSET) / COMPASS_SECTOR_DEGREES
) {
    0, 8 -> CardinalDirection.North
    1 -> CardinalDirection.NorthEast
    2 -> CardinalDirection.East
    3 -> CardinalDirection.SouthEast
    4 -> CardinalDirection.South
    5 -> CardinalDirection.SouthWest
    6 -> CardinalDirection.West
    else -> CardinalDirection.NorthWest
}

fun dialRotation(heading: CompassHeading): Float = -heading.degrees.toFloat()

fun magneticFieldStrength(x: Float, y: Float, z: Float): Float = sqrt(x * x + y * y + z * z)

private const val MIN_HEADING = 0
private const val MAX_HEADING = 359
private const val FULL_CIRCLE_DEGREES = 360
private const val COMPASS_SECTOR_DEGREES = 45
private const val COMPASS_SECTOR_OFFSET = COMPASS_SECTOR_DEGREES / 2
