package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.maplibre.compose.overlay.MapOverlayScope
import org.maplibre.compose.overlay.PointerPinButton as MapLibrePointerPinButton
import org.maplibre.compose.overlay.PointerPinButtonStyle
import org.maplibre.spatialk.geojson.Position

/** A pointer at the edge of the unobstructed map, shown while [targetPosition] lies outside
 *  its inscribed ellipse. The pin points towards the target; its content stays upright. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapOverlayScope.PointerPinButton(
    targetPosition: Position,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.surface,
    elevation: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    MapLibrePointerPinButton(
        targetPosition = targetPosition,
        modifier = modifier,
        onClick = onClick,
        style = PointerPinButtonStyle(
            containerColor = color,
            shadowElevation = elevation,
            hoveredShadowElevation = elevation
        ),
        contentPadding = contentPadding,
        content = content,
    )
}
