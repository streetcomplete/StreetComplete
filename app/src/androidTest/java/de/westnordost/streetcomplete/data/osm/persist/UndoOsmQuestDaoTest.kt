package de.westnordost.streetcomplete.data.osm.persist

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.UndoOsmQuest
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.persist.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UndoOsmQuestDaoTest : ApplicationDbTestCase() {

    private val questType = TestQuestType()
    private lateinit var geometryDao: ElementGeometryDao
    private lateinit var dao: UndoOsmQuestDao

    @Before fun createDaos() {
        val elementGeometryMapping = ElementGeometryMapping(serializer)
        geometryDao = ElementGeometryDao(dbHelper, elementGeometryMapping)
        dao = UndoOsmQuestDao(dbHelper, UndoOsmQuestMapping(serializer, QuestTypeRegistry(listOf(questType)), elementGeometryMapping))
    }

    @Test fun getButNothingIsThere() {
        assertNull(dao.get(1L))
    }

    @Test fun getAllButNothingIsThere() {
        assertEquals(listOf<UndoOsmQuest>(), dao.getAll())
    }

    @Test fun addAndGet() {
        val id = 1L
        val input = addUndoQuest(id)
        val output = dao.get(id)!!

        assertEquals(input.id, output.id)
        assertEquals(input.type, output.type)
        assertEquals(input.geometry, output.geometry)
        assertEquals(input.changesSource, output.changesSource)
        assertEquals(input.changes, output.changes)
        assertEquals(input.elementType, output.elementType)
        assertEquals(input.elementId, output.elementId)
    }

    @Test fun delete() {
        val id = 1L
        addUndoQuest(id)
        dao.delete(id)
        assertNull(dao.get(id))
    }

    @Test fun getAll() {
        addUndoQuest(1L, 1L)
        addUndoQuest(2L, 2L)
        assertEquals(2, dao.getAll().size)
    }

    private fun addUndoQuest(id: Long, elementId: Long = 1L): UndoOsmQuest {
        val geometry = ElementPointGeometry(OsmLatLon(1.0, 2.0))
        val elementType = Element.Type.NODE
        val changes = StringMapChanges(listOf(StringMapEntryAdd("foo", "bar")))
        val quest = UndoOsmQuest(id, questType, elementType, elementId, changes, "test", geometry)
        geometryDao.put(ElementGeometryEntry(elementType, elementId, geometry))
        dao.add(quest)
        return quest
    }
}
