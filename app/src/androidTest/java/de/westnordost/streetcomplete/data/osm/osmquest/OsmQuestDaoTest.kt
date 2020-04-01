package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.data.BoundingBox
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

import java.util.Date

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.elementgeometry.*
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import org.mockito.Mockito.*

class OsmQuestDaoTest : ApplicationDbTestCase() {
    private lateinit var geometryDao: ElementGeometryDao
    private lateinit var dao: OsmQuestDao

    @Before fun createDaos() {
        val elementGeometryMapping = ElementGeometryMapping(serializer)
        geometryDao = ElementGeometryDao(dbHelper, elementGeometryMapping)
        val list = listOf<QuestType<*>>(TEST_QUEST_TYPE, TEST_QUEST_TYPE2)
        val mapping = OsmQuestMapping(serializer, QuestTypeRegistry(list), elementGeometryMapping)
        dao = OsmQuestDao(dbHelper, mapping)
    }

    @Test fun addGetNoChanges() {
        val quest = create()
        addToDaos(quest)
        assertEquals(quest, dao.get(quest.id!!))
    }

    @Test fun addGetWithChanges() {
        val quest = create(
                changes = StringMapChanges(listOf(
                        StringMapEntryAdd("a key", "a value"),
                        StringMapEntryDelete("delete this", "key"),
                        StringMapEntryModify("modify", "this", "to that")
                )),
                changesSource = "bla",
                status = QuestStatus.CLOSED
        )
        addToDaos(quest)

        assertEquals(quest, dao.get(quest.id!!))
    }

    @Test fun addAnsweredQuestTriggersListener() {
        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        addToDaos(create(status = QuestStatus.ANSWERED))

        verify(listener).onAnsweredQuestCountIncreased()
    }

    @Test fun deleteButNothingIsThere() {
        assertFalse(dao.delete(1L))
    }

    @Test fun addAndDelete() {
        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        val quest = create()
        addToDaos(quest)

        assertTrue(dao.delete(quest.id!!))
        assertNull(dao.get(quest.id!!))
        assertFalse(dao.delete(quest.id!!))

        // because the quests never had the status ANSWERED in the first place
        verifyZeroInteractions(listener)
    }

    @Test fun deleteAnsweredQuestTriggersListener() {
        val quest = create(status = QuestStatus.ANSWERED)
        addToDaos(quest)

        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)
        dao.delete(quest.id!!)

