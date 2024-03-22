package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.location.Location
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.map.maplibre.inMeters
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.ktx.toLatLon
import org.maplibre.android.style.layers.CircleLayer

/** Takes care of showing the location + direction + accuracy marker on the map */
class CurrentLocationMapComponent(context: Context, mapStyle: Style, private val map: MapLibreMap) {

    private val locationSource = GeoJsonSource("location-source")

    /** Whether the whole thing is visible. True by default. It is only visible if both this flag
     *  is true and location is not null. */
    var isVisible: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            if (!value) hide() else show()
        }

    /** The location of the GPS location dot on the map. Null if none (yet) */
    var location: Location? = null
        set(value) {
            if (field == value) return
            field = value
            update()
        }

    /** The view rotation angle in degrees. Null if not set (yet) */
    var rotation: Double? = null
        set(value) {
            if (field == value) return
            field = value
            update()
        }

    val layers: List<Layer> = listOf(
        CircleLayer("accuracy", "location-source")
            .withProperties(
                circleColor(context.resources.getColor(R.color.location_dot)),
                circleRadius(inMeters(get("radius"))),
                circleOpacity(0.15f)
            ),
        SymbolLayer("direction", "location-source")
            .withFilter(has("rotation"))
            .withProperties(
                iconImage("directionImg"),
                iconAllowOverlap(true),
                iconRotate(get("rotation")),
                iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
            ),
        SymbolLayer("location", "location-source")
            .withProperties(
                iconImage("dotImg"),
                iconAllowOverlap(true),
                iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
            ),
    )

    init {
        mapStyle.addImage("dotImg", context.getDrawable(R.drawable.location_dot)!!)
        mapStyle.addImage("directionImg", context.getDrawable(R.drawable.location_direction)!!)

        mapStyle.addSource(locationSource)
    }

    private fun hide() {
        locationSource.clear()
    }

    private fun show() {
        update()
    }

    /** Update the GPS position shown on the map */
    private fun update() {
        if (!isVisible) return
        val location = location ?: return
        val p = JsonObject()
        p.addProperty("radius", 8.5)
        rotation?.let { p.addProperty("rotation", it) }
        locationSource.setGeoJson(Feature.fromGeometry(location.toLatLon().toPoint(), p))
    }
}
