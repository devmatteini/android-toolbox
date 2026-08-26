package com.cosimomatteini.toolbox.ui

import androidx.lifecycle.ViewModel
import com.cosimomatteini.toolbox.features.Tools
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(tools: Tools) : ViewModel() {
    private val mutableUiState = MutableStateFlow(HomeUiState(tools()))

    val uiState = mutableUiState.asStateFlow()
}
