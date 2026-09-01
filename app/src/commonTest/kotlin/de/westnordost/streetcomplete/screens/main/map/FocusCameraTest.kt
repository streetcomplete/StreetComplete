package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FocusCameraTest {
    @Test fun pointFocusUsesPaddingZoomCapAndLegacyMinimumDuration() = runTest {
        val camera = FakeCamera(CameraPosition(bearing = 20.0, tilt = 10.0, zoom = 12.0))
        val controller = MainMapCameraController(camera, FakePersistedState(), this)

        controller.startFocus(
            ElementPointGeometry(LatLon(1.0, 2.0)),
            PaddingValues(bottom = 320.dp),
        )
        advanceUntilIdle()

        assertEquals(1.0, camera.position.target.latitude, absoluteTolerance = 1e-9)
        assertEquals(2.0, camera.position.target.longitude, absoluteTolerance = 1e-9)
        assertEquals(19.0, camera.position.zoom)
        assertEquals(20.0, camera.position.bearing)
        assertEquals(10.0, camera.position.tilt)
        assertEquals(3150.milliseconds, camera.animations.single().duration)
        assertEquals(320.dp, controller.cameraPadding.calculateBottomPadding())
    }

    @Test fun smallZoomDifferenceMovesCenterWithoutChangingZoom() = runTest {
        val camera = FakeCamera(CameraPosition(zoom = 10.0))
        val controller = MainMapCameraController(camera, FakePersistedState(), this)
        val geometry = ElementPolylinesGeometry(
            polylines = listOf(listOf(LatLon(-0.62, -0.62), LatLon(0.62, 0.62))),
            center = LatLon(0.0, 0.0),
        )

        controller.startFocus(geometry, PaddingValues(0.dp))
        advanceUntilIdle()

        assertEquals(10.0, camera.position.zoom)
        assertEquals(450.milliseconds, camera.animations.single().duration)
    }

    @Test fun endFocusReturnsPositionAndZoomButRetainsCurrentOrientation() = runTest {
        val initial = CameraPosition(
            bearing = 15.0,
            target = Position(5.0, 6.0),
            tilt = 20.0,
            zoom = 12.0,
        )
        val camera = FakeCamera(initial)
        val controller = MainMapCameraController(camera, FakePersistedState(), this)
        controller.startFocus(ElementPointGeometry(LatLon(1.0, 2.0)), PaddingValues(100.dp))
        advanceUntilIdle()
        camera.position = camera.position.copy(bearing = 80.0, tilt = 40.0)

        controller.endFocus()
        advanceUntilIdle()

        assertEquals(initial.target, camera.position.target)
        assertEquals(initial.zoom, camera.position.zoom)
        assertEquals(80.0, camera.position.bearing)
        assertEquals(40.0, camera.position.tilt)
        assertEquals(2100.milliseconds, camera.animations.last().duration)
        assertEquals(0.dp, controller.cameraPadding.calculateBottomPadding())
    }

    @Test fun clearFocusDiscardsReturnCamera() = runTest {
        val camera = FakeCamera(CameraPosition(target = Position(5.0, 6.0), zoom = 12.0))
        val controller = MainMapCameraController(camera, FakePersistedState(), this)
        controller.startFocus(ElementPointGeometry(LatLon(1.0, 2.0)), PaddingValues(100.dp))
        advanceUntilIdle()

        controller.clearFocus()
        controller.endFocus()
        advanceUntilIdle()

        assertEquals(1, camera.animations.size)
        assertEquals(0.dp, controller.cameraPadding.calculateBottomPadding())
    }
}
