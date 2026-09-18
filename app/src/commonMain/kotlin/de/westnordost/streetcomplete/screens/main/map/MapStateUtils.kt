package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.toLatLon
import org.maplibre.compose.map.MapState

/** The position under the crosshair, which is drawn at the center of the map area not covered by
 *  the [padding] of an open form. This is the camera target only while that padding is applied to
 *  the camera; it is not while the form is still opening or if the map is deliberately not moved
 *  for a form. Null if the map has no size yet. */
fun MapState.crosshairPosition(padding: PaddingValues, layoutDirection: LayoutDirection): LatLon? {
    val size = viewport?.size ?: return null
    val left = padding.calculateLeftPadding(layoutDirection)
    val right = padding.calculateRightPadding(layoutDirection)
    val top = padding.calculateTopPadding()
    val bottom = padding.calculateBottomPadding()
    return positionFromScreenLocation(DpOffset(
        left + (size.width - left - right) / 2,
        top + (size.height - top - bottom) / 2,
    ))?.toLatLon()
}

/** The offset of [position] in the window, given the [mapOrigin] in the window. Null if the map
 *  has no size yet. */
fun MapState.offsetInWindow(position: LatLon, mapOrigin: Offset, density: Density): Offset? =
    screenLocationFromPosition(position.toPosition())?.let {
        with(density) { Offset(it.x.toPx(), it.y.toPx()) } + mapOrigin
    }
