package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.logs.LogsDao
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloader
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsController
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsDao
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class CleanerTest {
    private val notes: NoteController = mock()
    private val mapData: MapDataController = mock()
    private val db: Database = mock()
    private val tiles = DownloadedTilesController(DownloadedTilesDao(db))
    private val logs = LogsController(LogsDao(db))
    private val mapTiles: MapTilesDownloader = mock()
    private val events = CalendarEventsController(CalendarEventsDao(db))
    private val cleaner = Cleaner(notes, mapData, QuestTypeRegistry(listOf(0 to TestQuestTypeA())), tiles, logs, mapTiles, events)

    @Test fun `cancellation stops cleanup between batches`() = runBlocking {
        val batchStarted = CompletableDeferred<Unit>()
        val finishBatch = CompletableDeferred<Unit>()
        var batches = 0
        every { notes.deleteOlderThan(any(), any()) } calls {
            batchStarted.complete(Unit)
            runBlocking { finishBatch.await() }
            if (batches++ == 0) 100_000 else 0
        }

        val job = cleaner.cleanOld()
        batchStarted.await()
        job.cancel()
        finishBatch.complete(Unit)
        job.join()

        verify(exactly(1)) { notes.deleteOlderThan(any(), any()) }
        verifyNoMoreCalls(mapData, mapTiles, db)
    }
}
