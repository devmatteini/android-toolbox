package com.cosimomatteini.toolbox.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.features.LoadCurrencyRates
import com.cosimomatteini.toolbox.features.RefreshCurrencyRates
import com.cosimomatteini.toolbox.features.RefreshCurrencyResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrencyRatesViewModel(
    private val loadCurrencyRates: LoadCurrencyRates,
    private val refreshCurrencyRates: RefreshCurrencyRates,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        CurrencyRatesUiState(
            rates = loadCurrencyRates()
        )
    )

    val uiState = mutableUiState.asStateFlow()

    fun onRefreshRequested() {
        refresh()
    }

    private fun refresh() {
        if (mutableUiState.value.isRefreshing) return
        mutableUiState.update { it.copy(isRefreshing = true, message = null) }
        viewModelScope.launch {
            when (val result = withContext(dispatcher) { refreshCurrencyRates() }) {
                is RefreshCurrencyResult.Updated -> updateAfterRefresh(
                    result.rates,
                    CurrencyRefreshMessage.Succeeded
                )
                RefreshCurrencyResult.Failed -> updateAfterRefresh(
                    null,
                    CurrencyRefreshMessage.Failed
                )
            }
        }
    }

    private fun updateAfterRefresh(rates: CurrencyRates?, message: CurrencyRefreshMessage?) {
        mutableUiState.update {
            it.copy(
                rates = rates ?: it.rates,
                isRefreshing = false,
                message = message
            )
        }
    }
}

data class CurrencyRatesUiState(
    val rates: CurrencyRates,
    val isRefreshing: Boolean = false,
    val message: CurrencyRefreshMessage? = null
)

enum class CurrencyRefreshMessage {
    Succeeded,
    Failed
}
