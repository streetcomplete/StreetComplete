package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.location.Location
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/** Takes care of showing the location + direction + accuracy marker on the map */
class CurrentLocationMapComponent(ctx: Context, mapStyle: Style, private val map: MapLibreMap) {

    private val locationSource = GeoJsonSource("location-source")

    private var useLocationComponent = false
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
            updateLocation()
        }

    /** The view rotation angle in degrees. Null if not set (yet) */
    var rotation: Double? = null
        set(value) {
            if (field == value) return
            field = value
            updateDirection()
        }

    /** Tell this component the current map zoom. Why does it need to know this at all? It doesn't,
     *  but it needs to know when it changed. There is no specific event for that. Whenever the
     *  zoom changed, the marker showing the accuracy must be updated because the accuracy's marker
     *  size is calculated programmatically using the current zoom. */
    var currentMapZoom: Double? = null
        set(value) {
            if (field == value) return
            field = value
            updateAccuracy()
        }

    val layers: List<Layer> = listOf(
        SymbolLayer("accuracy", "location-source")
            .withProperties(
                iconImage("accuracyImg"),
                iconAllowOverlap(true),
                iconSize(interpolate(exponential(2), zoom(),
                    stop(4, division(get("size"), literal(4096f))),
                    stop(27, division(get("size"), literal(1/4096f)))
                )),
                iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
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
        GlobalScope.launch { // simply way of delaying until map is initialized, todo: do it properly
            // this is the maplibre location component, could be used instead most of the stuff in here
            val options = LocationComponentOptions.builder(ctx)
                .bearingDrawable(R.drawable.location_direction) // todo: not displayed
                .gpsDrawable(R.drawable.location_dot) // todo: not displayed
                .build()
            val activationOptions = LocationComponentActivationOptions.builder(ctx, map.style!!)
                .locationEngine(null) // disable listening to location updates, use locationComponent.forceLocationUpdate(location)
                .locationComponentOptions(options)
                .build()
            // can also set compass engine somewhere
            MainActivity.activity?.runOnUiThread {
                map.locationComponent.activateLocationComponent(activationOptions)
                useLocationComponent = false // maybe use it? but icons not displayed, and circle looks weird on zoom
            }
        }

        mapStyle.addImage("dotImg", ctx.getDrawable(R.drawable.location_dot)!!)
        mapStyle.addImage("directionImg", ctx.getDrawable(R.drawable.location_direction)!!)
        mapStyle.addImage("accuracyImg", ctx.getDrawable(R.drawable.accuracy_circle)!!)

        mapStyle.addSource(locationSource)
    }

    private fun hide() {
        locationSource.clear()
    }

    private fun show() {
        updateLocation()
        updateDirection()
    }

    /** Update the GPS position shown on the map */
    private fun updateLocation() {
        if (!isVisible) return
        val pos = location?.toLatLon() ?: return
        val location = location ?: return
        val p = JsonObject()
        p.addProperty("size", location.accuracy / 50) // factor 50 seems ok, though probably not 100% correct
        if (rotation != null)
            p.addProperty("rotation", rotation?.toFloat() ?: 0f)

        MainActivity.activity?.runOnUiThread {
            locationSource.setGeoJson(Feature.fromGeometry(pos.toPoint(), p))
        }
        if (useLocationComponent) // do nothing instead of crashing
            map.locationComponent.forceLocationUpdate(location)
    }

    /** Update the circle that shows the GPS accuracy on the map */
    private fun updateAccuracy() {
        updateLocation()
    }

    /** Update the marker that shows the direction in which the smartphone is held */
    private fun updateDirection() {
        if (!isVisible) return
        // no sense to display direction if there is no location yet
        if (rotation == null || location == null) return
        if (!useLocationComponent)
            // todo: android.util.AndroidRuntimeException: Animators may only be run on Looper threads
            //  when using maplibre location component
            updateLocation()
    }
}
