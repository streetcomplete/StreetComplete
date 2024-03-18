package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import androidx.annotation.UiThread
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint

class DownloadedAreaMapComponent(private val context: Context, private val map: MapboxMap) {

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
