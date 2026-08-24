package com.cosimomatteini.toolbox.domain

import kotlin.math.roundToInt

data class CompassHeading(val degrees: Int) {
    init {
        require(degrees in MIN_HEADING..MAX_HEADING)
    }
}

enum class CardinalDirection {
    North,
    East,
    South,
    West
}

fun normalizeHeading(degrees: Float): CompassHeading =
    CompassHeading(degrees.roundToInt().mod(FULL_CIRCLE_DEGREES))

fun cardinalDirection(heading: CompassHeading): CardinalDirection = when (
    (heading.degrees + CARDINAL_SECTOR_OFFSET) / CARDINAL_SECTOR_DEGREES
) {
    0, 4 -> CardinalDirection.North
    1 -> CardinalDirection.East
    2 -> CardinalDirection.South
    else -> CardinalDirection.West
}

fun dialRotation(heading: CompassHeading): Float = -heading.degrees.toFloat()

private const val MIN_HEADING = 0
private const val MAX_HEADING = 359
private const val FULL_CIRCLE_DEGREES = 360
private const val CARDINAL_SECTOR_DEGREES = 90
private const val CARDINAL_SECTOR_OFFSET = CARDINAL_SECTOR_DEGREES / 2
