package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.downloaded_area_hatching
import de.westnordost.streetcomplete.screens.main.map.toPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.toJson

/** Displays hatching everywhere outside the downloaded tiles. */
@Composable
@MaplibreComposable
fun DownloadedAreaLayer(mapState: MapState, tiles: Collection<TilePos>) {
    val data by produceState<GeoJsonData>(EMPTY_DOWNLOADED_AREA_DATA, tiles) {
        value = withContext(Dispatchers.Default) {
            GeoJsonData.JsonString(tiles.toHolesInWorldPolygon().toJson())
        }
    }
    val source = rememberImperativeGeoJsonSource(
        mapState = mapState,
        id = DOWNLOADED_AREA_SOURCE_ID,
        data = data,
    )

    FillLayer(
        id = "downloaded-area",
        source = source,
        opacity = const(0.6f),
        pattern = image(painterResource(Res.drawable.downloaded_area_hatching)),
    )
}

private val EMPTY_DOWNLOADED_AREA_DATA = GeoJsonData.JsonString(
    emptyList<TilePos>().toHolesInWorldPolygon().toJson()
)

private const val DOWNLOADED_AREA_SOURCE_ID = "downloaded-area-source"

/** convert the given tile positions into a polygon that spans the whole world but has holes at
 *  where the tiles are at. */
internal fun Collection<TilePos>.toHolesInWorldPolygon(): Polygon {
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
