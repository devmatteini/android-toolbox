package com.cosimomatteini.toolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cosimomatteini.toolbox.ui.HomeScreen
import com.cosimomatteini.toolbox.ui.HomeViewModel
import com.cosimomatteini.toolbox.ui.theme.ToolboxTheme

class MainActivity : ComponentActivity() {
    private val appContainer = ToolboxAppContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToolboxTheme {
                val homeViewModel = viewModel<HomeViewModel>(
                    factory = viewModelFactory {
                        initializer { HomeViewModel(appContainer.tools) }
                    }
                )
                val uiState by homeViewModel.uiState.collectAsState()

                HomeScreen(
                    uiState = uiState,
                    onToolClick = {}
                )
            }
        }
    }
}
