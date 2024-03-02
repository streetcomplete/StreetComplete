package de.westnordost.streetcomplete.screens.main.map.components

import androidx.annotation.UiThread
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint

class DownloadedAreaMapComponent(private val map: MapboxMap) {

    private val downloadedAreaSource = GeoJsonSource("downloaded-area-source")

    init {
        map.style?.addSource(downloadedAreaSource)
    }

    @UiThread fun set(tiles: Collection<TilePos>) {
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
        downloadedAreaSource.setGeoJson(feature)
    }
}
