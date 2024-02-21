package de.westnordost.streetcomplete.screens.main.map.tangram

import android.content.ContentResolver
import android.provider.Settings
import androidx.annotation.AnyThread
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.math.PI
import kotlin.math.min

/**
 *  Controls the camera of a Tangram MapController. Use in place of the
 *  MapController.updateCameraPosition methods to enable parallel animations with an easy API.
 *  Usage example:
 *
 *  // move to new position within 500ms and at the same time zoom to level 18.5 and tilt within 1000ms
 *  cameraManager.updateCamera(500) {
 *    position = LngLat(2.0, 3.0)
 *  }
 *  cameraManager.updateCamera(1000) {
 *    zoom = 18.5
 *    tilt = 0.4
 *  }
 *
 *  See https://github.com/tangrams/tangram-es/issues/1962
 *  */
class CameraManager(private val mapboxMap: MapboxMap, private val contentResolver: ContentResolver) {
    val camera: ScCameraPosition get() = ScCameraPosition(mapboxMap.cameraPosition)

    var maximumTilt: Double = 60.0 // maplibre is using degrees

    @AnyThread fun updateCamera(duration: Int = 0, update: CameraUpdate) {
        synchronized(mapboxMap) { // todo: where to synchronize? is it necessary a all?
            update.resolveDeltas(camera)
            val cameraPositionBuilder = CameraPosition.Builder(mapboxMap.cameraPosition)
            update.rotation?.let { cameraPositionBuilder.bearing(it) }
            update.position?.let { cameraPositionBuilder.target(it.toLatLng()) }
            update.zoom?.let { cameraPositionBuilder.zoom(it) }
            update.tilt?.let { cameraPositionBuilder.tilt(min(it, maximumTilt)) }
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPositionBuilder.build())
            if (duration == 0 || isAnimationsOff) {
                mapboxMap.moveCamera(cameraUpdate)
            } else {
                mapboxMap.easeCamera(cameraUpdate, duration)
            }
        }
    }

    private val isAnimationsOff get() =
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}

class CameraUpdate {
    var position: LatLon? = null
    var rotation: Double? = null
    var tilt: Double? = null
    var zoom: Double? = null

    var zoomBy: Double? = null
    var tiltBy: Double? = null
    var rotationBy: Double? = null
}

data class ScCameraPosition(
    val position: LatLon,
    val rotation: Double,
    val tilt: Double,
    val zoom: Double
) {
    constructor(p: CameraPosition) : this(
        p.target?.toLatLon() ?: LatLon(0.0, 0.0),
        -p.bearing * PI / 180.0,
        p.tilt,
        p.zoom
    )
}

fun LatLng.toLatLon() = LatLon(latitude, longitude)

private fun CameraUpdate.resolveDeltas(pos: ScCameraPosition) {
    zoomBy?.let { zoom = pos.zoom + (zoom ?: 0.0) + it }
    tiltBy?.let { tilt = pos.tilt + (tilt ?: 0.0) + it }
    rotationBy?.let { rotation = pos.rotation + (rotation ?: 0.0) + it }
}
