package de.westnordost.streetcomplete.data.osm.mapdata

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.*
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.testutils.rel

import org.mockito.Mockito.*

class ElementDaoTest {
    private lateinit var nodeDao: NodeDao
    private lateinit var wayDao: WayDao
    private lateinit var relationDao: RelationDao
    private lateinit var dao: ElementDao

    @Before fun setUp() {
        nodeDao = mock()
        wayDao = mock()
        relationDao = mock()
        dao = ElementDao(nodeDao, wayDao, relationDao)
    }

    @Test fun putNode() {
        val node = node(1)
        dao.put(node)
        verify(nodeDao).put(node)
    }

    @Test fun getNode() {
        dao.get(NODE, 1L)
        verify(nodeDao).get(1L)
    }

    @Test fun deleteNode() {
        dao.delete(NODE, 1L)
        verify(nodeDao).delete(1L)
    }

    @Test fun putWay() {
        val way = way()
        dao.put(way)
        verify(wayDao).put(way)
    }

    @Test fun getWay() {
        dao.get(WAY, 1L)
        verify(wayDao).get(1L)
    }

    @Test fun deleteWay() {
        dao.delete(WAY, 1L)
        verify(wayDao).delete(1L)
    }

    @Test fun putRelation() {
        val relation = rel()
        dao.put(relation)
        verify(relationDao).put(relation)
    }

    @Test fun getRelation() {
        dao.get(RELATION, 1L)
        verify(relationDao).get(1L)
    }

    @Test fun deleteRelation() {
        dao.delete(RELATION, 1L)
        verify(relationDao).delete(1L)
    }

    @Test fun putAllRelations() {
        dao.putAll(listOf(rel()))
        verify(relationDao).putAll(anyCollection())
    }

    @Test fun putAllWays() {
        dao.putAll(listOf(way()))
        verify(wayDao).putAll(anyCollection())
    }

    @Test fun putAllNodes() {
        dao.putAll(listOf(node()))
        verify(nodeDao).putAll(anyCollection())
    }

    @Test fun putAllElements() {
        dao.putAll(listOf(node(), way(), rel()))

        verify(nodeDao).putAll(anyCollection())
        verify(wayDao).putAll(anyCollection())
        verify(relationDao).putAll(anyCollection())
    }

    @Test fun deleteAllElements() {
        dao.deleteAll(listOf(
            ElementKey(NODE,0),
            ElementKey(WAY,0),
            ElementKey(RELATION,0)
        ))

        verify(nodeDao).deleteAll(listOf(0L))
        verify(wayDao).deleteAll(listOf(0L))
        verify(relationDao).deleteAll(listOf(0L))
    }

    @Test fun getAllElements() {
        dao.getAll(listOf(
            ElementKey(NODE,0),
            ElementKey(WAY,0),
            ElementKey(RELATION,0)
        ))

        verify(nodeDao).getAll(listOf(0L))
        verify(wayDao).getAll(listOf(0L))
        verify(relationDao).getAll(listOf(0L))
    }
}
