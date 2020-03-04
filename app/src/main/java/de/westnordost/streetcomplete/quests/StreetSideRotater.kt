package de.westnordost.streetcomplete.quests

import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import android.view.View

import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.util.centerLineOfPolyline
import de.westnordost.streetcomplete.util.initialBearingTo
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle

class StreetSideRotater(
    private val puzzle: StreetSideSelectPuzzle,
    private val compassView: View,
    geometry: ElementPolylinesGeometry
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

    private fun ElementPolylinesGeometry.getWayOrientationAtCenterLineInDegrees(): Float {
        val centerLine = polylines.first().centerLineOfPolyline()
        return centerLine.first.initialBearingTo(centerLine.second).toFloat()
    }
}
