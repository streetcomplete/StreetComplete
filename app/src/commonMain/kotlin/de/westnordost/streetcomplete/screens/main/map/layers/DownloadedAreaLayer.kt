package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.downloaded_area_hatching
import de.westnordost.streetcomplete.screens.main.map.toPosition
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Polygon

/** Displays hatching everywhere outside the downloaded tiles. */
@Composable
@MaplibreComposable
fun DownloadedAreaLayer(tiles: Collection<TilePos>) {
    // TODO(maplibre-compose): Restore the legacy source's volatile flag when GeoJsonOptions exposes it.
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(tiles.toDownloadedAreaMask())
    )

    FillLayer(
        id = "downloaded-area",
        source = source,
        opacity = const(0.6f),
        pattern = image(painterResource(Res.drawable.downloaded_area_hatching)),
    )
}

/** Converts tile positions to a world polygon with one transparent hole per downloaded tile. */
internal fun Collection<TilePos>.toDownloadedAreaMask(): Polygon {
    val world = listOf(
        LatLon(+90.0, -180.0),
        LatLon(-90.0, -180.0),
        LatLon(-90.0, +180.0),
        LatLon(+90.0, +180.0),
        LatLon(+90.0, -180.0),
    )
    val holes = map { tile ->
        tile.asBoundingBox(ApplicationConstants.DOWNLOAD_TILE_ZOOM).toPolygon().asReversed()
    }
    return Polygon((listOf(world) + holes).map { ring -> ring.map { it.toPosition() } })
}
