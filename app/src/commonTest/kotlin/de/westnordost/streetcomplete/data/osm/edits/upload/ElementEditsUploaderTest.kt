package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClient
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.user.statistics.StatisticsController
import dev.mokkery.matcher.any
import de.westnordost.streetcomplete.testutils.edit
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.node
import dev.mokkery.answering.repeat
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import kotlin.test.BeforeTest
import kotlin.test.Test

class ElementEditsUploaderTest {

    private lateinit var elementEditsController: ElementEditsController
    private lateinit var mapDataController: MapDataController
    private lateinit var noteEditsController: NoteEditsController
    private lateinit var singleUploader: ElementEditUploader
    private lateinit var mapDataApi: MapDataApiClient
    private lateinit var statisticsController: StatisticsController

    private lateinit var uploader: ElementEditsUploader
    private lateinit var listener: OnUploadedChangeListener

    @BeforeTest fun setUp() {
        elementEditsController = mock()
        mapDataController = mock()
        noteEditsController = mock()

        singleUploader = mock()
        mapDataApi = mock()
        statisticsController = mock()

        listener = mock()

        uploader = ElementEditsUploader(elementEditsController, noteEditsController, mapDataController, singleUploader, mapDataApi, statisticsController)
        uploader.uploadedChangeListener = listener
    }

    @Test fun `cancel upload works`() = runBlocking {
        val job = launch { uploader.upload() }
        job.cancelAndJoin()
        verifyNoMoreCalls(elementEditsController, mapDataController, singleUploader, statisticsController)
    }

    @Test fun `upload works`() = runBlocking {
        val edit = edit()
        val updates = MapDataUpdates()

        every { elementEditsController.getOldestUnsynced() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }
        everySuspend { singleUploader.upload(any(), any()) } returns updates

        uploader.upload()

        verifySuspend { singleUploader.upload(edit, any()) }
        verify { listener.onUploaded(any(), any()) }
        verify { elementEditsController.markSynced(edit, updates) }
        verify { noteEditsController.updateElementIds(any()) }
        verify { mapDataController.updateAll(updates) }

        verify { statisticsController.addOne(any(), any()) }
    }

    @Test fun `upload catches conflict exception`() = runBlocking {
        // edit modifies node 1 and way 1
        val node1 = node()
        val action: ElementEditAction = mock()
        every { action.elementKeys } returns listOf(
            ElementKey(ElementType.NODE, 1),
            ElementKey(ElementType.WAY, 1),
        )
        val edit = edit(action = action)

        // ...but way 1 is gone
        everySuspend { mapDataApi.getNode(1) } returns node1
        everySuspend { mapDataApi.getWayComplete(1) } returns null

        // the edit is the first in the upload queue and the uploader throws a conflict exception
        every { elementEditsController.getOldestUnsynced() } sequentially {
            returns(edit)
            repeat { returns(null) }
        }
        everySuspend { singleUploader.upload(any(), any()) } throws ConflictException()

        uploader.upload()

        verifySuspend { singleUploader.upload(edit, any()) }
        verify { listener.onDiscarded(any(), any()) }

        verify { elementEditsController.markSyncFailed(edit) }
        verifyNoMoreCalls(statisticsController)

        verify {
            mapDataController.updateAll(MapDataUpdates(
                updated = listOf(node1),
                deleted = listOf(ElementKey(ElementType.WAY, 1))
            ))
        }
    }
}
