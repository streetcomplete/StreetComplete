package de.westnordost.streetcomplete.data.quest

import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
import de.westnordost.streetcomplete.data.connection.NetworkCapabilities
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.data.download.strategy.MobileDataAutoDownloadStrategy
import de.westnordost.streetcomplete.data.download.strategy.WifiAutoDownloadStrategy
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilterSource
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import kotlin.test.Test
import kotlin.test.assertEquals

class AutoSyncerTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun resumesLocationCollectionAfterPermissionFailureCompletesFlow() = runTest {
        var collections = 0
        val locationProvider = object : LocationProvider {
            override fun updates(request: LocationRequest): Flow<LocationEvent> = flow {
                collections++
                emit(LocationEvent.Unavailable(LocationUnavailableReason.PermissionDenied))
            }
        }
        val noNetwork = object : ActiveNetworkConnection {
            override val capabilitiesFlow = emptyFlow<NetworkCapabilities?>()
            override val capabilities: NetworkCapabilities? = null
        }
        val downloadedTilesSource = mock<DownloadedTilesSource>()
        val mapDataSource = mock<MapDataSource>()
        val noteEditsSource: NoteEditsSource = mock {
            every { addListener(any()) } calls { (_: NoteEditsSource.Listener) -> }
        }
        val elementEditsSource: ElementEditsSource = mock {
            every { addListener(any()) } calls { (_: ElementEditsSource.Listener) -> }
        }
        val applicationScope = CoroutineScope(StandardTestDispatcher(testScheduler) + SupervisorJob())
        val autoSyncer = AutoSyncer(
            mock<DownloadController>(),
            mock<UploadController>(),
            MobileDataAutoDownloadStrategy(mapDataSource, downloadedTilesSource),
            WifiAutoDownloadStrategy(mapDataSource, downloadedTilesSource),
            locationProvider,
            noNetwork,
            UnsyncedChangesCountSource(noteEditsSource, elementEditsSource),
            mock<DownloadProgressSource>(),
            mock<UserLoginSource>(),
            mock<Preferences>(),
            mock<TeamModeQuestFilterSource>(),
            DownloadedTilesController(mock<DownloadedTilesDao>()),
            applicationScope,
        )
        val owner = mock<LifecycleOwner>()

        autoSyncer.onStart(owner)
        advanceUntilIdle()
        assertEquals(1, collections)

        autoSyncer.onResume(owner)
        advanceUntilIdle()
        assertEquals(2, collections)

        autoSyncer.onStop(owner)
        applicationScope.cancel()
    }
}
