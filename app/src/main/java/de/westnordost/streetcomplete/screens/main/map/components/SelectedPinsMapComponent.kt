package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint

/** Takes care of displaying "selected" pins. Those pins are always shown on top of pins displayed
 *  by the [PinsMapComponent] */
class SelectedPinsMapComponent(private val context: Context, private val map: MapboxMap) {

    private val selectedPinsSource = GeoJsonSource("selected-pins-source")

    val layers: List<Layer> = listOf(
        SymbolLayer("selected-pins-layer", "selected-pins-source")
            .withProperties(
                iconImage(get("icon-image")),
                iconOffset(listOf(-8f, -33f).toTypedArray()),
            )
    )

    init {
        map.style?.addSource(selectedPinsSource)
    }

    /** Show selected pins with the given icon at the given positions. "Selected pins" are not
     *  related to pins, they are just visuals that are displayed on top of the normal pins and look
     *  highlighted/selected. */
    @UiThread fun set(@DrawableRes iconResId: Int, pinPositions: Collection<LatLon>) {
        val p = JsonObject()
        p.addProperty("icon-image", context.resources.getResourceEntryName(iconResId))
        val points = pinPositions.map { Feature.fromGeometry(it.toPoint(), p) }
        selectedPinsSource.setGeoJson(FeatureCollection.fromFeatures(points))
    }

    /** Clear the display of any selected pins */
    @UiThread fun clear() {
        selectedPinsSource.clear()
    }
}
