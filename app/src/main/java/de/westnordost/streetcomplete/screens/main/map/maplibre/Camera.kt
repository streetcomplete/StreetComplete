package de.westnordost.streetcomplete.screens.main.map.maplibre

import android.content.ContentResolver
import android.provider.Settings
import androidx.core.graphics.Insets
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

fun MapLibreMap.getEnclosingCamera(geometry: ElementGeometry, insets: Insets): CameraPosition? =
    getCameraForGeometry(
        geometry.toMapLibreGeometry(),
        intArrayOf(insets.left, insets.top, insets.right, insets.bottom)
    )?.toCameraPosition()

fun MapLibreMap.getEnclosingCamera(bbox: BoundingBox, insets: Insets): CameraPosition? =
    getCameraForLatLngBounds(
        bbox.toLatLngBounds(),
        intArrayOf(insets.left, insets.top, insets.right, insets.bottom)
    )?.toCameraPosition()

var MapLibreMap.camera: CameraPosition
    get() = cameraPosition.toCameraPosition()
    set(value) { cameraPosition = value.toMapLibreCameraPosition() }

fun MapLibreMap.updateCamera(duration: Int = 0, contentResolver: ContentResolver, builder: CameraUpdate.() -> Unit) {
    val update = CameraUpdate().apply(builder).toMapLibreCameraUpdate(camera)
    val animatorScale = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    if (duration == 0 || animatorScale == 0f) {
        moveCamera(update)
    } else {
        easeCamera(update, (duration * animatorScale).toInt())
    }
}

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

private fun CameraUpdate.toMapLibreCameraUpdate(cameraPosition: CameraPosition): org.maplibre.android.camera.CameraUpdate {
    resolveDeltas(cameraPosition)
    val builder = org.maplibre.android.camera.CameraPosition.Builder(cameraPosition.toMapLibreCameraPosition())
    rotation?.let { builder.bearing(it) }
    position?.let { builder.target(it.toLatLng()) }
    zoom?.let { builder.zoom(it) }
    tilt?.let { builder.tilt(it) }
    padding?.let { builder.padding(it.toDoubleArray()) }
    return CameraUpdateFactory.newCameraPosition(builder.build())
}

private fun CameraUpdate.resolveDeltas(pos: CameraPosition) {
    zoomBy?.let { zoom = pos.zoom + (zoom ?: 0.0) + it }
    tiltBy?.let { tilt = pos.tilt + (tilt ?: 0.0) + it }
    rotationBy?.let { rotation = pos.rotation + (rotation ?: 0.0) + it }
}

/** State of the camera */
data class CameraPosition(
    val position: LatLon,
    val rotation: Double,
    val tilt: Double,
    val zoom: Double,
    val padding: Padding? = null
)
data class Padding(val left: Double, val top: Double, val right: Double, val bottom: Double)

private fun org.maplibre.android.camera.CameraPosition.toCameraPosition() = CameraPosition(
    position = target?.toLatLon() ?: LatLon(0.0, 0.0),
    rotation = bearing,
    tilt = tilt,
    zoom = zoom,
    padding = padding?.toPadding()
)

private fun CameraPosition.toMapLibreCameraPosition(): org.maplibre.android.camera.CameraPosition =
    org.maplibre.android.camera.CameraPosition.Builder()
        .bearing(rotation)
        .zoom(zoom)
        .tilt(tilt)
        .target(position.toLatLng())
        .padding(padding?.toDoubleArray())
        .build()

private fun Padding.toDoubleArray() = doubleArrayOf(left, top, right, bottom)
private fun DoubleArray.toPadding() = Padding(this[0], this[1], this[2], this[3])

fun Insets.toPadding() = Padding(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble())
