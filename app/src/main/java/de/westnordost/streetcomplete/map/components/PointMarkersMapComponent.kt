package de.westnordost.streetcomplete.map.components

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.map.tangram.KtMapController

/** Manages putting some generic point markers with a drawable on the map */
class PointMarkersMapComponent(private val resources: Resources, private val ctrl: KtMapController) {

    // markers: LatLon -> Marker Id
    private val markerIds: MutableMap<LatLon, Long> = HashMap()

    // cache for all drawable res ids supplied so far
    private val drawables: MutableMap<Int, BitmapDrawable> = HashMap()

    @Synchronized fun put(pos: LatLon, @DrawableRes drawableResId: Int) {
        delete(pos)
        val marker = ctrl.addMarker()
        val drawable = getBitmapDrawable(drawableResId)
        marker.setDrawable(drawable)
        marker.setStylingFromString("""
        {
            style: 'points',
            color: 'white',
            size: ${drawable.intrinsicWidth / resources.displayMetrics.density}px,
            order: 2000,
            collide: false
        }
        """.trimIndent())
        marker.setPoint(pos)
        markerIds[pos] = marker.markerId
    }

    @Synchronized fun delete(pos: LatLon): Boolean {
        val markerId = markerIds[pos] ?: return false
        markerIds.remove(pos)
        return ctrl.removeMarker(markerId)
    }

    @Synchronized fun clear() {
        for (markerId in markerIds.values) {
            ctrl.removeMarker(markerId)
        }
        markerIds.clear()
    }

    private fun getBitmapDrawable(@DrawableRes drawableResId: Int): BitmapDrawable {
        if (drawables[drawableResId] == null) {
            drawables[drawableResId] = resources.getBitmapDrawable(drawableResId)
        }
        return drawables[drawableResId]!!
    }
}
