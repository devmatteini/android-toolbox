package com.cosimomatteini.toolbox.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParsePosition
import java.util.Locale

private const val MAX_FRACTION_DIGITS = 5

fun decimalSeparator(locale: Locale): Char =
    DecimalFormatSymbols.getInstance(locale).decimalSeparator

fun parseDecimal(value: String, locale: Locale): BigDecimal? {
    if (value.isEmpty()) return null

    val parsePosition = ParsePosition(0)
    val parsed = (DecimalFormat.getNumberInstance(locale) as DecimalFormat).apply {
        isGroupingUsed = false
        isParseBigDecimal = true
    }.parse(value, parsePosition) as? BigDecimal

    return parsed?.takeIf { parsePosition.index == value.length }
}

fun formatDecimal(value: BigDecimal, locale: Locale, useGrouping: Boolean = false): String =
    (DecimalFormat.getNumberInstance(locale) as DecimalFormat).apply {
        isGroupingUsed = useGrouping
        minimumFractionDigits = 0
        maximumFractionDigits = MAX_FRACTION_DIGITS
        roundingMode = RoundingMode.HALF_UP
    }.format(value)
