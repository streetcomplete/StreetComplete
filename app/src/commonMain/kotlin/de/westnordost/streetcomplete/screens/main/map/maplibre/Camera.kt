package de.westnordost.streetcomplete.screens.main.map.maplibre

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

/** State of the camera */
data class CameraPosition(
    val position: LatLon,
    val rotation: Double,
    val tilt: Double,
    val zoom: Double,
    val padding: Padding? = null
)
data class Padding(val left: Double, val top: Double, val right: Double, val bottom: Double)

/** Builder data class for camera updates */
class CameraUpdate {
    var position: LatLon? = null
    var rotation: Double? = null // degrees
    var tilt: Double? = null // degrees
    var zoom: Double? = null
    var padding: Padding? = null

    var zoomBy: Double? = null
    var tiltBy: Double? = null
    var rotationBy: Double? = null
}

fun PaddingValues.toPadding(layoutDirection: LayoutDirection, density: Density) = with(density) {
    Padding(
        left = calculateLeftPadding(layoutDirection).toPx().toDouble(),
        top = calculateTopPadding().toPx().toDouble(),
        right = calculateRightPadding(layoutDirection).toPx().toDouble(),
        bottom = calculateBottomPadding().toPx().toDouble()
    )
}
