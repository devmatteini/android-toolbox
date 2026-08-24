package com.cosimomatteini.toolbox.ui

import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.domain.TemperatureUnit
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
