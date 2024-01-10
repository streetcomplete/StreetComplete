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
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.expressions.Expression
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.util.ktx.isApril1st
import de.westnordost.streetcomplete.util.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.EARTH_CIRCUMFERENCE
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

/** Takes care of showing the location + direction + accuracy marker on the map */
class CurrentLocationMapComponent(ctx: Context, mapStyle: Style, private val symbolManager: SymbolManager, private val ctrl: KtMapController) {
    // markers showing the user's location, direction and accuracy of location
    private val locationSymbol: Symbol
    private val accuracySymbol: Symbol
    private val directionSymbol: Symbol

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
    var currentMapZoom: Float? = null
        set(value) {
            if (field == value) return
            field = value
            updateAccuracy()
        }

    init {
        // todo: use location component instead of symbol manager
        //  problem: accuracy circle is choppy when zooming (huge performance impact) -> maybe stay with symbols...
        //  and other icons aren't displayed, but that hopefully can be fixed
        GlobalScope.launch {
            while (MainMapFragment.mapboxMap == null) {
                delay(100)
            }
            delay(500) // just to be sure
            val lc = MainMapFragment.mapboxMap!!.locationComponent
            val options = LocationComponentOptions.builder(ctx)
                .bearingDrawable(R.drawable.location_direction) // todo: not displayed
                .gpsDrawable(R.drawable.location_dot) // todo: not displayed
                .build()
            val activationOptions = LocationComponentActivationOptions.builder(ctx, MainMapFragment.style!!)
                .locationEngine(null) // is this enough to disable? then lc.forceLocationUpdate(loc) pushes location update
                .locationComponentOptions(options)
                .build()
            // can also set compass engine somewhere
            MainActivity.activity?.runOnUiThread { lc.activateLocationComponent(activationOptions) }
        }

        val dotImg = ctx.resources.getBitmapDrawable(if (isApril1st()) R.drawable.location_nyan else R.drawable.location_dot)
        val dotSize = PointF(
            ctx.pxToDp(dotImg.bitmap.width),
            ctx.pxToDp(dotImg.bitmap.height)
        )

        val accuracyImg = ctx.resources.getBitmapDrawable(R.drawable.accuracy_circle)

        symbolManager.iconAllowOverlap = true
//        symbolManager.setFilter(Expression.literal(false)) // disable for testing location component
        mapStyle.addImage("dotImg", bitmapFromDrawableRes(ctx, R.drawable.location_dot)!!)
        mapStyle.addImage("directionImg", bitmapFromDrawableRes(ctx, R.drawable.location_direction)!!)
        mapStyle.addImage("accuracyImg", accuracyImg)

        directionSymbol = symbolManager.create(
            SymbolOptions()
                .withIconImage("directionImg")
                .withLatLng(LatLng(0.0, 0.0))
        )
        locationSymbol = symbolManager.create(
            SymbolOptions()
                .withIconImage("dotImg")
                .withLatLng(LatLng(0.0, 0.0))
        )
        accuracySymbol = symbolManager.create(
            SymbolOptions()
                .withIconImage("accuracyImg")
                .withLatLng(LatLng(0.0, 0.0))
        )
    }

    private fun hide() {
        symbolManager.setFilter(Expression.literal(false))
    }

    private fun show() {
        symbolManager.setFilter(Expression.literal(true)) // easier way?
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

        locationSymbol.latLng = LatLng(pos.latitude, pos.longitude)
        directionSymbol.latLng = locationSymbol.latLng
        accuracySymbol.latLng = locationSymbol.latLng
        symbolManager.update(directionSymbol)
        symbolManager.update(locationSymbol)
        // todo: sometimes crashing with: The LocationComponent has to be activated with one of the LocationComponent#activateLocationComponent overloads before any other methods are invoked.
        //  i guess that happens when the map isn't fully initialized?
//        try {
//            MainMapFragment.mapboxMap?.locationComponent?.forceLocationUpdate(location)
//        } catch (_: Exception) {}

        updateAccuracy()
    }

    /** Update the circle that shows the GPS accuracy on the map */
    private fun updateAccuracy() {
        if (!isVisible) return
        val location = location ?: return

        // todo: size is constant, does not change when zooming
        //  and what is the unit? not pixels it seems, but also not meters at current zoom
        val size = location.accuracy * pixelsPerMeter(location.latitude, ctrl.cameraPosition.zoom.toFloat())
        accuracySymbol.iconSize = size.toFloat() / 100
    }

    /** Update the marker that shows the direction in which the smartphone is held */
    private fun updateDirection() {
        if (!isVisible) return
        // no sense to display direction if there is no location yet
        if (rotation == null || location == null) return

        directionSymbol.iconRotate = rotation!!.toFloat() // todo: does nothing?
    }

    private fun pixelsPerMeter(latitude: Double, zoom: Float): Double {
        val numberOfTiles = (2.0).pow(zoom.toDouble())
        val metersPerTile = cos(latitude * PI / 180.0) * EARTH_CIRCUMFERENCE / numberOfTiles
        return 256 / metersPerTile
    }
}
