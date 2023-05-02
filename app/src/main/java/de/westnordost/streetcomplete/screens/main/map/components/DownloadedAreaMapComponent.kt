package de.westnordost.streetcomplete.screens.main.map.components

import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Polygon
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toLngLat

class DownloadedAreaMapComponent(private val ctrl: KtMapController) {
    private var layer: MapData? = null

    fun set(tiles: Collection<TilePos>) {
        // tangram does not clear a layer properly on re-setting features on it, so let's remove
        // and re-add the whole layer
        layer?.remove()
        val layer = ctrl.addDataLayer(DOWNLOADED_AREA_LAYER)

        val zoom = ApplicationConstants.DOWNLOAD_TILE_ZOOM
        val world = listOf(
            LatLon(+90.0, -180.0),
            LatLon(-90.0, -180.0),
            LatLon(-90.0, +180.0),
            LatLon(+90.0, +180.0),
            LatLon(+90.0, -180.0),
        )
        val holes = tiles.map { it.asBoundingBox(zoom).toPolygon().asReversed() }
        val polygons = listOf(world) + holes

        layer.setFeatures(listOf(
            Polygon(
                polygons.map { polygon -> polygon.map { it.toLngLat() }},
                mapOf()
            )
        ))
        this.layer = layer
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the below layers
        private const val DOWNLOADED_AREA_LAYER = "streetcomplete_downloaded_area"
    }
}
