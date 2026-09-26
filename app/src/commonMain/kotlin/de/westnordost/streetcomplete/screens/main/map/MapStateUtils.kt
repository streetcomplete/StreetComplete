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
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.map.MapState
import org.maplibre.compose.util.DpPadding
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/** The position shown at the center of the map area inside the given [padding], e.g. where the
 *  crosshair is drawn while a form is open. Null if the map has no size yet. */
fun MapState.positionAtCenter(padding: DpPadding): LatLon? {
    val size = viewport?.size ?: return null
    return positionFromScreenLocation(DpOffset(
        padding.left + (size.width - padding.left - padding.right) / 2,
        padding.top + (size.height - padding.top - padding.bottom) / 2,
    ))?.toLatLon()
}

fun PaddingValues.toDpPadding(layoutDirection: LayoutDirection) = DpPadding(
    left = calculateLeftPadding(layoutDirection),
    top = calculateTopPadding(),
    right = calculateRightPadding(layoutDirection),
    bottom = calculateBottomPadding(),
)

/** The offset of [position] in the window, given the [mapOrigin] in the window. Null if the map
 *  has no size yet. */
fun MapState.offsetInWindow(position: LatLon, mapOrigin: Offset, density: Density): Offset? =
    screenLocationFromPosition(position.toPosition())?.let {
        with(density) { Offset(it.x.toPx(), it.y.toPx()) } + mapOrigin
    }

/** Zoom to the given [geometry]. */
suspend fun MapState.animateTo(geometry: ElementGeometry, padding: DpPadding) {
    val camera = cameraPosition
    val fitted = cameraForGeometry(geometry.toGeometry(), camera.bearing, camera.tilt, cameraPadding = padding)
    // zoom in a bit less than fully to keep a margin around the element, and not too far for points
    val targetZoom = min(fitted.zoom - 0.75, 19.0)
    val zoomDiff = abs(camera.zoom - targetZoom)
    animateCamera(
        update = CameraUpdate(
            target = fitted.target,
            // only zoom if the difference is big enough
            zoom = if (zoomDiff > 0.5) targetZoom else null,
            padding = padding,
        ),
        // more animation duration for longer zooms
        animation = CameraAnimation.Ease(maxOf(450, (zoomDiff * 450).roundToInt()).milliseconds),
    )
}
