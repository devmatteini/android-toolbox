package com.cosimomatteini.toolbox.ui

import androidx.lifecycle.ViewModel
import com.cosimomatteini.toolbox.domain.ConverterUnit
import com.cosimomatteini.toolbox.domain.decimalSeparator
import com.cosimomatteini.toolbox.domain.formatDecimal
import com.cosimomatteini.toolbox.domain.parseDecimal
import java.math.BigDecimal
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConverterViewModel<U : ConverterUnit>(
    sourceUnit: U,
    targetUnit: U,
    private val convert: (BigDecimal, U, U) -> BigDecimal,
    private val locale: Locale = Locale.getDefault()
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(newState(sourceUnit, targetUnit, ZERO))

    val uiState = mutableUiState.asStateFlow()

    fun onDigit(digit: Int) {
        require(digit in 0..9)
        val sourceValue = mutableUiState.value.sourceValue
        updateSourceValue(
            when (sourceValue) {
                ZERO -> digit.toString()
                NEGATIVE_ZERO -> "$NEGATIVE_SIGN$digit"
                else -> sourceValue + digit
            }
        )
    }

    fun onDecimal() {
        val sourceValue = mutableUiState.value.sourceValue
        val separator = decimalSeparator(locale)
        if (separator !in sourceValue) {
            updateSourceValue("$sourceValue$separator")
        }
    }

    fun onDelete() {
        val sourceValue = mutableUiState.value.sourceValue.dropLast(1)
        val value = sourceValue.takeUnless {
            it.isEmpty() || it == NEGATIVE_SIGN.toString()
        } ?: ZERO
        updateSourceValue(value)
    }

    fun onClear() {
        updateSourceValue(ZERO)
    }

    fun onToggleSign() {
        val sourceValue = mutableUiState.value.sourceValue
        val value = if (sourceValue.startsWith(NEGATIVE_SIGN)) {
            sourceValue.drop(1)
        } else {
            "$NEGATIVE_SIGN$sourceValue"
        }
        updateSourceValue(value)
    }

    fun onSourceUnitSelected(unit: U) {
        val state = mutableUiState.value
        mutableUiState.value = newState(unit, state.targetUnit, state.sourceValue)
    }

    fun onTargetUnitSelected(unit: U) {
        val state = mutableUiState.value
        mutableUiState.value = newState(state.sourceUnit, unit, state.sourceValue)
    }

    fun onSwap() {
        val state = mutableUiState.value
        mutableUiState.value = newState(state.targetUnit, state.sourceUnit, state.sourceValue)
    }

    private fun updateSourceValue(value: String) {
        val state = mutableUiState.value
        mutableUiState.value = newState(state.sourceUnit, state.targetUnit, value)
    }

    private fun newState(sourceUnit: U, targetUnit: U, sourceValue: String): ConverterUiState<U> =
        ConverterUiState(
            sourceUnit = sourceUnit,
            targetUnit = targetUnit,
            sourceValue = sourceValue,
            targetValue = parseDecimal(sourceValue, locale)?.let {
                formatDecimal(convert(it, sourceUnit, targetUnit), locale)
            }.orEmpty(),
            equivalenceValue = formatDecimal(
                convert(BigDecimal.ONE, sourceUnit, targetUnit),
                locale
            )
        )

    private companion object {
        const val ZERO = "0"
        const val NEGATIVE_SIGN = '-'
        const val NEGATIVE_ZERO = "-0"
    }
}
