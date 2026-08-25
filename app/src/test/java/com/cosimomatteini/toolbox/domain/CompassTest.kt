package com.cosimomatteini.toolbox.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CompassTest {
    @Test
    fun `normalizes headings into a full circle`() {
        assertEquals(CompassHeading(359), normalizeHeading(-1f))
        assertEquals(CompassHeading(0), normalizeHeading(360f))
        assertEquals(CompassHeading(1), normalizeHeading(721f))
    }

    @Test
    fun `rounds sensor headings to nearest degree`() {
        assertEquals(CompassHeading(0), normalizeHeading(359.6f))
        assertEquals(CompassHeading(90), normalizeHeading(89.5f))
    }

    @Test
    fun `maps heading boundaries to eight compass directions`() {
        assertEquals(CardinalDirection.North, cardinalDirection(CompassHeading(22)))
        assertEquals(CardinalDirection.NorthEast, cardinalDirection(CompassHeading(23)))
        assertEquals(CardinalDirection.NorthEast, cardinalDirection(CompassHeading(67)))
        assertEquals(CardinalDirection.East, cardinalDirection(CompassHeading(68)))
        assertEquals(CardinalDirection.East, cardinalDirection(CompassHeading(112)))
        assertEquals(CardinalDirection.SouthEast, cardinalDirection(CompassHeading(113)))
        assertEquals(CardinalDirection.SouthEast, cardinalDirection(CompassHeading(157)))
        assertEquals(CardinalDirection.South, cardinalDirection(CompassHeading(158)))
        assertEquals(CardinalDirection.South, cardinalDirection(CompassHeading(202)))
        assertEquals(CardinalDirection.SouthWest, cardinalDirection(CompassHeading(203)))
        assertEquals(CardinalDirection.SouthWest, cardinalDirection(CompassHeading(247)))
        assertEquals(CardinalDirection.West, cardinalDirection(CompassHeading(248)))
        assertEquals(CardinalDirection.West, cardinalDirection(CompassHeading(292)))
        assertEquals(CardinalDirection.NorthWest, cardinalDirection(CompassHeading(293)))
        assertEquals(CardinalDirection.NorthWest, cardinalDirection(CompassHeading(337)))
        assertEquals(CardinalDirection.North, cardinalDirection(CompassHeading(338)))
    }

    @Test
    fun `rotates dial opposite to heading`() {
        assertEquals(-270f, dialRotation(CompassHeading(270)), 0f)
    }

    @Test
    fun `calculates magnetic field strength from its axes`() {
        assertEquals(13f, magneticFieldStrength(3f, 4f, 12f), 0f)
    }
}
