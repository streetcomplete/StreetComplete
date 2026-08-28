package de.westnordost.streetcomplete.screens.main.map.components

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.UiThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.gson.JsonObject
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.inMeters
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.ktx.isApril1st
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import de.westnordost.streetcomplete.util.math.normalizeLongitude
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.compose.location.PositionWithAccuracy
import org.maplibre.geojson.Feature
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.International

/** Takes care of showing the location + direction + accuracy marker on the map */
class CurrentLocationMapComponent(context: Context, mapStyle: Style, private val map: MapLibreMap) :
    DefaultLifecycleObserver {

    private val locationSource = GeoJsonSource(SOURCE)
    private val locationAnimation = ValueAnimator()

    /** The location the GPS location dot on the map should be animated to */
    var targetPositionWithAccuracy: PositionWithAccuracy? = null
        @UiThread set(value) {
            if (field == value) return
            field = value
            val positionWithAccuracy = this.positionWithAccuracy
            if (positionWithAccuracy == null || value == null) {
                locationAnimation.cancel()
                this.positionWithAccuracy = value
                update()
            } else  {
                locationAnimation.setObjectValues(positionWithAccuracy, value)
                locationAnimation.setEvaluator(locationTypeEvaluator)
                locationAnimation.start()
            }
        }

    /** The location of the GPS location dot on the map (animated) */
    var positionWithAccuracy: PositionWithAccuracy? = null
        private set

    private val locationTypeEvaluator = object : TypeEvaluator<PositionWithAccuracy> {
        override fun evaluate(fraction: Float, s: PositionWithAccuracy, e: PositionWithAccuracy): PositionWithAccuracy {
            val sp = s.value
            val ep = e.value
            val sa = s.accuracy
            val ea = e.accuracy
            return PositionWithAccuracy(
                value = Position(
                    longitude = normalizeLongitude(sp.longitude + (ep.longitude - sp.longitude) * fraction),
                    latitude = sp.latitude + (ep.latitude - sp.latitude) * fraction,
                ),
                accuracy = if (ea != null && sa != null) {
                    sa + (ea - sa) * fraction.toDouble()
                } else { null }
            )
        }
    }

    /** The view rotation angle in degrees. Null if not set (yet) */
    var targetRotation: Float? = null
        @UiThread set(value) {
            if (field == value) return
            field = value
            val rotation = this.rotation
            if (rotation == null || value == null) {
                rotationAnimation.cancel()
                this.rotation = value
                update()
            } else {
                rotationAnimation.setFloatValues(rotation, normalizeDegrees(value, rotation - 180))
                rotationAnimation.start()
            }
        }
    private val rotationAnimation = ValueAnimator()

    var rotation: Float? = null
        private set

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
        } else {
            null
        }
    )

    init {
        locationAnimation.duration = 600L
        locationAnimation.interpolator = AccelerateDecelerateInterpolator()
        locationAnimation.addUpdateListener {
            positionWithAccuracy = locationAnimation.animatedValue as PositionWithAccuracy
            update()
        }

        rotationAnimation.duration = 200L
        rotationAnimation.interpolator = AccelerateDecelerateInterpolator()
        rotationAnimation.addUpdateListener {
            rotation = rotationAnimation.animatedValue as Float
            update()
        }

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
        locationAnimation.pause()
        rotationAnimation.pause()
    }

    override fun onResume(owner: LifecycleOwner) {
        locationAnimation.resume()
        rotationAnimation.resume()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        locationAnimation.cancel()
        rotationAnimation.cancel()
    }

    private fun hide() {
        locationSource.clear()
    }

    private fun show() {
        update()
    }

    /** Update the GPS position shown on the map */
    private fun update() {
        val positionWithAccuracy = this.positionWithAccuracy
        if (positionWithAccuracy == null) {
            locationSource.clear()
            return
        }
        val pos = positionWithAccuracy.value

        val p = JsonObject()
        p.addProperty("radius", positionWithAccuracy.accuracy?.toDouble(International.Meters))
        rotation?.let { p.addProperty("rotation", it) }
        map.style?.getLayerAs<CircleLayer>("accuracy")?.setProperties(
            circleRadius(inMeters(get("radius"), pos.latitude))
        )
        locationSource.setGeoJson(Feature.fromGeometry(LatLon(pos.latitude, pos.longitude).toPoint(), p))
    }

    companion object {
        private const val SOURCE = "location-source"
    }
}
