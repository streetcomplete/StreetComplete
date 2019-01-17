package de.westnordost.streetcomplete.quests

import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import android.view.View

import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.util.SphericalEarthMath
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle

class StreetSideRotater(
    private val puzzle: StreetSideSelectPuzzle,
    private val compassView: View,
    geometry: ElementGeometry
) {
    private val wayOrientationAtCenter = geometry.getWayOrientationAtCenterLineInDegrees()
    private val uiThread = Handler(Looper.getMainLooper())

    @AnyThread fun onMapOrientation(rotation: Float, tilt: Float) {
        uiThread.post {
            puzzle.setStreetRotation(wayOrientationAtCenter + rotation.toDegrees())
            compassView.rotation = rotation.toDegrees()
            compassView.rotationX = tilt.toDegrees()
        }
    }

    private fun Float.toDegrees() = (180 * this / Math.PI).toFloat()

    private fun ElementGeometry.getWayOrientationAtCenterLineInDegrees(): Float {
        if (polylines == null) return 0f

        val points = polylines[0]
        if (points != null && points.size > 1) {
            val centerLine = SphericalEarthMath.centerLineOfPolyline(points)
            if (centerLine != null) {
                return SphericalEarthMath.bearing(centerLine[0], centerLine[1]).toFloat()
            }
        }
        return 0f
    }
}
