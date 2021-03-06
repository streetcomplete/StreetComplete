package de.westnordost.streetcomplete.data.osm.mapdata

import org.junit.Before
import org.junit.Test

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.node
import de.westnordost.osmapi.map.data.OsmRelation
import de.westnordost.osmapi.map.data.OsmRelationMember
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on

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
        dao.get(Element.Type.NODE, 1L)
        verify(nodeDao).get(1L)
    }

    @Test fun deleteNode() {
        dao.delete(Element.Type.NODE, 1L)
        verify(nodeDao).delete(1L)
    }

    @Test fun putWay() {
        val way: Way = mock()
        on(way.type).thenReturn(Element.Type.WAY)
        dao.put(way)
        verify(wayDao).put(way)
    }

    @Test fun getWay() {
        val way: Way = mock()
        on(way.id).thenReturn(1L)

        dao.get(Element.Type.WAY, 1L)
        verify(wayDao).get(1L)
    }

    @Test fun deleteWay() {
        dao.delete(Element.Type.WAY, 1L)
        verify(wayDao).delete(1L)
    }

    @Test fun putRelation() {
        val relation: Relation = mock()
        on(relation.type).thenReturn(Element.Type.RELATION)
        dao.put(relation)
        verify(relationDao).put(relation)
    }

    @Test fun getRelation() {
        val relation: Relation = mock()
        on(relation.id).thenReturn(1L)

        dao.get(Element.Type.RELATION, 1L)
        verify(relationDao).get(1L)
    }

    @Test fun deleteRelation() {
        dao.delete(Element.Type.RELATION, 1L)
        verify(relationDao).delete(1L)
    }

    @Test fun putAllRelations() {
        dao.putAll(listOf(createARelation()))
        verify(relationDao).putAll(anyCollection())
    }

    @Test fun putAllWays() {
        dao.putAll(listOf(createAWay()))
        verify(wayDao).putAll(anyCollection())
    }

    @Test fun putAllNodes() {
        dao.putAll(listOf(node()))
        verify(nodeDao).putAll(anyCollection())
    }

    @Test fun putAllElements() {
        dao.putAll(listOf(node(), createAWay(), createARelation()))

        verify(nodeDao).putAll(anyCollection())
        verify(wayDao).putAll(anyCollection())
        verify(relationDao).putAll(anyCollection())
    }

    @Test fun deleteAllElements() {
        dao.deleteAll(listOf(
            ElementKey(Element.Type.NODE,0),
            ElementKey(Element.Type.WAY,0),
            ElementKey(Element.Type.RELATION,0)
        ))

        verify(nodeDao).deleteAll(listOf(0L))
        verify(wayDao).deleteAll(listOf(0L))
        verify(relationDao).deleteAll(listOf(0L))
    }

    @Test fun getAllElements() {
        dao.getAll(listOf(
            ElementKey(Element.Type.NODE,0),
            ElementKey(Element.Type.WAY,0),
            ElementKey(Element.Type.RELATION,0)
        ))

        verify(nodeDao).getAll(listOf(0L))
        verify(wayDao).getAll(listOf(0L))
        verify(relationDao).getAll(listOf(0L))
    }

    private fun createAWay() = OsmWay(0, 0, listOf(0L), null)

    private fun createARelation() =
        OsmRelation(0, 0, listOf(OsmRelationMember(0, "", Element.Type.NODE)), null)
}
