package com.cosimomatteini.toolbox.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosimomatteini.toolbox.R
import com.cosimomatteini.toolbox.domain.CardinalDirection
import com.cosimomatteini.toolbox.domain.CompassHeading
import com.cosimomatteini.toolbox.domain.CompassSensorReading
import com.cosimomatteini.toolbox.domain.cardinalDirection
import com.cosimomatteini.toolbox.domain.dialRotation
import com.cosimomatteini.toolbox.features.ObserveCompass
import com.cosimomatteini.toolbox.ui.theme.ToolboxTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassScreen(observeCompass: ObserveCompass, onBack: () -> Unit) {
    val readings = remember(observeCompass) { observeCompass.readings() }
    val reading by readings.collectAsState(initial = null)

    CompassScreenContent(reading = reading, onBack = onBack)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CompassScreenContent(reading: CompassSensorReading?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tool_compass)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.converter_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp, vertical = 24.dp)
        ) {
            when (reading) {
                null -> Text(
                    text = stringResource(R.string.compass_loading),
                    style = MaterialTheme.typography.bodyLarge
                )

                CompassSensorReading.Unavailable -> CompassUnavailable()
                is CompassSensorReading.Heading -> CompassHeadingContent(reading.value)
            }
        }
    }
}

@Composable
private fun CompassHeadingContent(heading: CompassHeading) {
    val direction = cardinalDirection(heading)
    val directionLabel = stringResource(direction.labelRes())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.compass_heading, heading.degrees),
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = direction.shortLabel(),
                color = direction.color(),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.semantics { contentDescription = directionLabel }
            )
        }
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            CompassDial(
                heading = heading,
                directionLabel = directionLabel,
                dialSize = minOf(maxWidth, MAX_COMPASS_DIAL_SIZE)
            )
        }
    }
}

@Composable
private fun CompassDial(heading: CompassHeading, directionLabel: String, dialSize: Dp) {
    val colorScheme = MaterialTheme.colorScheme
    val description = stringResource(
        R.string.compass_dial_description,
        heading.degrees,
        directionLabel
    )

    Box(
        modifier = Modifier
            .size(dialSize)
            .semantics { contentDescription = description }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f - RING_INSET.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCompassFace(center, radius, colorScheme.surfaceContainerHigh, colorScheme.outline)
            drawCompassTicks(heading, center, radius, colorScheme.onSurfaceVariant)
            drawDegreeLabels(heading, center, radius, colorScheme.onSurface)
            drawHeadingArrow(center, radius, colorScheme.primary)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = dialRotation(heading) }
        ) {
            DialLetter(
                CardinalDirection.North.shortLabel(),
                NORTH_RED,
                Modifier.align(Alignment.TopCenter)
            )
            DialLetter(
                CardinalDirection.East.shortLabel(),
                colorScheme.onSurface,
                Modifier.align(Alignment.CenterEnd)
            )
            DialLetter(
                CardinalDirection.South.shortLabel(),
                colorScheme.onSurface,
                Modifier.align(Alignment.BottomCenter)
            )
            DialLetter(
                CardinalDirection.West.shortLabel(),
                colorScheme.onSurface,
                Modifier.align(Alignment.CenterStart)
            )
        }
    }
}

