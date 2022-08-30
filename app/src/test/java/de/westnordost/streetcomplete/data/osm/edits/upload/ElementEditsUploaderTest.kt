package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.user.statistics.StatisticsController
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

class ElementEditsUploaderTest {

    private lateinit var elementEditsController: ElementEditsController
    private lateinit var mapDataController: MapDataController
    private lateinit var noteEditsController: NoteEditsController
    private lateinit var singleUploader: ElementEditUploader
    private lateinit var mapDataApi: MapDataApi
    private lateinit var statisticsController: StatisticsController

    private lateinit var uploader: ElementEditsUploader
    private lateinit var listener: OnUploadedChangeListener

    @Before fun setUp() {
        elementEditsController = mock()
        mapDataController = mock()
        noteEditsController = mock()

        singleUploader = mock()
        mapDataApi = mock()
        statisticsController = mock()

        listener = mock()

        uploader = ElementEditsUploader(elementEditsController, noteEditsController, mapDataController, singleUploader, mapDataApi, mock())
        uploader.uploadedChangeListener = listener
    }

    @Test fun `cancel upload works`() = runBlocking {
        val job = launch { uploader.upload(mock()) }
        job.cancelAndJoin()
        verifyNoInteractions(elementEditsController, mapDataController, singleUploader, statisticsController)
    }

    @Test fun `upload works`() = runBlocking {
        val edit = edit()
        val idProvider = mock<ElementIdProvider>()
        val updates = mock<MapDataUpdates>()

        on(elementEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(elementEditsController.getIdProvider(anyLong())).thenReturn(idProvider)
        on(singleUploader.upload(any(), any())).thenReturn(updates)

        uploader.upload(mock())

        verify(singleUploader).upload(edit, idProvider)
        verify(listener).onUploaded(any(), any())
        verify(elementEditsController).markSynced(edit, updates)
        verify(noteEditsController).updateElementIds(any())
        verify(mapDataController).updateAll(updates)
    }

    @Test fun `upload catches conflict exception`() = runBlocking {
        val edit = edit()
        val idProvider = mock<ElementIdProvider>()
        val updatedNode = node()

        on(elementEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(elementEditsController.getIdProvider(anyLong())).thenReturn(idProvider)
        on(singleUploader.upload(any(), any())).thenThrow(ConflictException())
        on(mapDataApi.getNode(anyLong())).thenReturn(updatedNode)

        uploader.upload(mock())

        verify(singleUploader).upload(edit, idProvider)
        verify(listener).onDiscarded(any(), any())

        verify(elementEditsController).markSyncFailed(edit)
        verify(mapDataController).updateAll(eq(MapDataUpdates(
            updated = listOf(updatedNode)
        )))

        verify(statisticsController, never()).addOne(any(), any())
    }

    @Test fun `upload catches deleted element exception`() = runBlocking {
        val edit = edit(element = node(1))
        val idProvider = mock<ElementIdProvider>()

        on(elementEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(elementEditsController.getIdProvider(anyLong())).thenReturn(idProvider)
        on(singleUploader.upload(any(), any())).thenThrow(ConflictException())
        on(mapDataApi.getNode(anyLong())).thenReturn(null)

        uploader.upload(mock())

        verify(singleUploader).upload(edit, idProvider)
        verify(listener).onDiscarded(any(), any())

        verify(elementEditsController).markSyncFailed(edit)
        verify(mapDataController).updateAll(eq(MapDataUpdates(
            deleted = listOf(ElementKey(ElementType.NODE, 1L))
        )))

        verify(statisticsController, never()).addOne(any(), any())
    }
}
