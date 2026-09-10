package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClusterCameraTest {
    @Test fun emptyClusterHasNoCameraPlan() {
        assertNull(clusterCameraPosition(CameraPosition(), WORLD, emptyList()))
    }

    @Test fun pointClusterCentersAtLegacyMaximumZoom() {
        val target = clusterCameraPosition(
            current = CameraPosition(bearing = 20.0, tilt = 30.0, zoom = 10.0),
            visibleBoundingBox = WORLD,
            positions = listOf(LatLon(37.5, -122.5), LatLon(37.5, -122.5)),
        )!!

        assertEquals(37.5, target.target.latitude, absoluteTolerance = 1e-9)
        assertEquals(-122.5, target.target.longitude, absoluteTolerance = 1e-9)
        assertEquals(19.0, target.zoom)
        assertEquals(20.0, target.bearing)
        assertEquals(30.0, target.tilt)
    }

    @Test fun spreadClusterLeavesQuarterZoomLevelOfBreathingRoom() {
        val current = CameraPosition(zoom = 10.0)
        val visible = BoundingBox(Position(-1.0, -1.0), Position(1.0, 1.0))
        val target = clusterCameraPosition(
            current,
            visible,
            listOf(LatLon(-0.5, -0.5), LatLon(0.5, 0.5)),
        )!!

        assertEquals(10.75, target.zoom, absoluteTolerance = 0.001)
        assertEquals(0.0, target.target.latitude, absoluteTolerance = 0.001)
        assertEquals(0.0, target.target.longitude, absoluteTolerance = 0.001)
    }

    @Test fun clusterCenterTakesShortPathAcrossAntimeridian() {
        val target = clusterCameraPosition(
            current = CameraPosition(zoom = 4.0),
            visibleBoundingBox = BoundingBox(Position(170.0, -10.0), Position(-170.0, 10.0)),
            positions = listOf(LatLon(0.0, 179.0), LatLon(0.0, -179.0)),
        )!!

        assertTrue(target.target.longitude == -180.0 || target.target.longitude == 180.0)
        assertTrue(target.zoom > 4.0)
    }

    private companion object {
        val WORLD = BoundingBox(Position(-180.0, -85.0), Position(180.0, 85.0))
    }
}