private fun DrawScope.drawCompassFace(
    center: Offset,
    radius: Float,
    backgroundColor: Color,
    outlineColor: Color
) {
    drawCircle(color = backgroundColor, radius = radius, center = center)
    drawCircle(
        color = outlineColor,
        radius = radius - 1.dp.toPx(),
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawCompassTicks(
    heading: CompassHeading,
    center: Offset,
    radius: Float,
    tickColor: Color
) {
    val tickInset = 8.dp.toPx()
    val shortTickLength = 7.dp.toPx()
    val longTickLength = 18.dp.toPx()

    rotate(degrees = dialRotation(heading), pivot = center) {
        repeat(TICK_COUNT) { index ->
            rotate(degrees = index * TICK_DEGREES, pivot = center) {
                val majorTick = index % MAJOR_TICK_INTERVAL == 0
                drawLine(
                    color = if (index == 0) NORTH_RED else tickColor,
                    start = Offset(center.x, center.y - radius + tickInset),
                    end = Offset(
                        center.x,
                        center.y - radius + tickInset +
                            if (majorTick) longTickLength else shortTickLength
                    ),
                    strokeWidth = if (majorTick) 3.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private fun DrawScope.drawDegreeLabels(
    heading: CompassHeading,
    center: Offset,
    radius: Float,
    labelColor: Color
) {
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = labelColor.toArgb()
        textAlign = Paint.Align.CENTER
        textSize = 18.sp.toPx()
    }
    val labelRadius = radius + 30.dp.toPx()

    drawIntoCanvas { canvas ->
        repeat(DEGREE_LABEL_COUNT) { index ->
            val degree = index * DEGREE_LABEL_INTERVAL
            val angle = Math.toRadians((-90f - heading.degrees + degree).toDouble())
            val x = center.x + labelRadius * cos(angle).toFloat()
            val y = center.y + labelRadius * sin(angle).toFloat()
            val baseline = y - (textPaint.ascent() + textPaint.descent()) / 2f

            canvas.nativeCanvas.drawText(degree.toString(), x, baseline, textPaint)
        }
    }
}

private fun DrawScope.drawHeadingArrow(center: Offset, radius: Float, color: Color) {
    val arrowTip = Offset(center.x, center.y - radius * 0.45f)
    val arrowBase = arrowTip.y + 12.dp.toPx()

    drawLine(
        color = color,
        start = center,
        end = Offset(center.x, arrowBase),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawPath(
        path = Path().apply {
            moveTo(arrowTip.x, arrowTip.y)
            lineTo(arrowTip.x - 7.dp.toPx(), arrowBase)
            lineTo(arrowTip.x + 7.dp.toPx(), arrowBase)
            close()
        },
        color = color
    )
}

@Composable
private fun DialLetter(letter: String, color: Color, modifier: Modifier) {
    Text(
        text = letter,
        color = color,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier.padding(82.dp)
    )
}

@Composable
private fun CompassUnavailable() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Explore,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.compass_unavailable),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun CardinalDirection.labelRes(): Int = when (this) {
    CardinalDirection.North -> R.string.compass_direction_north
    CardinalDirection.East -> R.string.compass_direction_east
    CardinalDirection.South -> R.string.compass_direction_south
    CardinalDirection.West -> R.string.compass_direction_west
}

@Composable
private fun CardinalDirection.color(): Color = if (this == CardinalDirection.North) {
    NORTH_RED
} else {
    MaterialTheme.colorScheme.onSurface
}

@Composable
private fun CardinalDirection.shortLabel(): String = stringResource(
    when (this) {
        CardinalDirection.North -> R.string.compass_direction_north_short
        CardinalDirection.East -> R.string.compass_direction_east_short
        CardinalDirection.South -> R.string.compass_direction_south_short
        CardinalDirection.West -> R.string.compass_direction_west_short
    }
)

@Preview
@Composable
private fun CompassHeadingPreview() {
    ToolboxTheme(dynamicColor = false) {
        CompassScreenContent(reading = CompassSensorReading.Heading(CompassHeading(45)), onBack = {
        })
    }
}

@Preview
@Composable
private fun CompassUnavailablePreview() {
    ToolboxTheme(dynamicColor = false) {
        CompassScreenContent(reading = CompassSensorReading.Unavailable, onBack = {})
    }
}

private val MAX_COMPASS_DIAL_SIZE = 360.dp
private val NORTH_RED = Color(0xFFB3261E)
private val RING_INSET = 50.dp
private const val TICK_COUNT = 72
private const val TICK_DEGREES = 360f / TICK_COUNT
private const val MAJOR_TICK_INTERVAL = 6
private const val DEGREE_LABEL_COUNT = 12
private const val DEGREE_LABEL_INTERVAL = 30