        verify(listener).onAnsweredQuestCountDecreased()
    }

    @Test fun update() {
        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        val quest = create()
        addToDaos(quest)

        quest.status = QuestStatus.HIDDEN
        quest.changesSource = "ho"
        quest.changes = StringMapChanges(listOf(StringMapEntryAdd("a key", "a value")))

        dao.update(quest)

        assertEquals(quest, dao.get(quest.id!!))

        // because the quests never had the status ANSWERED in the first place
        verifyZeroInteractions(listener)
    }

    @Test fun updateToAnsweredQuestTriggersListener() {
        val quest = create()
        addToDaos(quest)

        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        quest.status = QuestStatus.ANSWERED
        dao.update(quest)

        verify(listener).onAnsweredQuestCountIncreased()
    }

    @Test fun updateFromAnsweredQuestTriggersListener() {
        val quest = create(status = QuestStatus.ANSWERED)
        addToDaos(quest)

        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        quest.status = QuestStatus.NEW
        dao.update(quest)

        verify(listener).onAnsweredQuestCountDecreased()
    }

    @Test fun addAllAndDeleteAll() {
        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        val quests = listOf(
                create(elementId = 1),
                create(elementId = 2),
                create(elementId = 3)
        )
        geometryDao.putAll(quests.map { it.geometryEntry } )
        assertEquals(3, dao.addAll(quests))

        for (quest in quests) {
            assertNotNull(quest.id)
            assertEquals(quest, dao.get(quest.id!!))
        }

        assertEquals(3, dao.deleteAllIds(quests.map { it.id!! }))
        assertEquals(0, dao.getCount())

        // because the added quests were not ANSWERED in the first place
        verifyZeroInteractions(listener)
    }

    @Test fun addAllAndDeleteAllTriggersListenerIfAtLeastOneIsOfStatusAnswered() {
        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        val quests = listOf(
            create(elementId = 1),
            create(elementId = 2, status = QuestStatus.ANSWERED)
        )

        addToDaos(*quests.toTypedArray())

        verify(listener).onAnsweredQuestCountIncreased()

        dao.deleteAllIds(quests.map { it.id!! })

        verify(listener).onAnsweredQuestCountDecreased()
    }

    @Test fun unhideAll() {
        addToDaos(
                create(elementId = 1, status = QuestStatus.HIDDEN),
                create(elementId = 2, status = QuestStatus.HIDDEN)
        )

        assertEquals(0, dao.getCount(listOf(QuestStatus.NEW)))

        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        dao.unhideAll()

        assertEquals(2, dao.getCount(listOf(QuestStatus.NEW)))

        // because the added quests were not ANSWERED in the first place
        verifyZeroInteractions(listener)
    }

    @Test fun getAllByBBox() {
        addToDaos(
                create(elementId = 1, geometry = ElementPointGeometry(OsmLatLon(5.0, 5.0))),
                create(elementId = 2, geometry = ElementPointGeometry(OsmLatLon(11.0, 11.0)))
        )

        assertEquals(1, dao.getAll(bounds = BoundingBox(0.0, 0.0, 10.0, 10.0)).size)
        assertEquals(2, dao.getAll().size)
    }

    @Test fun getAllByStatus() {
        addToDaos(
                create(elementId = 1, status = QuestStatus.HIDDEN),
                create(elementId = 2, status = QuestStatus.NEW)
        )

        assertEquals(1, dao.getAll(statusIn = listOf(QuestStatus.HIDDEN)).size)
        assertEquals(1, dao.getAll(statusIn = listOf(QuestStatus.NEW)).size)
        assertEquals(0, dao.getAll(statusIn = listOf(QuestStatus.CLOSED)).size)
        assertEquals(2, dao.getAll(statusIn = listOf(QuestStatus.NEW, QuestStatus.HIDDEN)).size)
    }

    @Test fun getAllByElement() {
        addToDaos(
                create(elementType = Element.Type.NODE, elementId = 1),
                create(elementType = Element.Type.WAY, elementId = 2)
        )

        assertEquals(1, dao.getAll(element = ElementKey(Element.Type.WAY, 2)).size)
        assertEquals(1, dao.getAll(element = ElementKey(Element.Type.NODE, 1)).size)
    }

    @Test fun getAllByQuestTypes() {
        addToDaos(
                create(questType = TEST_QUEST_TYPE),
                create(questType = TEST_QUEST_TYPE2)
        )

        assertEquals(1, dao.getAll(questTypes = listOf(TestQuestType::class.java.simpleName)).size)
        assertEquals(2, dao.getAll(questTypes = listOf(TestQuestType::class.java.simpleName, TestQuestType2::class.java.simpleName)).size)
        assertEquals(1, dao.getAll(questTypes = listOf(TestQuestType::class.java.simpleName)).size)
    }

    @Test fun getAllIds() {
        addToDaos(
                create(questType = TEST_QUEST_TYPE),
                create(questType = TEST_QUEST_TYPE2)
        )
        assertEquals(2, dao.getAllIds().size)
    }

    @Test fun getCount() {
        addToDaos(
                create(questType = TEST_QUEST_TYPE),
                create(questType = TEST_QUEST_TYPE2)
        )
        assertEquals(2, dao.getCount())
    }

    @Test fun getAnsweredCount() {
        assertEquals(0, dao.answeredCount)
        addToDaos(create(questType = TEST_QUEST_TYPE))
        assertEquals(0, dao.answeredCount)
        val quest2 = create(questType = TEST_QUEST_TYPE2, status = QuestStatus.ANSWERED)
        addToDaos(quest2)
        assertEquals(1, dao.answeredCount)
        dao.delete(quest2.id!!)
        assertEquals(0, dao.answeredCount)
    }

    @Test fun deleteAll() {
        addToDaos(
                create(questType = TEST_QUEST_TYPE),
                create(questType = TEST_QUEST_TYPE2)
        )
        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        assertEquals(2, dao.deleteAll())
        // because the added quests were not ANSWERED in the first place
        verifyZeroInteractions(listener)
    }

    @Test fun deleteAllTriggersListenerIfAtLeastOneIsOfStatusAnswered() {
        addToDaos(
            create(questType = TEST_QUEST_TYPE),
            create(questType = TEST_QUEST_TYPE2, status = QuestStatus.ANSWERED)
        )

        val listener = mock(OsmQuestDao.AnsweredQuestCountListener::class.java)
        dao.addAnsweredQuestCountListener(listener)

        dao.deleteAll()

        verify(listener).onAnsweredQuestCountDecreased()
    }

    private fun addToDaos(vararg quests: OsmQuest) {
        for (quest in quests) {
            geometryDao.put(quest.geometryEntry)
            assertTrue(dao.add(quest))
        }
    }
}

private val OsmQuest.geometryEntry get() = ElementGeometryEntry(elementType, elementId, geometry)

private val TEST_QUEST_TYPE = TestQuestType()
private val TEST_QUEST_TYPE2 = TestQuestType2()

private fun create(
        questType: OsmElementQuestType<*> = TEST_QUEST_TYPE,
        elementType: Element.Type = Element.Type.NODE,
        elementId: Long = 1,
        status: QuestStatus = QuestStatus.NEW,
        geometry: ElementGeometry = ElementPointGeometry(OsmLatLon(5.0, 5.0)),
        changes: StringMapChanges? = null,
        changesSource: String? = null
) = OsmQuest(
        null, questType, elementType, elementId, status, changes, changesSource, Date(), geometry
)
