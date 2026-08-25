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
    fun `maps heading boundaries to four cardinal directions`() {
        assertEquals(CardinalDirection.North, cardinalDirection(CompassHeading(44)))
        assertEquals(CardinalDirection.East, cardinalDirection(CompassHeading(45)))
        assertEquals(CardinalDirection.East, cardinalDirection(CompassHeading(134)))
        assertEquals(CardinalDirection.South, cardinalDirection(CompassHeading(135)))
        assertEquals(CardinalDirection.South, cardinalDirection(CompassHeading(224)))
        assertEquals(CardinalDirection.West, cardinalDirection(CompassHeading(225)))
        assertEquals(CardinalDirection.West, cardinalDirection(CompassHeading(314)))
        assertEquals(CardinalDirection.North, cardinalDirection(CompassHeading(315)))
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
