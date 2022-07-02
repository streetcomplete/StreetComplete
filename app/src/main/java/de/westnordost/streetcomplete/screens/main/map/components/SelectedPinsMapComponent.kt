package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Point
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.Marker
import de.westnordost.streetcomplete.screens.main.map.tangram.toLngLat
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.util.ktx.pxToDp

/** Takes care of displaying "selected" pins. Those pins are always shown on top of pins displayed
 *  by the [PinsMapComponent] */
class SelectedPinsMapComponent(private val ctx: Context, private val ctrl: KtMapController) {

    private val selectedPinsLayer: MapData
    private val pinSelectionMarkers: MutableList<Marker> = mutableListOf()

    private val selectionDrawable: BitmapDrawable
    private val selectionDrawableSize: PointF

    init {
        selectionDrawable = ctx.resources.getBitmapDrawable(R.drawable.pin_selection_ring)
        selectionDrawableSize = PointF(
            ctx.pxToDp(selectionDrawable.bitmap.width),
            ctx.pxToDp(selectionDrawable.bitmap.height)
        )

        selectedPinsLayer = ctrl.addDataLayer(SELECTED_PINS_LAYER)
    }

    /** Show selected pins with the given icon at the given positions. "Selected pins" are not
     *  related to pins, they are just visuals that are displayed on top of the normal pins and look
     *  highlighted/selected. */
    fun set(@DrawableRes iconResId: Int, pinPositions: Collection<LatLon>) {
        putSelectedPins(iconResId, pinPositions)
        showPinSelectionMarkers(pinPositions)
    }

    /** Clear the display of any selected pins */
    fun clear() {
        selectedPinsLayer.clear()
        clearPinSelectionMarkers()
    }

    private fun putSelectedPins(@DrawableRes iconResId: Int, pinPositions: Collection<LatLon>) {
        val points = pinPositions.map { position ->
            Point(position.toLngLat(), mapOf(
                "type" to "point",
                "kind" to ctx.resources.getResourceEntryName(iconResId)
            ))
        }
        selectedPinsLayer.setFeatures(points)
    }

    private fun showPinSelectionMarkers(positions: Collection<LatLon>) {
        clearPinSelectionMarkers()
        for (position in positions) {
            pinSelectionMarkers.add(createPinSelectionMarker(position))
        }
    }

    private fun clearPinSelectionMarkers() {
        pinSelectionMarkers.forEach { ctrl.removeMarker(it) }
        pinSelectionMarkers.clear()
    }

    private fun createPinSelectionMarker(pos: LatLon): Marker = ctrl.addMarker().also {
        it.setStylingFromString("""
        {
            style: 'pin-selection',
            color: 'white',
            size: [${selectionDrawableSize.x}px, ${selectionDrawableSize.y}px],
            flat: false,
            collide: false,
            offset: ['0px', '-38px']
        }
        """.trimIndent())
        it.setDrawable(selectionDrawable)
        it.isVisible = true
        it.setPoint(pos)
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the below layers
        private const val SELECTED_PINS_LAYER = "streetcomplete_selected_pins"
    }
}
