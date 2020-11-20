package de.westnordost.streetcomplete.data.osm.delete_element

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType
import de.westnordost.streetcomplete.data.osm.osmquest.TestQuestType2
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito.*

class DeleteOsmElementDaoTest : ApplicationDbTestCase() {

    private val questType = TestQuestType()
    private val questType2 = TestQuestType2()
    private lateinit var dao: DeleteOsmElementDao

    @Before fun createDao() {
        val mapping = DeleteOsmElementMapping(QuestTypeRegistry(listOf(questType, questType2)))
        dao = DeleteOsmElementDao(dbHelper, mapping)
    }

    @Test fun getButNothingIsThere() {
        assertNull(dao.get(1L))
    }

    @Test fun getAllButNothingIsThere() {
        assertEquals(listOf<DeleteOsmElement>(), dao.getAll())
    }

    @Test fun addAndGet() {
        val listener = mock(DeleteOsmElementDao.Listener::class.java)
        dao.addListener(listener)

        val id = 1L
        val input = createDeleteOsmElement(id)
        dao.add(input)
        verify(listener).onAddedDeleteOsmElement()
        val output = dao.get(id)!!

        assertEquals(input, output)
    }

    @Test fun delete() {
        val listener = mock(DeleteOsmElementDao.Listener::class.java)
        dao.addListener(listener)

        val id = 1L
        assertFalse(dao.delete(id))
        val input = createDeleteOsmElement(id)
        dao.add(input)
        verify(listener).onAddedDeleteOsmElement()
        assertTrue(dao.delete(id))
        verify(listener).onDeletedDeleteOsmElement()
        assertNull(dao.get(id))
    }

    @Test(expected = Exception::class)
    fun addingTwiceThrowsException() {
        val id = 1L
        dao.add(createDeleteOsmElement(id, 1L))
        dao.add(createDeleteOsmElement(id, 123L))
    }

    @Test fun getAll() {
        dao.add(createDeleteOsmElement(1L, 1L))
        dao.add(createDeleteOsmElement(2L, 2L))
        assertEquals(2, dao.getAll().size)
    }

    @Test fun getCount0() {
        assertEquals(0, dao.getCount())
    }

    @Test fun getCount1() {
        dao.add(createDeleteOsmElement(1L, 1L))
        assertEquals(1, dao.getCount())
    }

    @Test fun getCount2() {
        dao.add(createDeleteOsmElement(1L, 1L))
        dao.add(createDeleteOsmElement(2L, 2L))
        assertEquals(2, dao.getCount())
    }

    private fun createDeleteOsmElement(
        id: Long,
        elementId: Long = 1L,
        elementType: Element.Type = Element.Type.NODE
    ): DeleteOsmElement {
        return DeleteOsmElement(id, questType, elementId, elementType, "test", OsmLatLon(0.0,0.0))
    }
}


