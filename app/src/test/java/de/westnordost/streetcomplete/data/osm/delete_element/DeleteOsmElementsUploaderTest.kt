package de.westnordost.streetcomplete.data.osm.delete_element

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementUpdateController
import de.westnordost.streetcomplete.data.osm.upload.ChangesetConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementDeletedException
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import de.westnordost.streetcomplete.mock
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicBoolean

class DeleteOsmElementsUploaderTest {
    private lateinit var deleteElementDB: DeleteOsmElementDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var singleUploader: DeleteSingleOsmElementUploader
    private lateinit var statisticsUpdater: StatisticsUpdater
    private lateinit var elementUpdateController: OsmElementUpdateController
    private lateinit var uploader: DeleteOsmElementsUploader

    @Before fun setUp() {
        deleteElementDB = mock()
        changesetManager = mock()
        singleUploader = mock()
        elementUpdateController = mock()
        statisticsUpdater = mock()
        uploader = DeleteOsmElementsUploader(changesetManager, elementUpdateController, deleteElementDB,
            singleUploader, statisticsUpdater)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(changesetManager, singleUploader, elementUpdateController, statisticsUpdater, deleteElementDB)
    }

    @Test fun `catches ElementConflict exception`() {
        val q = createDeleteOsmElement()
        on(deleteElementDB.getAll()).thenReturn(listOf(q))
        on(singleUploader.upload(anyLong(), any())).thenThrow(ElementConflictException())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(createElement())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.questType.javaClass.simpleName, q.position)

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        val q = createDeleteOsmElement()
        on(deleteElementDB.getAll()).thenReturn(listOf(q))
        on(singleUploader.upload(anyLong(), any())).thenThrow(ElementDeletedException())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(createElement())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.questType.javaClass.simpleName, q.position)
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(deleteElementDB.getAll()).thenReturn(listOf(createDeleteOsmElement()))
        doThrow(ChangesetConflictException())
            .doNothing()
            .on(singleUploader).upload(anyLong(), any())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(singleUploader, times(2)).upload(anyLong(), any())
    }

    @Test fun `delete each uploaded delete element from local DB and calls listener`() {
        val quests = listOf(createDeleteOsmElement(), createDeleteOsmElement())

        on(deleteElementDB.getAll()).thenReturn(quests)
        doThrow(ElementConflictException())
            .doNothing()
            .on(singleUploader).upload(anyLong(), any())
        on(elementUpdateController.get(any(), anyLong())).thenReturn(createElement())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(deleteElementDB, times(2)).delete(anyLong())
        verify(uploader.uploadedChangeListener)?.onUploaded(quests[0].questType.javaClass.simpleName, quests[0].position)
        verify(uploader.uploadedChangeListener)?.onDiscarded(quests[1].questType.javaClass.simpleName,quests[1].position)

        verify(elementUpdateController, times(1)).delete(any(), anyLong())
        verify(elementUpdateController, times(2)).get(any(), anyLong())
        verify(statisticsUpdater).addOne(any(), any())
        verifyNoMoreInteractions(elementUpdateController)
    }

    @Test fun `clean metadata at the end`() {
        val quest = createDeleteOsmElement()

        on(deleteElementDB.getAll()).thenReturn(listOf(quest))
        on(elementUpdateController.get(any(), anyLong())).thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        verify(quest.osmElementQuestType).cleanMetadata()
    }
}

private fun createDeleteOsmElement() = DeleteOsmElement(1, mock(), 1, Element.Type.NODE, "survey", OsmLatLon(1.0,0.1))

private fun createElement() = OsmNode(1,1, OsmLatLon(1.0,0.1), null)
