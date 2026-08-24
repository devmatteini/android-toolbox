package com.cosimomatteini.toolbox.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.Tool
import com.cosimomatteini.toolbox.domain.ToolId
import com.cosimomatteini.toolbox.features.Tools
import com.cosimomatteini.toolbox.ui.theme.ToolboxTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(uiState: HomeUiState, onToolClick: (Tool) -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tools_title)) })
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(uiState.tools, key = { it.id }) { tool ->
                ToolCard(tool = tool, onClick = { onToolClick(tool) })
            }
        }
    }
}

@Composable
private fun ToolCard(tool: Tool, onClick: () -> Unit) {
    val presentation = tool.presentation()
    val label = stringResource(presentation.labelRes)
    val colorScheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.95f)
            .semantics { contentDescription = label },
        border = CardDefaults.outlinedCardBorder(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainerLow
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = presentation.icon,
                    contentDescription = null,
                    tint = colorScheme.onSurface
                )
                Text(
                    text = label,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun Tool.presentation(): ToolPresentation = when (id) {
    ToolId.Length -> ToolPresentation(R.string.tool_length, Icons.Outlined.Straighten)

    ToolId.Mass -> ToolPresentation(R.string.tool_mass, Icons.Outlined.Scale)

    ToolId.Temperature -> ToolPresentation(
        R.string.tool_temperature,
        Icons.Outlined.DeviceThermostat
    )

    ToolId.Speed -> ToolPresentation(R.string.tool_speed, Icons.Outlined.Speed)

    ToolId.Volume -> ToolPresentation(R.string.tool_volume, Icons.Outlined.WaterDrop)

    ToolId.Area -> ToolPresentation(R.string.tool_area, Icons.Outlined.CropSquare)

    ToolId.Compass -> ToolPresentation(R.string.tool_compass, Icons.Outlined.Explore)
}

private data class ToolPresentation(@param:StringRes val labelRes: Int, val icon: ImageVector)

@Preview(name = "Narrow", widthDp = 360)
@Composable
private fun HomeScreenNarrowPreview() {
    ToolboxTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(Tools().catalog()),
            onToolClick = {}
        )
    }
}

@Preview(name = "Wide", widthDp = 840)
@Composable
private fun HomeScreenWidePreview() {
    ToolboxTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(Tools().catalog()),
            onToolClick = {}
        )
    }
}
