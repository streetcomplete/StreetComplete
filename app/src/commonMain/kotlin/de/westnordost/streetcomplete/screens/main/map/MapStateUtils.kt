package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toPosition
import org.maplibre.compose.camera.CameraAnimation
import org.maplibre.compose.map.MapState
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

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

/** Zoom to the given [geometry]. */
suspend fun MapState.animateTo(
    geometry: ElementGeometry,
    animation: (zoomDiff: Double) -> CameraAnimation
) {
    val camera = cameraPosition
    val fitted = cameraForGeometry(geometry.toGeometry(), camera.bearing, camera.tilt)
    // zoom in a bit less than fully to keep a margin around the element, and not too far for points
    val targetZoom = min(fitted.zoom - 0.75, 19.0)
    val zoomDiff = abs(camera.zoom - targetZoom)
    animateCameraPosition(
        position = camera.copy(
            target = fitted.target,
            // only zoom if the difference is big enough
            zoom = if (zoomDiff > 0.5) targetZoom else camera.zoom,
        ),
        // more animation duration for longer zooms
        animation = CameraAnimation.Ease(maxOf(450, (zoomDiff * 450).roundToInt()).milliseconds),
    )
}

/** Zoom in or out by the given zoom level [amount] */
suspend fun MapState.zoomBy(amount: Double, animation: CameraAnimation) {
    val camera = cameraPosition
    animateCameraPosition(
        position = camera.copy(zoom = camera.zoom + amount),
        animation = animation
    )
}
