package de.westnordost.streetcomplete.quests

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.AnyThread
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.util.getOrientationAtCenterLineInDegrees
import de.westnordost.streetcomplete.view.StreetRotateable

class StreetSideRotater(
    private val puzzle: StreetRotateable,
    private val compassView: View,
    geometry: ElementPolylinesGeometry
) {
    private val wayOrientationAtCenter = geometry.getOrientationAtCenterLineInDegrees()
    private val uiHandler = Handler(Looper.getMainLooper())

    @AnyThread fun onMapOrientation(rotation: Float, tilt: Float) {
        if (uiHandler.looper.thread == Thread.currentThread()) {
            applyOrientation(rotation, tilt)
        } else {
            uiHandler.post { applyOrientation(rotation, tilt) }
        }
    }

    private fun applyOrientation(rotation: Float, tilt: Float) {
        puzzle.setStreetRotation(wayOrientationAtCenter + rotation.toDegrees())
        compassView.rotation = rotation.toDegrees()
        compassView.rotationX = tilt.toDegrees()
    }

    private fun Float.toDegrees() = (180 * this / Math.PI).toFloat()
}
