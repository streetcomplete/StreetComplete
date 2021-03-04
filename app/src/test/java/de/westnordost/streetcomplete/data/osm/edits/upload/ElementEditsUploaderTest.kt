package de.westnordost.streetcomplete.data.osm.edits.upload

import de.westnordost.osmapi.map.ElementUpdates
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicBoolean

class ElementEditsUploaderTest {

    private lateinit var elementEditsController: ElementEditsController
    private lateinit var mapDataController: MapDataController
    private lateinit var singleUploader: ElementEditUploader
    private lateinit var mapDataApi: MapDataApi
    private lateinit var statisticsUpdater: StatisticsUpdater

    private lateinit var uploader: ElementEditsUploader
    private lateinit var listener: OnUploadedChangeListener

    @Before fun setUp() {
        elementEditsController = mock()
        mapDataController = mock()
        singleUploader = mock()
        statisticsUpdater = mock()
        mapDataApi = mock()

        listener = mock()

        uploader = ElementEditsUploader(elementEditsController, mapDataController, singleUploader, mapDataApi, statisticsUpdater)
        uploader.uploadedChangeListener = listener
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyNoInteractions(elementEditsController, mapDataController, singleUploader, statisticsUpdater)
    }

    @Test fun `upload works`() {
        val action = DeletePoiNodeAction(1)
        val edit = ElementEdit(1, TestQuestTypeA(), Element.Type.NODE, 1L, "bla", OsmLatLon(1.0,2.0), 123L, false, action)
        val idProvider = mock<ElementIdProvider>()
        val updates = mock<ElementUpdates>()

        on(elementEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(elementEditsController.getIdProvider(anyLong())).thenReturn(idProvider)
        on(singleUploader.upload(any(), any())).thenReturn(updates)

        uploader.upload(AtomicBoolean(false))

        verify(singleUploader).upload(edit, idProvider)
        verify(listener).onUploaded(any(), any())
        verify(elementEditsController).synced(edit, updates)
        verify(mapDataController).updateAll(updates)

        verify(statisticsUpdater).addOne(any(), any())
    }

    @Test fun `upload catches conflict exception`() {
        val action = DeletePoiNodeAction(1)
        val edit = ElementEdit(1, TestQuestTypeA(), Element.Type.NODE, 1L, "bla", OsmLatLon(1.0,2.0), 123L, false, action)
        val idProvider = mock<ElementIdProvider>()
        val updatedNode = mock<Node>()

        on(elementEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(elementEditsController.getIdProvider(anyLong())).thenReturn(idProvider)
        on(singleUploader.upload(any(), any())).thenThrow(ConflictException())
        on(mapDataApi.getNode(anyLong())).thenReturn(updatedNode)

        uploader.upload(AtomicBoolean(false))

        verify(singleUploader).upload(edit, idProvider)
        verify(listener).onDiscarded(any(), any())

        verify(elementEditsController).syncFailed(edit)
        verify(mapDataController).updateAll(eq(ElementUpdates(
            updated = listOf(updatedNode)
        )))

        verify(statisticsUpdater, never()).addOne(any(), any())
    }

    @Test fun `upload catches deleted element exception`() {
        val action = DeletePoiNodeAction(1)
        val edit = ElementEdit(1, TestQuestTypeA(), Element.Type.NODE, 1L, "bla", OsmLatLon(1.0,2.0), 123L, false, action)
        val idProvider = mock<ElementIdProvider>()

        on(elementEditsController.getOldestUnsynced()).thenReturn(edit).thenReturn(null)
        on(elementEditsController.getIdProvider(anyLong())).thenReturn(idProvider)
        on(singleUploader.upload(any(), any())).thenThrow(ConflictException())
        on(mapDataApi.getNode(anyLong())).thenReturn(null)

        uploader.upload(AtomicBoolean(false))

        verify(singleUploader).upload(edit, idProvider)
        verify(listener).onDiscarded(any(), any())

        verify(elementEditsController).syncFailed(edit)
        verify(mapDataController).updateAll(eq(ElementUpdates(
            deleted = listOf(ElementKey( Element.Type.NODE, 1L))
        )))

        verify(statisticsUpdater, never()).addOne(any(), any())
    }
}
