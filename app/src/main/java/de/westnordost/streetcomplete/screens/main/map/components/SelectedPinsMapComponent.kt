package de.westnordost.streetcomplete.screens.main.map.components

import android.animation.ValueAnimator
import android.content.Context
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.gson.JsonObject
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.createPinBitmap
import de.westnordost.streetcomplete.screens.main.map.maplibre.MapImages
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

/** Takes care of displaying "selected" pins. Those pins are always shown on top of pins displayed
 *  by the [PinsMapComponent] */
class SelectedPinsMapComponent(
    private val context: Context,
    private val map: MapLibreMap,
    private val mapImages: MapImages
) : DefaultLifecycleObserver {

    private val selectedPinsSource = GeoJsonSource("selected-pins-source")
    private val animation: ValueAnimator

    val layers: List<Layer> = listOf(
        SymbolLayer("selected-pins-layer", "selected-pins-source")
            .withProperties(
                iconImage(get("icon-image")),
                iconSize(0.5f),
                iconOffset(listOf(-4.5f, -34.5f).toTypedArray()),
            )
    )

    init {
        selectedPinsSource.isVolatile = true
        map.style?.addSource(selectedPinsSource)
        animation = ValueAnimator.ofFloat(0.5f, 1.5f)
        animation.duration = 300
        animation.interpolator = OvershootInterpolator()
        animation.addUpdateListener { animatePin(it.animatedValue as Float) }
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

    /** Show selected pins with the given icon at the given positions. "Selected pins" are not
     *  related to pins, they are just visuals that are displayed on top of the normal pins and look
     *  highlighted/selected. */
    suspend fun set(@DrawableRes iconResId: Int, pinPositions: Collection<LatLon>) {
        mapImages.addOnce(iconResId) { createPinBitmap(context, it) to false }
        val p = JsonObject()
        p.addProperty("icon-image", context.resources.getResourceEntryName(iconResId))
        val points = pinPositions.map { Feature.fromGeometry(it.toPoint(), p) }
        withContext(Dispatchers.Main) {
            selectedPinsSource.setGeoJson(FeatureCollection.fromFeatures(points))
            animation.start()
        }
    }

    private fun animatePin(value: Float) {
        map.style?.getLayerAs<SymbolLayer>("selected-pins-layer")?.setProperties(
            iconSize(value),
        )
    }

    /** Clear the display of any selected pins */
    @UiThread fun clear() {
        selectedPinsSource.clear()
    }
}
