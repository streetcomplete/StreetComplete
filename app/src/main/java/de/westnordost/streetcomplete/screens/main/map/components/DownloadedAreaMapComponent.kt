package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import androidx.core.graphics.drawable.toBitmap
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Polygon

class DownloadedAreaMapComponent(private val context: Context, private val map: MapLibreMap) {

    private val downloadedAreaSource = GeoJsonSource("downloaded-area-source")

    val layers: List<Layer> = listOf(
        FillLayer("downloaded-area", "downloaded-area-source")
            .withProperties(
                fillPattern("downloaded_area_hatching"),
                fillOpacity(0.6f)
            )
    )

    init {
        map.style?.addImage(
            "downloaded_area_hatching",
            context.getDrawable(R.drawable.downloaded_area_hatching)!!.toBitmap()
        )
        downloadedAreaSource.isVolatile = true
        map.style?.addSource(downloadedAreaSource)
    }

    suspend fun set(tiles: Collection<TilePos>) {
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
        withContext(Dispatchers.Main) { downloadedAreaSource.setGeoJson(feature) }
    }
}
