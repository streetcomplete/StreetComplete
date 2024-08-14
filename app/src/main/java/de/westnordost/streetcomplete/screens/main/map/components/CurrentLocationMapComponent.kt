package de.westnordost.streetcomplete.screens.main.map.components

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.location.Location
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.UiThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.gson.JsonObject
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
import de.westnordost.streetcomplete.util.ktx.isApril1st
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.math.normalizeLongitude
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.geojson.Feature

/** Takes care of showing the location + direction + accuracy marker on the map */
class CurrentLocationMapComponent(context: Context, mapStyle: Style, private val map: MapLibreMap)
    : DefaultLifecycleObserver {

    private val locationSource = GeoJsonSource(SOURCE)
    private val animation = ValueAnimator()

    /** Whether the whole thing is visible. True by default. It is only visible if both this flag
     *  is true and location is not null. */
    var isVisible: Boolean = true
        @UiThread set(value) {
            if (field == value) return
            field = value
            if (!value) hide() else show()
        }

    /** The location the GPS location dot on the map should be animated to */
    var targetLocation: Location? = null
        @UiThread set(value) {
            if (field == value) return
            field = value
            val location = this.location
            if (location == null || value == null) {
                this.location = value
                update()
            } else  {
                animation.setObjectValues(Location(location), value)
                animation.setEvaluator(locationTypeEvaluator)
                animation.start()
            }
        }

    /** The location of the GPS location dot on the map (animated) */
    var location: Location? = null
        private set

    private val locationTypeEvaluator = object : TypeEvaluator<Location> {
        override fun evaluate(fraction: Float, s: Location, e: Location): Location {
            val l = location ?: return s
            l.accuracy = s.accuracy + (e.accuracy - s.accuracy) * fraction
            l.latitude = s.latitude + (e.latitude - s.latitude) * fraction
            l.longitude = normalizeLongitude(s.longitude + (e.longitude - s.longitude) * fraction)
            return l
        }
    }

    /** The view rotation angle in degrees. Null if not set (yet) */
    var rotation: Double? = null
        @UiThread set(value) {
            if (field == value) return
            field = value
            update()
        }

    val layers: List<Layer> = listOfNotNull(
        CircleLayer("accuracy", SOURCE)
            .withProperties(
                circleColor(context.resources.getColor(R.color.location_dot)),
                circleOpacity(0.15f),
                circleRadius(inMeters(get("radius"))),
                circleStrokeColor(context.resources.getColor(R.color.location_dot)),
                circleStrokeWidth(1.0f),
                circleStrokeOpacity(0.5f),
                circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP)
            ),
        SymbolLayer("direction", SOURCE)
            .withFilter(has("rotation"))
            .withProperties(
                iconImage("directionImg"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconRotate(get("rotation")),
                iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
            ),
        SymbolLayer("location-shadow", SOURCE)
            .withProperties(
                iconImage("shadowImg"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
            ),
        CircleLayer("location", SOURCE)
            .withProperties(
                circleColor(context.resources.getColor(R.color.location_dot)),
                circleRadius(8.0f),
                circleStrokeWidth(2.0f),
                circleStrokeColor("#fff"),
                circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP)
            ),
        if (isApril1st()) {
            SymbolLayer("location-nyan", SOURCE)
                .withProperties(
                    iconImage("nyanImg"),
                    iconSize(2.0f),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
        } else null
    )

    init {
        animation.duration = 600L
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.addUpdateListener { update() }

        if (!isApril1st()) {
            mapStyle.addImage("directionImg", context.getDrawable(R.drawable.location_view_direction)!!)
            mapStyle.addImage("shadowImg", context.getDrawable(R.drawable.location_shadow)!!)
        } else {
            mapStyle.addImage("nyanImg", context.getDrawable(R.drawable.location_nyan)!!)
        }

        locationSource.isVolatile = true
        mapStyle.addSource(locationSource)
    }

    override fun onPause(owner: LifecycleOwner) {
        animation.pause()
    }

    override fun onResume(owner: LifecycleOwner) {
        animation.resume()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        animation.cancel()
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
        val location = location
        if (location == null) {
            locationSource.clear()
            return
        }
        val p = JsonObject()
        p.addProperty("radius", location.accuracy)
        rotation?.let { p.addProperty("rotation", it) }
        map.style?.getLayerAs<CircleLayer>("accuracy")?.setProperties(
            circleRadius(inMeters(get("radius"), location.latitude))
        )
        locationSource.setGeoJson(Feature.fromGeometry(location.toLatLon().toPoint(), p))
    }

    companion object {
        private const val SOURCE = "location-source"
    }
}
