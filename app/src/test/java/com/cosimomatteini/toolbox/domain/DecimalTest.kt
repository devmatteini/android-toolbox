package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecimalTest {
    @Test
    fun `locale-aware decimal separator`() {
        assertEquals(BigDecimal("1.5"), parseDecimal("1,5", Locale.ITALY))
        assertNull(parseDecimal("1.5", Locale.ITALY))
    }

    @Test
    fun `parsing rejects trailing text`() {
        assertNull(parseDecimal("1,5x", Locale.ITALY))
    }

    @Test
    fun `locale-aware formatting`() {
        assertEquals("1234.5", formatDecimal(BigDecimal("1234.500"), Locale.US))
        assertEquals("1234,5", formatDecimal(BigDecimal("1234.500"), Locale.ITALY))
    }

    @Test
    fun `locale-aware grouped formatting`() {
        assertEquals("99,999", formatDecimal(BigDecimal("99999"), Locale.US, useGrouping = true))
        assertEquals("99.999", formatDecimal(BigDecimal("99999"), Locale.ITALY, useGrouping = true))
    }
}
