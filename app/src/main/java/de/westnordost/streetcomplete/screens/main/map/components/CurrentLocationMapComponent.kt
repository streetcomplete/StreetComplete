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
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapzen.tangram.MapController
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.Marker
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.util.ktx.isApril1st
import de.westnordost.streetcomplete.util.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.math.EARTH_CIRCUMFERENCE
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

/** Takes care of showing the location + direction + accuracy marker on the map */
class CurrentLocationMapComponent(ctx: Context, mapStyle: Style, private val symbolManager: SymbolManager, private val ctrl: KtMapController) {
    // markers showing the user's location, direction and accuracy of location
    private val locationMarker: Marker
    private val locationSymbol: Symbol
    private val accuracyMarker: Marker
    private val directionMarker: Marker
    private val directionSymbol: Symbol

    private val directionMarkerSize: PointF

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
        val dotImg = ctx.resources.getBitmapDrawable(if (isApril1st()) R.drawable.location_nyan else R.drawable.location_dot)
        val dotSize = PointF(
            ctx.pxToDp(dotImg.bitmap.width),
            ctx.pxToDp(dotImg.bitmap.height)
        )

        val directionImg = ctx.resources.getBitmapDrawable(R.drawable.location_direction)
        directionMarkerSize = PointF(
            ctx.pxToDp(directionImg.bitmap.width),
            ctx.pxToDp(directionImg.bitmap.height)
        )

        val accuracyImg = ctx.resources.getBitmapDrawable(R.drawable.accuracy_circle)

        locationMarker = ctrl.addMarker().also {
            it.setStylingFromString("""
            {
                style: 'points',
                color: 'white',
                size: [${dotSize.x}px, ${dotSize.y}px],
                order: 2000,
                flat: true,
                collide: false,
                interactive: true
            }
            """.trimIndent())
            it.setDrawable(dotImg)
            it.setDrawOrder(3)
        }
        symbolManager.iconAllowOverlap = true
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

        directionMarker = ctrl.addMarker().also {
            it.setDrawable(directionImg)
            it.setDrawOrder(2)
        }
        accuracyMarker = ctrl.addMarker().also {
            it.setDrawable(accuracyImg)
            it.setDrawOrder(1)
        }
    }

    private fun hide() {
        locationMarker.isVisible = false
        accuracyMarker.isVisible = false
        directionMarker.isVisible = false
    }

    private fun show() {
        updateLocation()
        updateDirection()
    }
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

        accuracyMarker.isVisible = true
        accuracyMarker.setPointEased(pos, 600, MapController.EaseType.CUBIC)
        locationMarker.isVisible = true
        locationMarker.setPointEased(pos, 600, MapController.EaseType.CUBIC)
        directionMarker.isVisible = rotation != null
        directionMarker.setPointEased(pos, 600, MapController.EaseType.CUBIC)
        locationSymbol.latLng = LatLng(pos.latitude, pos.longitude)
        directionSymbol.latLng = locationSymbol.latLng
        symbolManager.update(directionSymbol)
        symbolManager.update(locationSymbol)

        updateAccuracy()
    }

    /** Update the circle that shows the GPS accuracy on the map */
    private fun updateAccuracy() {
        if (!isVisible) return
        val location = location ?: return

        val size = location.accuracy * pixelsPerMeter(location.latitude, ctrl.cameraPosition.zoom)
        accuracyMarker.setStylingFromString("""
        {
            style: 'points',
            color: 'white',
            size: ${size}px,
            order: 2000,
            flat: true,
            collide: false
        }
        """)
    }

    /** Update the marker that shows the direction in which the smartphone is held */
    private fun updateDirection() {
        if (!isVisible) return
        // no sense to display direction if there is no location yet
        if (rotation == null || location == null) return

        directionMarker.isVisible = true
        directionMarker.setStylingFromString("""
        {
            style: 'points',
            color: '#cc536dfe',
            size: [${directionMarkerSize.x}px, ${directionMarkerSize.y}px],
            order: 2000,
            collide: false,
            flat: true,
            angle: $rotation
        }
        """)
        directionSymbol.iconRotate = rotation!!.toFloat();
    }

    private fun pixelsPerMeter(latitude: Double, zoom: Float): Double {
        val numberOfTiles = (2.0).pow(zoom.toDouble())
        val metersPerTile = cos(latitude * PI / 180.0) * EARTH_CIRCUMFERENCE / numberOfTiles
        return 256 / metersPerTile
    }
}
