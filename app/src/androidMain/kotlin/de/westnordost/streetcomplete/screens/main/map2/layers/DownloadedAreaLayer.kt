package de.westnordost.streetcomplete.screens.main.map2.layers

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.downloaded_area_hatching
import de.westnordost.streetcomplete.screens.main.map2.toPosition
import dev.sargunv.maplibrecompose.compose.MaplibreComposable
import dev.sargunv.maplibrecompose.compose.layer.FillLayer
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.source.GeoJsonData
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.image
import io.github.dellisd.spatialk.geojson.Polygon
import org.jetbrains.compose.resources.painterResource

/** Displays which areas have (not) been downloaded */
@Composable @MaplibreComposable
fun DownloadedAreaLayer(tiles: Collection<TilePos>) {
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(tiles.toHolesInWorldPolygon())
    )

    FillLayer(
        id = "downloaded-area",
        source = source,
        opacity = const(0.6f),
        pattern = image(painterResource(Res.drawable.downloaded_area_hatching)),
    )
}

/** convert the given tile positions into a polygon that spans the whole world but has holes at
 *  where the tiles are at. */
private fun Collection<TilePos>.toHolesInWorldPolygon(): Polygon {
    val zoom = ApplicationConstants.DOWNLOAD_TILE_ZOOM
    val world = listOf(
        LatLon(+90.0, -180.0),
        LatLon(-90.0, -180.0),
        LatLon(-90.0, +180.0),
        LatLon(+90.0, +180.0),
        LatLon(+90.0, -180.0),
    )
    val holes = this.map { it.asBoundingBox(zoom).toPolygon().asReversed() }
    val polygons = listOf(world) + holes
    return Polygon(polygons.map { polygon -> polygon.map { it.toPosition() } })
}
