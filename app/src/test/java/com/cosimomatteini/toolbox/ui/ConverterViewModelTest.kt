package com.cosimomatteini.toolbox.ui

import com.cosimomatteini.toolbox.domain.CurrencyUnit
import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.domain.TemperatureUnit
import com.cosimomatteini.toolbox.domain.convertCurrency
import com.cosimomatteini.toolbox.domain.convertLength
import com.cosimomatteini.toolbox.domain.convertTemperature
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterViewModelTest {
    @Test
    fun `delete removes the final digit`() {
        val viewModel = viewModel()

        viewModel.onDigit(1)
        viewModel.onDigit(2)
        viewModel.onDelete()

        assertEquals("1", viewModel.uiState.value.sourceValue)
        assertEquals("0.001", viewModel.uiState.value.targetValue)
    }

    @Test
    fun `clear resets source and converted values to zero`() {
        val viewModel = viewModel()

        viewModel.onDigit(1)
        viewModel.onClear()

        assertEquals("0", viewModel.uiState.value.sourceValue)
        assertEquals("0", viewModel.uiState.value.targetValue)
    }

    @Test
    fun `initial source and converted values are zero`() {
        val viewModel = viewModel()

        assertEquals("0", viewModel.uiState.value.sourceValue)
        assertEquals("0", viewModel.uiState.value.targetValue)
    }

    @Test
    fun `decimal entry uses the active locale separator`() {
        val viewModel = viewModel(Locale.ITALY)

        viewModel.onDigit(1)
        viewModel.onDecimal()
        viewModel.onDigit(5)

        assertEquals("1,5", viewModel.uiState.value.sourceValue)
        assertEquals("0,0015", viewModel.uiState.value.targetValue)
    }

    @Test
    fun `swap retains source input`() {
        val viewModel = viewModel()

        viewModel.onDigit(1)
        viewModel.onSwap()

        assertEquals(LengthUnit.Kilometer, viewModel.uiState.value.sourceUnit)
        assertEquals(LengthUnit.Meter, viewModel.uiState.value.targetUnit)
        assertEquals("1", viewModel.uiState.value.sourceValue)
        assertEquals("1000", viewModel.uiState.value.targetValue)
    }

    @Test
    fun `toggle sign converts a temperature source value`() {
        val viewModel = temperatureViewModel()

        viewModel.onToggleSign()
        viewModel.onDigit(4)
        viewModel.onDigit(0)

        assertEquals("-40", viewModel.uiState.value.sourceValue)
        assertEquals("-40", viewModel.uiState.value.targetValue)
    }

    @Test
    fun `convert value again when converter changes`() {
        val euro = CurrencyUnit("EUR", java.math.BigDecimal.ONE)
        val originalDollar = CurrencyUnit("USD", java.math.BigDecimal("1.1"))
        val viewModel = ConverterViewModel(euro, originalDollar, ::convertCurrency, Locale.US)

        viewModel.onDigit(2)
        val refreshedDollar = CurrencyUnit("USD", java.math.BigDecimal("1.2"))
        viewModel.onConverterUpdated(::convertCurrency) { unit ->
            if (unit.code == "USD") refreshedDollar else euro
        }

        assertEquals("EUR", viewModel.uiState.value.sourceUnit.code)
        assertEquals("USD", viewModel.uiState.value.targetUnit.code)
        assertEquals("2", viewModel.uiState.value.sourceValue)
        assertEquals("2.4", viewModel.uiState.value.targetValue)
    }

    private fun viewModel(locale: Locale = Locale.US) = ConverterViewModel(
        sourceUnit = LengthUnit.Meter,
        targetUnit = LengthUnit.Kilometer,
        convert = ::convertLength,
        locale = locale
    )

    private fun temperatureViewModel() = ConverterViewModel(
        sourceUnit = TemperatureUnit.Celsius,
        targetUnit = TemperatureUnit.Fahrenheit,
        convert = ::convertTemperature,
        locale = Locale.US
    )
}
