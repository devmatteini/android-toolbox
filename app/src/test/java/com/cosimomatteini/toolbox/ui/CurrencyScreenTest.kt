package com.cosimomatteini.toolbox.ui

import com.cosimomatteini.toolbox.domain.CurrencyUnit
import java.math.BigDecimal
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyScreenTest {
    @Test
    fun `currency label uses localized name and code`() {
        assertEquals(
            "US Dollar (USD)",
            currencyLabel(CurrencyUnit("USD", BigDecimal.ONE), Locale.US)
        )
    }

    @Test
    fun `unknown platform currency falls back to its code`() {
        assertEquals("ZZZ (ZZZ)", currencyLabel(CurrencyUnit("ZZZ", BigDecimal.ONE), Locale.US))
    }

    @Test
    fun `units are sorted by localized name`() {
        val units = listOf(
            CurrencyUnit("USD", BigDecimal.ONE),
            CurrencyUnit("EUR", BigDecimal.ONE)
        )

        val ordered = orderedCurrencyUnits(units, Locale.US)

        assertTrue(currencyLabel(ordered[0], Locale.US) < currencyLabel(ordered[1], Locale.US))
    }

    @Test
    fun `source date uses the active locale`() {
        assertEquals("Aug 25, 2026", formatSourceDate("2026-08-25", Locale.US))
    }
}
