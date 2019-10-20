package de.westnordost.streetcomplete.data.osm.persist

import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.OsmQuestSplitWay
import de.westnordost.streetcomplete.data.osm.changes.SplitAtLinePosition
import de.westnordost.streetcomplete.data.osm.changes.SplitAtPoint
import de.westnordost.streetcomplete.data.osm.persist.test.*
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class OsmQuestSplitWayDaoTest : ApplicationDbTestCase() {

	private val questType = TestQuestType()
	private lateinit var dao: OsmQuestSplitWayDao

	@Before fun createDao() {
		dao = OsmQuestSplitWayDao(dbHelper, serializer, QuestTypeRegistry(listOf(questType)))
	}

    @Test fun getButNothingIsThere() {
	    assertNull(dao.get(1L))
    }

	@Test fun getAllButNothingIsThere() {
		assertEquals(listOf<OsmQuestSplitWay>(), dao.getAll())
	}

    @Test fun putAndGet() {
	    val id = 1L
	    val input = createOsmQuestSplitWay(id)
	    dao.put(input)
	    val output = dao.get(id)!!

	    assertEquals(input.questType, output.questType)
	    assertEquals(input.wayId, output.wayId)
	    assertEquals(input.questId, output.questId)
	    assertEquals(input.splits.size, output.splits.size)
	    val it = input.splits.listIterator()
	    val ot = output.splits.listIterator()
	    while(it.hasNext()) {
		    val iSplit = it.next()
		    val oSplit = ot.next()
            assertEquals(iSplit, oSplit)
	    }
    }

    @Test fun delete() {
	    val id = 1L
	    val input = createOsmQuestSplitWay(id)
	    dao.put(input)
	    dao.delete(id)
	    assertNull(dao.get(id))
    }

	@Test fun putReplaces() {
		val id = 1L
		dao.put(createOsmQuestSplitWay(id, 1L))
		dao.put(createOsmQuestSplitWay(id, 123L))
		assertEquals(123L, dao.get(id)?.wayId)
		assertEquals(1, dao.getAll().size)
	}

	@Test fun getAll() {
		dao.put(createOsmQuestSplitWay(1L, 1L))
		dao.put(createOsmQuestSplitWay(2L, 2L))
		assertEquals(2, dao.getAll().size)
	}

    @Test fun getCount0() {
        assertEquals(0, dao.getCount())
    }

    @Test fun getCount1() {
        dao.put(createOsmQuestSplitWay(1L, 1L))
        assertEquals(1, dao.getCount())
    }

    @Test fun getCount2() {
        dao.put(createOsmQuestSplitWay(1L, 1L))
        dao.put(createOsmQuestSplitWay(2L, 2L))
        assertEquals(2, dao.getCount())
    }

	private fun createOsmQuestSplitWay(id: Long, wayId: Long = 1L): OsmQuestSplitWay {
		val pos1 = OsmLatLon(0.0, 0.0)
		val pos2 = OsmLatLon(1.0, 0.0)
		return OsmQuestSplitWay(id, questType, wayId, "test", listOf(
            SplitAtLinePosition(pos1, pos2, 0.3),
            SplitAtPoint(pos2))
		)
	}
}


