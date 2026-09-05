package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainMapCameraControllerTest {
    @Test fun firstFollowUpdateCentersAndZoomsLikeLegacyMap() = runTest {
        val camera = FakeCamera(CameraPosition(zoom = 12.0))
        val controller = controller(camera = camera, scope = this)
        val first = location(1.0, 2.0)

        controller.onLocationChanged(first, null)
        advanceUntilIdle()

        assertEquals(1.0, camera.position.target.latitude)
        assertEquals(2.0, camera.position.target.longitude)
        assertEquals(18.0, camera.position.zoom)
        assertEquals(600.milliseconds, camera.animations.single().duration)

        controller.onLocationChanged(location(3.0, 4.0), null)
        advanceUntilIdle()
        assertEquals(18.0, camera.position.zoom)
        assertEquals(2, camera.animations.size)
    }

    @Test fun navigationUsesTrackBearingAndResetsOnlyTiltWhenDisabled() = runTest {
        val camera = FakeCamera(CameraPosition(bearing = 25.0, zoom = 18.0))
        val controller = controller(camera = camera, scope = this)
        controller.onLocationChanged(
            location(1.001, 1.0),
            navigationBearing = 0.0,
        )
        advanceUntilIdle()

        controller.updateNavigationMode(true)
        advanceUntilIdle()
        assertEquals(0.0, camera.position.bearing, absoluteTolerance = 0.01)
        assertEquals(60.0, camera.position.tilt)
        assertEquals(600.milliseconds, camera.animations.last().duration)

        controller.updateNavigationMode(false)
        advanceUntilIdle()
        assertEquals(0.0, camera.position.bearing, absoluteTolerance = 0.01)
        assertEquals(0.0, camera.position.tilt)
        assertEquals(300.milliseconds, camera.animations.last().duration)
    }

    @Test fun gestureTargetMovementStopsFollowingButRotationDoesNot() = runTest {
        val camera = FakeCamera(CameraPosition(target = Position(2.0, 1.0), zoom = 18.0))
        val controller = controller(camera = camera, scope = this)
        controller.onLocationChanged(location(1.0, 2.0), null)
        advanceUntilIdle()

        controller.onCameraChanged(
            camera.position.copy(bearing = 15.0),
            CameraMoveReason.GESTURE,
            isMoving = true,
        )
        assertTrue(controller.isFollowingPosition)
        assertTrue(controller.userHasMovedCamera)

        controller.onCameraChanged(
            camera.position.copy(target = Position(2.001, 1.0)),
            CameraMoveReason.GESTURE,
            isMoving = true,
        )
        assertFalse(controller.isFollowingPosition)
    }

    @Test fun compassEndsNavigationAndResetsBearingAndTiltTogether() = runTest {
        val camera = FakeCamera(CameraPosition(bearing = 75.0, tilt = 60.0, zoom = 18.0))
        val persisted = FakePersistedState(isNavigationMode = true)
        val controller = controller(camera, persisted, this)

        controller.resetCompass()
        advanceUntilIdle()

        assertFalse(controller.isNavigationMode)
        assertEquals(0.0, camera.position.bearing)
        assertEquals(0.0, camera.position.tilt)
        assertEquals(300.milliseconds, camera.animations.single().duration)
    }

    @Test fun clusterClickAnimatesTheComputedCameraForAtLeastLegacyMinimum() = runTest {
        val camera = FakeCamera(CameraPosition(zoom = 10.0))
        camera.visibleBoundingBox = BoundingBox(Position(-1.0, -1.0), Position(1.0, 1.0))
        val controller = controller(camera = camera, scope = this)

        controller.fitCluster(listOf(LatLon(-0.5, -0.5), LatLon(0.5, 0.5)))
        advanceUntilIdle()

        assertEquals(10.75, camera.position.zoom, absoluteTolerance = 0.001)
        assertEquals(450.milliseconds, camera.animations.single().duration)
    }

    @Test fun explicitMoveUpdatesTargetZoomPaddingAndDuration() = runTest {
        val camera = FakeCamera(CameraPosition(zoom = 12.0))
        val controller = controller(camera = camera, scope = this)
        val padding = PaddingValues(start = 40.dp, bottom = 80.dp)

        controller.moveTo(LatLon(3.0, 4.0), 17.5, padding, 450.milliseconds)
        advanceUntilIdle()

        assertEquals(Position(4.0, 3.0), camera.position.target)
        assertEquals(17.5, camera.position.zoom)
        assertEquals(padding, controller.cameraPadding)
        assertEquals(450.milliseconds, camera.animations.single().duration)
    }

    @Test fun explicitMoveBeforePresentationRunsWhenMapAttaches() = runTest {
        val camera = FakeCamera(CameraPosition(zoom = 12.0)).apply { isPresented = false }
        val controller = controller(camera = camera, scope = this)

        controller.moveTo(
            LatLon(3.0, 4.0),
            zoom = 17.5,
            padding = PaddingValues(0.dp),
            duration = 450.milliseconds,
        )
        advanceUntilIdle()
        assertTrue(camera.animations.isEmpty())

        camera.isPresented = true
        controller.onMapPresented()
        advanceUntilIdle()

        assertEquals(Position(4.0, 3.0), camera.position.target)
        assertEquals(17.5, camera.position.zoom)
        assertEquals(450.milliseconds, camera.animations.single().duration)
    }

    @Test fun settledCameraAndInteractionModeArePersisted() = runTest {
        val camera = FakeCamera(CameraPosition())
        val persisted = FakePersistedState()
        val controller = controller(camera, persisted, this)
        controller.updateFollowingPosition(false)
        controller.updateNavigationMode(true)
        val settled = CameraPosition(
            bearing = 40.0,
            target = Position(5.0, 6.0),
            tilt = 20.0,
            zoom = 17.0,
        )

        controller.onCameraChanged(settled, CameraMoveReason.PROGRAMMATIC, isMoving = false)

        assertEquals(PersistedMapCameraState(settled, false, true), persisted.saved.last())
    }

    private fun controller(
        camera: FakeCamera = FakeCamera(CameraPosition()),
        persisted: FakePersistedState = FakePersistedState(),
        scope: kotlinx.coroutines.CoroutineScope,
    ) = MainMapCameraController(camera, persisted, scope)

    private fun location(latitude: Double, longitude: Double) = Location(
        position = LatLon(latitude, longitude),
        accuracy = 3f,
        measuredAt = Instant.fromEpochMilliseconds(0),
    )
}

internal class FakeCamera(initialPosition: CameraPosition) : MainMapCamera {
    override var position: CameraPosition = initialPosition
    override var visibleBoundingBox: BoundingBox? = BoundingBox(
        southwest = Position(-1.0, -1.0),
        northeast = Position(1.0, 1.0),
    )
    override var viewportSize: DpSize? = DpSize(400.dp, 800.dp)
    override var isPresented: Boolean = true
    val animations = mutableListOf<Animation>()

    override suspend fun animateTo(position: CameraPosition, duration: Duration) {
        animations += Animation(position, duration)
        this.position = position
    }
}

internal data class Animation(val position: CameraPosition, val duration: Duration)

internal class FakePersistedState(
    private val camera: CameraPosition = CameraPosition(),
    private val isFollowingPosition: Boolean = true,
    private val isNavigationMode: Boolean = false,
) : MainMapPersistedState {
    val saved = mutableListOf<PersistedMapCameraState>()
    override fun loadCamera() = camera
    override fun loadIsFollowingPosition() = isFollowingPosition
    override fun loadIsNavigationMode() = isNavigationMode
    override fun save(state: PersistedMapCameraState) { saved += state }
}
