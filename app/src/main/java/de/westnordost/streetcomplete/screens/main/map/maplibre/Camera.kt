package de.westnordost.streetcomplete.screens.main.map.maplibre

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

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

fun CameraUpdate.toMapLibreCameraUpdate(cameraPosition: CameraPosition): com.mapbox.mapboxsdk.camera.CameraUpdate {
    resolveDeltas(cameraPosition)
    val builder = com.mapbox.mapboxsdk.camera.CameraPosition.Builder(cameraPosition.toMapLibreCameraPosition())
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

fun com.mapbox.mapboxsdk.camera.CameraPosition.toCameraPosition() =
    CameraPosition(
        position = target?.toLatLon() ?: LatLon(0.0, 0.0),
        rotation = -bearing,
        tilt = tilt,
        zoom = zoom,
        padding = padding?.toPadding()
    )

fun CameraPosition.toMapLibreCameraPosition(): com.mapbox.mapboxsdk.camera.CameraPosition =
    com.mapbox.mapboxsdk.camera.CameraPosition.Builder()
        .bearing(rotation)
        .zoom(zoom)
        .tilt(tilt)
        .target(position.toLatLng())
        .padding(padding?.toDoubleArray())
        .build()

data class Padding(val left: Double, val top: Double, val right: Double, val bottom: Double)

private fun Padding.toDoubleArray() = doubleArrayOf(left, top, right, bottom)
private fun DoubleArray.toPadding() = Padding(this[0], this[1], this[2], this[3])
