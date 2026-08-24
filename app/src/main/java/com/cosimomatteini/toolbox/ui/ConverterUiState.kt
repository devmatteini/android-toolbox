package com.cosimomatteini.toolbox.ui

import com.cosimomatteini.toolbox.domain.ConverterUnit

data class ConverterUiState<U : ConverterUnit>(
    val sourceUnit: U,
    val targetUnit: U,
    val sourceValue: String = "",
    val targetValue: String = "",
    val equivalenceValue: String
)
