package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.clear
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.util.ktx.isApril1st
import de.westnordost.streetcomplete.util.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Takes care of showing the location + direction + accuracy marker on the map */
class CurrentLocationMapComponent(ctx: Context, mapStyle: Style, private val ctrl: KtMapController) {
    private val locationSource: GeoJsonSource
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

    init {
        GlobalScope.launch { // simply way of delaying until map is initialized, todo: do it properly
            while (MainMapFragment.mapboxMap == null) {
                delay(100)
            }
            delay(500) // just to be sure
            // this is the maplibre location component, could be used instead most of the stuff in here
            val options = LocationComponentOptions.builder(ctx)
                .bearingDrawable(R.drawable.location_direction) // todo: not displayed
                .gpsDrawable(R.drawable.location_dot) // todo: not displayed
                .build()
            val activationOptions = LocationComponentActivationOptions.builder(ctx, MainMapFragment.style!!)
                .locationEngine(null) // disable listening to location updates, use locationComponent.forceLocationUpdate(location)
                .locationComponentOptions(options)
                .build()
            // can also set compass engine somewhere
            MainActivity.activity?.runOnUiThread {
                MainMapFragment.mapboxMap?.locationComponent?.activateLocationComponent(activationOptions)
                useLocationComponent = false // maybe use it? but icons not displayed, and circle looks weird on zoom
            }
        }

        val dotImg = ctx.resources.getBitmapDrawable(if (isApril1st()) R.drawable.location_nyan else R.drawable.location_dot)
        val dotSize = PointF(
            ctx.pxToDp(dotImg.bitmap.width),
            ctx.pxToDp(dotImg.bitmap.height)
        )

        val accuracyImg = ctx.resources.getBitmapDrawable(R.drawable.accuracy_circle)
        mapStyle.addImage("dotImg", bitmapFromDrawableRes(ctx, R.drawable.location_dot)!!)
        mapStyle.addImage("directionImg", bitmapFromDrawableRes(ctx, R.drawable.location_direction)!!)
        mapStyle.addImage("accuracyImg", accuracyImg)
        val locationLayer = SymbolLayer("location", "location-source")
            .withProperties(
                PropertyFactory.iconImage("dotImg"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
            )
        val zoomExpression = Expression.interpolate(Expression.exponential(2), Expression.zoom(),
                Expression.stop(4, Expression.division(Expression.get("size"), Expression.literal(4096f))),
                Expression.stop(27, Expression.division(Expression.get("size"), Expression.literal(1/4096f)))
            )

        val accuracyLayer = SymbolLayer("accuracy", "location-source")
            .withProperties(
                PropertyFactory.iconImage("accuracyImg"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconSize(zoomExpression),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
            )
        val directionLayer = SymbolLayer("direction", "location-source")
            .withProperties(
                PropertyFactory.iconImage("directionImg"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconRotate(Expression.get("rotation")),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP)
            )
            .withFilter(Expression.has("rotation"))
        mapStyle.addLayerBelow(accuracyLayer, "pins-layer")
        mapStyle.addLayerBelow(directionLayer, "pins-layer")
        mapStyle.addLayerBelow(locationLayer, "pins-layer")
        locationSource = GeoJsonSource("location-source")
        mapStyle.addSource(locationSource)
    }

    private fun hide() {
        locationSource.clear()
    }

    private fun show() {
        updateLocation()
        updateDirection()
    }

    // actually this is unnecessary, as MapLibre creates bitmap from supplied drawable anyway -> just add dotImg / directionImg
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
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
            locationSource.setGeoJson(Feature.fromGeometry(Point.fromLngLat(pos.longitude, pos.latitude), p))
        }
        if (useLocationComponent) // do nothing instead of crashing
            MainMapFragment.mapboxMap?.locationComponent?.forceLocationUpdate(location)
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
