package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DownloadedTilesStateSourceTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun loadsRetainedTilesAndRefreshesOnUpdates() = runTest {
        var retained = listOf(TilePos(1, 2))
        var requestedCutoff = 0L
        lateinit var listener: DownloadedTilesSource.Listener
        val downloaded: DownloadedTilesSource = mock {
            every { getAll(any()) } calls { (cutoff: Long) ->
                requestedCutoff = cutoff
                retained
            }
            every { addListener(any()) } calls { (value: DownloadedTilesSource.Listener) ->
                listener = value
            }
        }
        val source = DownloadedTilesStateSource(
            downloaded,
            workerDispatcher = StandardTestDispatcher(testScheduler),
        )

        advanceUntilIdle()
        assertEquals(ApplicationConstants.DELETE_OLD_DATA_AFTER, requestedCutoff)
        assertEquals(retained, source.tiles.value)

        retained = listOf(TilePos(3, 4), TilePos(5, 6))
        listener.onUpdated()
        advanceUntilIdle()
        assertEquals(retained, source.tiles.value)
        source.close()
    }
}
