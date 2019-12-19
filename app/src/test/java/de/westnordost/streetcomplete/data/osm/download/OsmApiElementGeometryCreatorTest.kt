package de.westnordost.streetcomplete.data.osm.download

import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class OsmApiElementGeometryCreatorTest {

    private lateinit var dao: MapDataDao
    private lateinit var elementCreator: ElementGeometryCreator
    private lateinit var creator: OsmApiElementGeometryCreator

    @Before fun setUp() {
        dao = mock()
        elementCreator = mock()
        creator = OsmApiElementGeometryCreator(dao, elementCreator)
    }

    @Test fun `creates for node`() {
        val node = OsmNode(1L, 1, 2.0, 3.0, null)
        creator.create(node)
        verify(elementCreator).create(node)
    }

    @Test fun `creates for way`() {
        val way = OsmWay(1L, 1, listOf(1,2,3), null)
        val positions = listOf(
            OsmLatLon(1.0, 2.0),
            OsmLatLon(2.0, 4.0),
            OsmLatLon(5.0, 6.0)
        )
        on(dao.getWayComplete(eq(1L), any())).thenAnswer { invocation ->
            val handler = (invocation.arguments[1]) as MapDataHandler
            handler.handle(way)
            way.nodeIds.forEachIndexed { i, nodeId ->
                handler.handle(OsmNode(nodeId, 1, positions[i], null))
            }
            Any()
        }
        creator.create(way)
        verify(elementCreator).create(way, positions)
    }

    @Test fun `returns null for non-existent way`() {
        val way = OsmWay(1L, 1, listOf(1,2,3), null)
        on(dao.getWayComplete(eq(1L), any())).thenThrow(OsmNotFoundException(404, "", ""))
        assertNull(creator.create(way))
        verify(elementCreator, never()).create(eq(way), any())
    }

    @Test fun `creates for relation`() {
        val relation = OsmRelation(1L, 1, listOf(
            OsmRelationMember(1L, "", Element.Type.WAY),
            OsmRelationMember(2L, "", Element.Type.WAY),
            OsmRelationMember(1L, "", Element.Type.NODE)
        ), null)

        val ways = listOf(
            OsmWay(1L, 1, listOf(1,2,3), null),
            OsmWay(2L, 1, listOf(4,5), null)
        )
        val positions = mapOf<Long, List<LatLon>>(
            1L to listOf(
                OsmLatLon(1.0, 2.0),
                OsmLatLon(2.0, 4.0),
                OsmLatLon(5.0, 6.0)
            ),
            2L to listOf(
                OsmLatLon(2.0, 1.0),
                OsmLatLon(0.0, -1.0)
            )
        )
        on(dao.getRelationComplete(eq(1L), any())).thenAnswer { invocation ->
            val handler = (invocation.arguments[1]) as MapDataHandler
            handler.handle(relation)
            for (way in ways) {
                handler.handle(way)
                way.nodeIds.forEachIndexed { i, nodeId ->
                    handler.handle(OsmNode(nodeId, 1, positions[way.id]!![i], null))
                }
            }
            Any()
        }
        creator.create(relation)
        verify(elementCreator).create(relation, positions)
    }

    @Test fun `returns null for non-existent relation`() {
        val relation = OsmRelation(1L, 1, listOf(
            OsmRelationMember(1L, "", Element.Type.WAY),
            OsmRelationMember(2L, "", Element.Type.WAY),
            OsmRelationMember(1L, "", Element.Type.NODE)
        ), null)
        on(dao.getRelationComplete(eq(1L), any())).thenThrow(OsmNotFoundException(404, "", ""))
        assertNull(creator.create(relation))
        verify(elementCreator, never()).create(eq(relation), any())
    }
}
