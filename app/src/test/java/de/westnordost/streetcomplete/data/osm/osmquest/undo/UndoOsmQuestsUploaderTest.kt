package de.westnordost.streetcomplete.data.osm.osmquest.undo

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.elementgeometry.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.osmquest.SingleOsmElementTagChangesUploader
import de.westnordost.streetcomplete.data.osm.upload.ChangesetConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import java.util.concurrent.atomic.AtomicBoolean

class UndoOsmQuestsUploaderTest {
    private lateinit var undoQuestDB: UndoOsmQuestDao
    private lateinit var elementDB: MergedElementDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var elementGeometryDB: ElementGeometryDao
    private lateinit var questGiver: OsmQuestGiver
    private lateinit var elementGeometryCreator: OsmApiElementGeometryCreator
    private lateinit var singleChangeUploader: SingleOsmElementTagChangesUploader
    private lateinit var uploader: UndoOsmQuestsUploader

    @Before fun setUp() {
        undoQuestDB = mock()
        elementDB = mock()
        on(elementDB.get(any(), anyLong())).thenReturn(createElement())
        changesetManager = mock()
        singleChangeUploader = mock()
        elementGeometryDB = mock()
        questGiver = mock()
        elementGeometryCreator = mock()
        on(elementGeometryCreator.create(any())).thenReturn(mock())
        uploader = UndoOsmQuestsUploader(elementDB, elementGeometryDB, changesetManager, questGiver,
                elementGeometryCreator, undoQuestDB, singleChangeUploader)
    }

    @Test fun `cancel upload works`() {
        val cancelled = AtomicBoolean(true)
        uploader.upload(cancelled)
        verifyZeroInteractions(changesetManager, singleChangeUploader, elementDB, undoQuestDB)
    }

    @Test fun `catches ElementConflict exception`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(createUndoQuest()))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        val q = createUndoQuest()
        on(undoQuestDB.getAll()).thenReturn(listOf(q))
        on(elementDB.get(any(), anyLong())).thenReturn(null)

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.osmElementQuestType.javaClass.simpleName, q.position)
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(undoQuestDB.getAll()).thenReturn(listOf(createUndoQuest()))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(singleChangeUploader, times(2)).upload(anyLong(), any(), any())
    }

    @Test fun `delete each uploaded quest from local DB and calls listener`() {
        val quests = listOf(createUndoQuest(), createUndoQuest())
        on(undoQuestDB.getAll()).thenReturn(quests)
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())
            .thenReturn(createElement())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(undoQuestDB, times(2)).delete(anyLong())
        verify(uploader.uploadedChangeListener)?.onUploaded(quests[0].osmElementQuestType.javaClass.simpleName, quests[0].position)
        verify(uploader.uploadedChangeListener)?.onDiscarded(quests[1].osmElementQuestType.javaClass.simpleName, quests[1].position)

        verify(elementDB, times(1)).put(any())
        verify(elementGeometryDB, times(1)).put(any())
        verify(questGiver, times(1)).updateQuests(any(), any())
        verifyNoMoreInteractions(questGiver)
    }

    @Test fun `delete unreferenced elements and clean metadata at the end`() {
        val quest = createUndoQuest()

        on(undoQuestDB.getAll()).thenReturn(listOf(quest))
        on(singleChangeUploader.upload(anyLong(), any(), any())).thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        verify(elementDB).deleteUnreferenced()
        verify(quest.osmElementQuestType).cleanMetadata()
    }
}

private fun createUndoQuest() : UndoOsmQuest {
    val changes = StringMapChanges(listOf(StringMapEntryAdd("surface","asphalt")))
    val geometry = ElementPointGeometry(OsmLatLon(0.0, 0.0))
    return UndoOsmQuest(1, mock(), Element.Type.NODE, 1, changes, "survey", geometry)
}

private fun createElement() = OsmNode(1,1,OsmLatLon(0.0,0.0),null)
