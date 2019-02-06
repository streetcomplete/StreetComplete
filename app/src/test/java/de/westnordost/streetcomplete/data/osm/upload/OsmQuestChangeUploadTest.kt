package de.westnordost.streetcomplete.data.osm.upload

import org.junit.Before
import org.junit.Test


import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.persist.AOsmQuestDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*

import java.lang.IllegalStateException

class OsmQuestChangeUploadTest {
    private lateinit var uploader: OsmQuestChangeUpload
    private lateinit var osmDao: MapDataDao
    private lateinit var questDB: AOsmQuestDao
    private lateinit var elementDB: MergedElementDao
    private lateinit var questGiver: OsmQuestGiver
	private lateinit var quest: OsmQuest
	private lateinit var questType: OsmElementQuestType<*>

	private val nodeId: Long = 5
	private val pos = OsmLatLon(1.0, 2.0)

    @Before fun setUp() {
	    questType = mock(OsmElementQuestType::class.java)
	    on(questType.isApplicableTo(any())).thenReturn(true)

        osmDao = mock(MapDataDao::class.java)
        questDB = mock(AOsmQuestDao::class.java)

        elementDB = mock(MergedElementDao::class.java)
	    on(elementDB.get(Element.Type.NODE, nodeId)).thenReturn(createNode(0))

	    questGiver = mock(OsmQuestGiver::class.java)
	    on(questGiver.updateQuests(any())).thenReturn(OsmQuestGiver.QuestUpdates())

	    quest = OsmQuest(1L, questType, Element.Type.NODE, nodeId, QuestStatus.ANSWERED, null,
		    "test case", null, ElementGeometry(pos))
	    quest.setChanges(StringMapEntryAdd("a key","a value"))

        uploader = OsmQuestChangeUpload(osmDao, questDB, elementDB, questGiver)
    }

	@Test fun `applies changes and uploads element`() {
		quest.setChanges(StringMapEntryAdd("test", "123"))

		val changesetId = 456L

		on(osmDao.uploadChanges(anyLong(), any(), any())).then { invocation ->
			val args = invocation.arguments
			assertEquals(changesetId, args[0])
			assertEquals(mapOf("test" to "123"), (args[1] as Iterable<Element>).first().tags )
		}

		assertTrue(uploader.upload(changesetId, quest).success)

		verify(elementDB).put(any())
		verify(questGiver).updateQuests(any())
		assertEquals(QuestStatus.CLOSED, quest.status)
		verify(questDB).update(quest)
	}

	@Test fun `passes on information about created and removed quests from QuestGiver`() {
		val questUpdates = OsmQuestGiver.QuestUpdates()
		questUpdates.createdQuests.addAll(listOf(mock(OsmQuest::class.java)))
		questUpdates.removedQuestIds.addAll(listOf(1L,2L))
		on(questGiver.updateQuests(any())).thenReturn(questUpdates)

		val result = uploader.upload(1, quest)

		assertEquals(questUpdates.createdQuests, result.createdQuests)
		assertEquals(questUpdates.removedQuestIds, result.removedQuestIds)
	}

	@Test fun `handles a solveable conflict`() {
		quest.setChanges(StringMapEntryAdd("a key", "a value"))

		// throw exception on first try
		doThrow(OsmConflictException::class.java).doNothing().on(osmDao).uploadChanges(anyLong(), any(), any())
		on(osmDao.getNode(anyLong())).thenReturn(createNode(1, mapOf("another key" to "another value")))

		assertTrue(uploader.upload(1, quest).success)

		// one time when getting the new element from server
		// and one time after uploading the element to server
		verify(elementDB, times(2)).put(any())
		verify(questGiver, times(2)).updateQuests(any())
		assertEquals(QuestStatus.CLOSED, quest.status)
		verify(questDB).update(quest)
	}

	@Test(expected = IllegalStateException::class) fun `disallow reusing object`() {
		uploader.upload(1, quest)
		uploader.upload(1, quest)
	}

    @Test fun `drop change when element was deleted`() {
	    doThrow(OsmConflictException::class.java).on(osmDao).uploadChanges(anyLong(), any(), any())
        on(osmDao.getNode(anyLong())).thenReturn(null)

        assertFalse(uploader.upload(1, quest).success)

	    verify(questDB).delete(quest.id)
	    verify(elementDB).delete(quest.elementType, quest.elementId)
	    verify(questGiver).removeQuests(quest.elementType, quest.elementId)
    }

	@Test fun `drop change because the updated element changed the same tag`() {
	    // quest wants to add key=123, but the updated element already has key=abc
	    quest.setChanges(StringMapEntryAdd("key", "123"))

	    doThrow(OsmConflictException::class.java).on(osmDao).uploadChanges(anyLong(), any(), any())
	    on(osmDao.getNode(anyLong())).thenReturn(createNode(1, mapOf("key" to "abc")))

        assertFalse(uploader.upload(1, quest).success)

		verify(elementDB).put(any())
		verify(questGiver).updateQuests(any())
		verify(questDB).delete(quest.id)
	}

	@Test fun `drop change because a tag value of the change is too long`() {
		quest.setChanges(StringMapEntryAdd("too", "l"+"o".repeat(1000)+"ng"))

		assertFalse(uploader.upload(1, quest).success)

		verify(questDB).delete(quest.id)
	}

	@Test fun `drop change because the updated element is no longer applicable to the quest`() {
		doThrow(OsmConflictException::class.java).on(osmDao).uploadChanges(anyLong(), any(), any())
		on(osmDao.getNode(anyLong())).thenReturn(createNode(1))
		on(questType.isApplicableTo(any())).thenReturn(false)

		assertFalse(uploader.upload(1, quest, true).success)

		verify(elementDB).put(any())
		verify(questGiver).updateQuests(any())
		verify(questDB).delete(quest.id)
	}

	@Test fun `do not drop change if the updated element is no longer applicable to the quest but that check is off`() {
		// throw exception on first try
		doThrow(OsmConflictException::class.java).doNothing().on(osmDao).uploadChanges(anyLong(), any(), any())
		on(osmDao.getNode(anyLong())).thenReturn(createNode(1))
		on(questType.isApplicableTo(any())).thenReturn(false)

		assertTrue(uploader.upload(1, quest, false).success)

		verify(elementDB, times(2)).put(any())
		verify(questGiver, times(2)).updateQuests(any())
		assertEquals(QuestStatus.CLOSED, quest.status)
		verify(questDB).update(quest)
	}

	@Test(expected = OsmConflictException::class) fun `do not catch a changeset conflict exception`() {
		// OSM Dao returns an element with the same version as in the database
		on(elementDB.get(Element.Type.NODE, nodeId)).thenReturn(createNode(0))
		on(osmDao.getNode(anyLong())).thenReturn(createNode(0))
		doThrow(OsmConflictException::class.java).on(osmDao).uploadChanges(anyLong(), any(), any())

		uploader.upload(1, quest)
	}

	private fun createNode(version: Int, tags: Map<String, String>? = null) =
		OsmNode(nodeId, version, pos, tags)

	private fun OsmQuest.setChanges(vararg changes: StringMapEntryChange) {
		this.setChanges(StringMapChanges(changes.asList()), "test")
	}
}
