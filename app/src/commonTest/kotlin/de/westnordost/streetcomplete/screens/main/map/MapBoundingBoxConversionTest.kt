package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.BoundingBox as MapLibreBoundingBox
import kotlin.test.Test
import kotlin.test.assertEquals

class MapBoundingBoxConversionTest {

    @Test fun convertsMapLibreSouthwestAndNortheastWithoutAxisSwap() {
        val mapLibre = MapLibreBoundingBox(
            southwest = Position(longitude = -122.5, latitude = 37.5),
            northeast = Position(longitude = -122.0, latitude = 38.0),
        )

        assertEquals(
            BoundingBox(37.5, -122.5, 38.0, -122.0),
            mapLibre.toStreetCompleteBoundingBox(),
        )
    }
}
