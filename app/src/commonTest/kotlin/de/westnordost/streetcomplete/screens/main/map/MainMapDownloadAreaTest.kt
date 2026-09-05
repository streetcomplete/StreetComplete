package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.area
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MainMapDownloadAreaTest {
    @Test fun unavailableProjectionIsReported() {
        assertEquals(
            MainMapDownloadArea.DisplayAreaUnavailable,
            calculateMainMapDownloadArea(null, LatLon(0.0, 0.0)),
        )
    }

    @Test fun oversizedTileAlignedAreaIsRejected() {
        val result = calculateMainMapDownloadArea(
            BoundingBox(0.0, 0.0, 1.0, 1.0),
            LatLon(0.5, 0.5),
        )
        assertEquals(MainMapDownloadArea.TooLarge, result)
    }

    @Test fun tinyViewportExpandsAroundCameraToMinimumArea() {
        val center = LatLon(50.0, 10.0)
        val result = assertIs<MainMapDownloadArea.Available>(
            calculateMainMapDownloadArea(BoundingBox(center, center), center)
        )
        val squareKilometers = result.bounds.area() / 1_000_000
        assertTrue(squareKilometers >= ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM * 0.99)
        assertTrue(center.latitude in result.bounds.min.latitude..result.bounds.max.latitude)
        assertTrue(center.longitude in result.bounds.min.longitude..result.bounds.max.longitude)
    }

    @Test fun ordinaryViewportIsAlignedToDownloadTiles() {
        val viewport = BoundingBox(52.50, 13.35, 52.51, 13.36)
        val result = assertIs<MainMapDownloadArea.Available>(
            calculateMainMapDownloadArea(viewport, LatLon(52.505, 13.355))
        )
        assertTrue(result.bounds.min.latitude <= viewport.min.latitude)
        assertTrue(result.bounds.min.longitude <= viewport.min.longitude)
        assertTrue(result.bounds.max.latitude >= viewport.max.latitude)
        assertTrue(result.bounds.max.longitude >= viewport.max.longitude)
    }
}
