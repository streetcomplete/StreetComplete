package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals

class DownloadedAreaLayerTest {

    @Test fun emptyTilesMaskTheWholeWorld() {
        val mask = emptyList<TilePos>().toDownloadedAreaMask()

        assertEquals(
            listOf(
                Position(-180.0, +90.0),
                Position(-180.0, -90.0),
                Position(+180.0, -90.0),
                Position(+180.0, +90.0),
                Position(-180.0, +90.0),
            ),
            mask.coordinates.single()
        )
    }

    @Test fun eachDownloadedTileBecomesAClockwiseHole() {
        val tile = TilePos(32768, 32768)
        val bounds = tile.asBoundingBox(ApplicationConstants.DOWNLOAD_TILE_ZOOM)

        val mask = listOf(tile).toDownloadedAreaMask()

        assertEquals(2, mask.coordinates.size)
        assertEquals(
            listOf(
                Position(bounds.min.longitude, bounds.min.latitude),
                Position(bounds.min.longitude, bounds.max.latitude),
                Position(bounds.max.longitude, bounds.max.latitude),
                Position(bounds.max.longitude, bounds.min.latitude),
                Position(bounds.min.longitude, bounds.min.latitude),
            ),
            mask.coordinates[1]
        )
    }
}
