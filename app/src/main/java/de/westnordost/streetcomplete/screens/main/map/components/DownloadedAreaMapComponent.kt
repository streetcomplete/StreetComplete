package de.westnordost.streetcomplete.screens.main.map.components

import com.mapzen.tangram.LngLat
import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Polygon
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController

class DownloadedAreaMapComponent(private val ctrl: KtMapController) {
    private val layer: MapData = ctrl.addDataLayer(DOWNLOADED_AREA_LAYER)

    fun set() {
        layer.setFeatures(listOf(
            Polygon(listOf(
                // whole world
                listOf(
                    LngLat(-180.0, 90.0),
                    LngLat(-180.0, -90.0),
                    LngLat(180.0, -90.0),
                    LngLat(180.0, 90.0),
                    LngLat(-180.0, 90.0)
                ),
                // a hole = downloaded area...
                listOf(
                    LngLat(-1.0, 1.0),
                    LngLat(-1.0, -1.0),
                    LngLat(1.0, -1.0),
                    LngLat(1.0, 1.0),
                    LngLat(-1.0, 1.0),
                ).reversed()
            ), mapOf())
        ))
    }

    fun clear() {
        layer.clear()
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the below layers
        private const val DOWNLOADED_AREA_LAYER = "streetcomplete_downloaded_area"
    }
}
