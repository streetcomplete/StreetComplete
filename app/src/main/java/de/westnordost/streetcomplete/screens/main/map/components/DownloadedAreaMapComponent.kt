package de.westnordost.streetcomplete.screens.main.map.components

import com.mapbox.geojson.Polygon
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController

class DownloadedAreaMapComponent(private val ctrl: KtMapController) {

    fun set(tiles: Collection<TilePos>) = synchronized(this) {
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

        val feature = Polygon.fromLngLats(polygons.map { polygon -> polygon.map { it.toPoint() } })
        MainActivity.activity?.runOnUiThread { MainMapFragment.downloadedAreaSource?.setGeoJson(feature) }
    }
}
