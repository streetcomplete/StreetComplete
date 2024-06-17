package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.user.statistics.StatisticsController
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.eq
import io.mockative.every
import io.mockative.mock
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test

class ElementEditsUploaderTest {

    @Mock
    private lateinit var elementEditsController: ElementEditsController
    @Mock private lateinit var mapDataController: MapDataController
    @Mock private lateinit var noteEditsController: NoteEditsController
    @Mock private lateinit var singleUploader: ElementEditUploader
    @Mock private lateinit var mapDataApi: MapDataApi
    private lateinit var statisticsController: StatisticsController

    // dummy required for mockative to generate mock class
    @Mock private lateinit var action: ElementEditAction

    private lateinit var uploader: ElementEditsUploader
    private lateinit var listener: OnUploadedChangeListener

    @BeforeTest fun setUp() {
        elementEditsController = mock(classOf<ElementEditsController>())
        mapDataController = mock(classOf<MapDataController>())
        noteEditsController = mock(classOf<NoteEditsController>())

        singleUploader = mock(classOf<ElementEditUploader>())
        mapDataApi = mock(classOf<MapDataApi>())
        statisticsController = mock(classOf<StatisticsController>())

        listener = mock(classOf<OnUploadedChangeListener>())

        uploader = ElementEditsUploader(elementEditsController, noteEditsController, mapDataController, singleUploader, mapDataApi, statisticsController)
        uploader.uploadedChangeListener = listener
    }

    @Test fun `cancel upload works`() = runBlocking {
        val job = launch { uploader.upload() }
        job.cancelAndJoin()
        // verifyNoInteractions(elementEditsController, mapDataController, singleUploader, statisticsController)
    }

    @Test fun `upload works`() = runBlocking {
        val edit = edit()
        val updates = MapDataUpdates()

        every { elementEditsController.getOldestUnsynced() }.returnsMany(edit)
        every { singleUploader.upload(any(), any()) }.returns(updates)

        uploader.upload()

        verifyInvokedExactlyOnce { singleUploader.upload(eq(edit), any()) }
        verifyInvokedExactlyOnce { listener.onUploaded(any(), any()) }
        verifyInvokedExactlyOnce { elementEditsController.markSynced(edit, updates) }
        verifyInvokedExactlyOnce { noteEditsController.updateElementIds(any()) }
        verifyInvokedExactlyOnce { mapDataController.updateAll(updates) }

        verifyInvokedExactlyOnce { statisticsController.addOne(any(), any()) }
    }

    @Test fun `upload catches conflict exception`() = runBlocking {
        // edit modifies node 1 and way 1
        val node1 = node()
        val action: ElementEditAction = mock(classOf<ElementEditAction>())
         every { action.elementKeys }.returns(listOf(
            ElementKey(ElementType.NODE, 1),
            ElementKey(ElementType.WAY, 1),
        ))
        val edit = edit(action = action)

        // ...but way 1 is gone
        every { mapDataApi.getNode(1) }.returns(node1)
        every { mapDataApi.getWayComplete(1) }.returns(null)

        // the edit is the first in the upload queue and the uploader throws a conflict exception
        every { elementEditsController.getOldestUnsynced() }.returnsMany(edit)
        every { singleUploader.upload(any(), any()) }.throws(ConflictException())

        uploader.upload()

        verifyInvokedExactlyOnce { singleUploader.upload(eq(edit), any()) }
        verifyInvokedExactlyOnce { listener.onDiscarded(any(), any()) }

        verifyInvokedExactlyOnce { elementEditsController.markSyncFailed(edit) }
        // verifyNoInteractions(statisticsController)

        verifyInvokedExactlyOnce { mapDataController.updateAll(eq(MapDataUpdates(
            updated = listOf(node1),
            deleted = listOf(ElementKey(ElementType.WAY, 1))
        )))}
    }
}
