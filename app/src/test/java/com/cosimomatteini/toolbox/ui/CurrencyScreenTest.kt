package com.cosimomatteini.toolbox.ui

import com.cosimomatteini.toolbox.currencyrates.CurrencyCode
import com.cosimomatteini.toolbox.domain.CurrencyUnit
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyScreenTest {
    @Test
    fun `currency label titlecases localized name`() {
        assertEquals(
            "Euro",
            currencyLabel(
                CurrencyUnit(CurrencyCode.EUR, BigDecimal.ONE),
                Locale.ITALY
            )
        )
    }

    @Test
    fun `units are sorted by localized name`() {
        val units = listOf(
            CurrencyUnit(CurrencyCode.USD, BigDecimal.ONE),
            CurrencyUnit(CurrencyCode.EUR, BigDecimal.ONE)
        )

        val ordered = orderedCurrencyUnits(units, Locale.US)

        assertTrue(currencyLabel(ordered[0], Locale.US) < currencyLabel(ordered[1], Locale.US))
    }

    @Test
    fun `source date uses the active locale`() {
        assertEquals("Aug 25, 2026", formatSourceDate(LocalDate.parse("2026-08-25"), Locale.US))
    }
}
