package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.elementgeometry.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.upload.ChangesetConflictException
import de.westnordost.streetcomplete.data.osm.upload.ElementConflictException
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class OsmQuestsUploaderTest {
    private lateinit var osmQuestController: OsmQuestController
    private lateinit var elementDB: MergedElementDao
    private lateinit var changesetManager: OpenQuestChangesetsManager
    private lateinit var elementGeometryDB: ElementGeometryDao
    private lateinit var questGiver: OsmQuestGiver
    private lateinit var elementGeometryCreator: OsmApiElementGeometryCreator
    private lateinit var singleChangeUploader: SingleOsmElementTagChangesUploader
    private lateinit var uploader: OsmQuestsUploader

    @Before fun setUp() {
        osmQuestController = mock()
        elementDB = mock()
        on(elementDB.get(any(), anyLong())).thenReturn(createElement())
        changesetManager = mock()
        singleChangeUploader = mock()
        elementGeometryDB = mock()
        questGiver = mock()
        elementGeometryCreator = mock()
        on(elementGeometryCreator.create(any())).thenReturn(mock())
        uploader = OsmQuestsUploader(elementDB, elementGeometryDB, changesetManager, questGiver,
                elementGeometryCreator, osmQuestController, singleChangeUploader)
    }

    @Test fun `cancel upload works`() {
        uploader.upload(AtomicBoolean(true))
        verifyZeroInteractions(changesetManager, singleChangeUploader, elementDB, osmQuestController)
    }

    @Test fun `catches ElementConflict exception`() {
        on(osmQuestController.getAllAnswered()).thenReturn(listOf(createQuest()))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())

        uploader.upload(AtomicBoolean(false))

        // will not throw ElementConflictException
    }

    @Test fun `discard if element was deleted`() {
        val q = createQuest()
        on(osmQuestController.getAllAnswered()).thenReturn(listOf(q))
        on(elementDB.get(any(), anyLong())).thenReturn(null)

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(uploader.uploadedChangeListener)?.onDiscarded(q.osmElementQuestType.javaClass.simpleName, q.position)
    }

    @Test fun `catches ChangesetConflictException exception and tries again once`() {
        on(osmQuestController.getAllAnswered()).thenReturn(listOf(createQuest()))
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ChangesetConflictException())
            .thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        // will not throw ChangesetConflictException but instead call single upload twice
        verify(changesetManager).getOrCreateChangeset(any(), any())
        verify(changesetManager).createChangeset(any(), any())
        verify(singleChangeUploader, times(2)).upload(anyLong(), any(), any())
    }

    @Test fun `close each uploaded quest in local DB and call listener`() {
        val quests = listOf(createQuest(), createQuest())

        on(osmQuestController.getAllAnswered()).thenReturn(quests)
        on(singleChangeUploader.upload(anyLong(), any(), any())).thenReturn(createElement())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(osmQuestController, times(2)).success(any())
        verify(uploader.uploadedChangeListener, times(2))?.onUploaded(any(), any())
        verify(elementDB, times(2)).put(any())
        verify(elementGeometryDB, times(2)).put(any())
        verify(questGiver, times(2)).updateQuests(any(), any())
    }

    @Test fun `delete each unsuccessful upload from local DB and call listener`() {
        val quests = listOf(createQuest(), createQuest())

        on(osmQuestController.getAllAnswered()).thenReturn(quests)
        on(singleChangeUploader.upload(anyLong(), any(), any()))
            .thenThrow(ElementConflictException())

        uploader.uploadedChangeListener = mock()
        uploader.upload(AtomicBoolean(false))

        verify(osmQuestController, times(2)).fail(any())
        verify(uploader.uploadedChangeListener,times(2))?.onDiscarded(any(), any())
        verifyZeroInteractions(questGiver, elementGeometryCreator)
    }

    @Test fun `delete unreferenced elements and clean metadata at the end`() {
        val quest = createQuest()

        on(osmQuestController.getAllAnswered()).thenReturn(listOf(quest))
        on(singleChangeUploader.upload(anyLong(), any(), any())).thenReturn(createElement())

        uploader.upload(AtomicBoolean(false))

        verify(elementDB).deleteUnreferenced()
        verify(quest.osmElementQuestType).cleanMetadata()
    }
}

private fun createQuest() : OsmQuest {
    val changes = StringMapChanges(listOf(StringMapEntryAdd("surface","asphalt")))
    val geometry = ElementPointGeometry(OsmLatLon(0.0, 0.0))
    return OsmQuest(1L, mock(), Element.Type.NODE, 1L, QuestStatus.ANSWERED, changes, "survey",
            Date(), geometry)
}

private fun createElement() = OsmNode(1,1,OsmLatLon(0.0,0.0),null)
