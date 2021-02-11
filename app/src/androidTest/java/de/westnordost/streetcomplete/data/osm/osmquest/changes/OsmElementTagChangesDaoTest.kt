package de.westnordost.streetcomplete.data.osm.osmquest.changes

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.changes.ChangeOsmElementTags
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class OsmElementTagChangesDaoTest : ApplicationDbTestCase() {

    private val questType = TestQuestType()
    private lateinit var dao: OsmElementTagChangesDao

    @Before fun createDaos() {
        dao = OsmElementTagChangesDao(dbHelper, ElementTagChangesMapping(serializer, QuestTypeRegistry(listOf(questType))))
    }

    @Test fun getButNothingIsThere() {
        assertNull(dao.get(1L))
    }

    @Test fun getAllButNothingIsThere() {
        assertEquals(listOf<ChangeOsmElementTags>(), dao.getAll())
    }

    @Test fun addAndGet() {
        val listener = mock(OsmElementTagChangesDao.Listener::class.java)
        dao.addListener(listener)

        val id = 1L
        val input = addUndoQuest(id)
        verify(listener).onAddedElementTagChanges()
        val output = dao.get(id)!!

        assertEquals(input, output)
    }

    @Test fun delete() {
        val id = 1L
        addUndoQuest(id)

        val listener = mock(OsmElementTagChangesDao.Listener::class.java)
        dao.addListener(listener)

        dao.delete(id)
        assertNull(dao.get(id))
        verify(listener).onDeletedElementTagChanges()
    }

    @Test fun getAll() {
        addUndoQuest(1L, 1L)
        addUndoQuest(2L, 2L)
        assertEquals(2, dao.getAll().size)
    }

    @Test fun getCount0() {
        assertEquals(0, dao.getCount())
    }

    @Test fun getCount1() {
        addUndoQuest(1L)
        assertEquals(1, dao.getCount())
    }

    @Test fun getCount2() {
        addUndoQuest(1L, 1L)
        addUndoQuest(2L, 2L)
        assertEquals(2, dao.getCount())
    }

    private fun addUndoQuest(id: Long, elementId: Long = 1L): ChangeOsmElementTags {
        val elementType = Element.Type.NODE
        val changes = StringMapChanges(listOf(StringMapEntryAdd("foo", "bar")))
        val quest = ChangeOsmElementTags(id, questType, elementType, elementId, changes, "test", OsmLatLon(1.0, 2.0), false)
        dao.add(quest)
        return quest
    }
}
