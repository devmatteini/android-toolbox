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
import androidx.compose.foundation.layout.width
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
import com.cosimomatteini.toolbox.domain.MagneticAccuracy
import com.cosimomatteini.toolbox.domain.MagneticFieldReading
import com.cosimomatteini.toolbox.domain.cardinalDirection
import com.cosimomatteini.toolbox.domain.dialRotation
import com.cosimomatteini.toolbox.features.ObserveCompass
import com.cosimomatteini.toolbox.ui.theme.ToolboxTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassScreen(observeCompass: ObserveCompass, onBack: () -> Unit) {
    val readings = remember(observeCompass) { observeCompass() }
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
                is CompassSensorReading.Heading -> CompassHeadingContent(reading)
            }
        }
    }
}

@Composable
private fun CompassHeadingContent(reading: CompassSensorReading.Heading) {
    val heading = reading.value
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
            val dialSize = minOf(maxWidth, MAX_COMPASS_DIAL_SIZE)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(dialSize)
            ) {
                CompassDial(
                    heading = heading,
                    directionLabel = directionLabel,
                    pitchDegrees = reading.pitchDegrees,
                    rollDegrees = reading.rollDegrees,
                    magneticField = reading.magneticField,
                    dialSize = dialSize
                )
                MagneticFieldInfo(
                    magneticField = reading.magneticField,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun CompassDial(
    heading: CompassHeading,
    directionLabel: String,
    pitchDegrees: Float,
    rollDegrees: Float,
    magneticField: MagneticFieldReading,
    dialSize: Dp
) {
    val colorScheme = MaterialTheme.colorScheme
    val levelDescription = stringResource(
        R.string.compass_level_description,
        pitchDegrees,
        rollDegrees
    )
    val magneticFieldDescription = magneticField.description()
    val description = stringResource(
        R.string.compass_dial_description,
        heading.degrees,
        directionLabel,
        levelDescription,
        magneticFieldDescription
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
            drawHeadingIndicator(center, radius, colorScheme.onSurface)
            drawLevelIndicator(
                center = center,
                radius = radius,
                pitchDegrees = pitchDegrees,
                rollDegrees = rollDegrees,
                guideColor = colorScheme.onSurfaceVariant,
                bubbleColor = colorScheme.primary
            )
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
    val tickInset = TICK_INSET.toPx()
    val shortTickLength = SHORT_TICK_LENGTH.toPx()
    val longTickLength = LONG_TICK_LENGTH.toPx()

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

private fun DrawScope.drawHeadingIndicator(center: Offset, radius: Float, color: Color) {
    val majorTickEnd = center.y - radius + TICK_INSET.toPx() + LONG_TICK_LENGTH.toPx()
    val labelEnd = center.y - radius - HEADING_INDICATOR_LABEL_GAP.toPx()

    drawLine(
        color = color,
        start = Offset(center.x, majorTickEnd),
        end = Offset(center.x, labelEnd),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawLevelIndicator(
    center: Offset,
    radius: Float,
    pitchDegrees: Float,
    rollDegrees: Float,
    guideColor: Color,
    bubbleColor: Color
) {
    val targetRadius = radius * LEVEL_TARGET_RADIUS_RATIO
    val crosshairRadius = targetRadius + LEVEL_CROSSHAIR_OUTSET.toPx()
    val bubbleRadius = LEVEL_BUBBLE_RADIUS.toPx()
    val maxBubbleOffset = targetRadius - bubbleRadius - LEVEL_BUBBLE_INSET.toPx()
    val bubbleOffset = Offset(
        x = rollDegrees / MAX_LEVEL_TILT_DEGREES * maxBubbleOffset,
        y = pitchDegrees / MAX_LEVEL_TILT_DEGREES * maxBubbleOffset
    ).limitDistance(maxBubbleOffset)

    drawCircle(
        color = guideColor.copy(alpha = LEVEL_TARGET_ALPHA),
        radius = targetRadius,
        center = center
    )
    drawLine(
        color = guideColor,
        start = Offset(center.x - crosshairRadius, center.y),
        end = Offset(center.x + crosshairRadius, center.y),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = guideColor,
        start = Offset(center.x, center.y - crosshairRadius),
        end = Offset(center.x, center.y + crosshairRadius),
        strokeWidth = 1.dp.toPx()
    )
    drawCircle(color = bubbleColor, radius = bubbleRadius, center = center + bubbleOffset)
}

private fun Offset.limitDistance(maxDistance: Float): Offset {
    val distance = getDistance()

    return if (distance > maxDistance) this * (maxDistance / distance) else this
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
private fun MagneticFieldInfo(magneticField: MagneticFieldReading, modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = stringResource(R.string.compass_accuracy, magneticField.accuracyLabel()),
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = stringResource(
                R.string.compass_field_strength,
                magneticField.fieldStrengthLabel()
            ),
            style = MaterialTheme.typography.titleSmall
        )
    }
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
    CardinalDirection.NorthEast -> R.string.compass_direction_north_east
    CardinalDirection.East -> R.string.compass_direction_east
    CardinalDirection.SouthEast -> R.string.compass_direction_south_east
    CardinalDirection.South -> R.string.compass_direction_south
    CardinalDirection.SouthWest -> R.string.compass_direction_south_west
    CardinalDirection.West -> R.string.compass_direction_west
    CardinalDirection.NorthWest -> R.string.compass_direction_north_west
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
        CardinalDirection.NorthEast -> R.string.compass_direction_north_east_short
        CardinalDirection.East -> R.string.compass_direction_east_short
        CardinalDirection.SouthEast -> R.string.compass_direction_south_east_short
        CardinalDirection.South -> R.string.compass_direction_south_short
        CardinalDirection.SouthWest -> R.string.compass_direction_south_west_short
        CardinalDirection.West -> R.string.compass_direction_west_short
        CardinalDirection.NorthWest -> R.string.compass_direction_north_west_short
    }
)

@Composable
private fun MagneticFieldReading.description(): String = when (this) {
    is MagneticFieldReading.Available -> stringResource(
        R.string.compass_magnetic_field_description,
        accuracyLabel(),
        fieldStrengthLabel()
    )
    MagneticFieldReading.Unavailable -> stringResource(R.string.compass_magnetic_field_unavailable)
}

@Composable
private fun MagneticFieldReading.accuracyLabel(): String = when (this) {
    is MagneticFieldReading.Available -> stringResource(accuracy.labelRes())
    MagneticFieldReading.Unavailable -> stringResource(R.string.compass_not_available)
}

@Composable
private fun MagneticFieldReading.fieldStrengthLabel(): String = when (this) {
    is MagneticFieldReading.Available -> stringResource(
        R.string.compass_field_strength_value,
        strengthMicroteslas
    )
    MagneticFieldReading.Unavailable -> stringResource(R.string.compass_not_available)
}

private fun MagneticAccuracy.labelRes(): Int = when (this) {
    MagneticAccuracy.Unreliable -> R.string.compass_accuracy_unreliable
    MagneticAccuracy.Low -> R.string.compass_accuracy_low
    MagneticAccuracy.Medium -> R.string.compass_accuracy_medium
    MagneticAccuracy.High -> R.string.compass_accuracy_high
}

@Preview
@Composable
private fun CompassHeadingPreview() {
    ToolboxTheme(dynamicColor = false) {
        CompassScreenContent(
            reading = CompassSensorReading.Heading(
                value = CompassHeading(45),
                pitchDegrees = 3f,
                rollDegrees = -2f,
                magneticField = MagneticFieldReading.Available(48f, MagneticAccuracy.High)
            ),
            onBack = {}
        )
    }
}

@Preview
@Composable
private fun CompassMagneticFieldUnavailablePreview() {
    ToolboxTheme(dynamicColor = false) {
        CompassScreenContent(
            reading = CompassSensorReading.Heading(
                value = CompassHeading(45),
                pitchDegrees = 0f,
                rollDegrees = 0f,
                magneticField = MagneticFieldReading.Unavailable
            ),
            onBack = {}
        )
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
private val TICK_INSET = 8.dp
private val SHORT_TICK_LENGTH = 7.dp
private val LONG_TICK_LENGTH = 18.dp
private val HEADING_INDICATOR_LABEL_GAP = 12.dp
private const val LEVEL_TARGET_RADIUS_RATIO = 0.3f
private const val LEVEL_TARGET_ALPHA = 0.12f
private const val MAX_LEVEL_TILT_DEGREES = 20f
private val LEVEL_CROSSHAIR_OUTSET = 10.dp
private val LEVEL_BUBBLE_RADIUS = 6.dp
private val LEVEL_BUBBLE_INSET = 2.dp
