package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import de.westnordost.streetcomplete.testutils.way
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.eq
import io.mockative.every
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementDaoTest {
    @Mock private lateinit var nodeDao: NodeDao
    @Mock private lateinit var wayDao: WayDao
    @Mock private lateinit var relationDao: RelationDao
    @Mock private lateinit var dao: ElementDao

    @BeforeTest fun setUp() {
        nodeDao = mock(classOf<NodeDao>())
        wayDao = mock(classOf<WayDao>())
        relationDao = mock(classOf<RelationDao>())
        dao = ElementDao(nodeDao, wayDao, relationDao)
    }

    @Test fun putNode() {
        val node = node(1)
        dao.put(node)
        verifyInvokedExactlyOnce { nodeDao.put(node) }
    }

    @Test fun getNode() {
        every { nodeDao.get(1L) }.returns(node())
        dao.get(NODE, 1L)
        verifyInvokedExactlyOnce { nodeDao.get(1L) }
    }

    @Test fun deleteNode() {
        every { nodeDao.delete(1L) }.returns(true)
        dao.delete(NODE, 1L)
        verifyInvokedExactlyOnce { nodeDao.delete(1L) }
    }

    @Test fun putWay() {
        val way = way()
        dao.put(way)
        verifyInvokedExactlyOnce { wayDao.put(way) }
    }

    @Test fun getWay() {
        every { wayDao.get(1L) }.returns(way())
        dao.get(WAY, 1L)
        verifyInvokedExactlyOnce { wayDao.get(1L) }
    }

    @Test fun deleteWay() {
        every { wayDao.delete(1L) }.returns(true)
        dao.delete(WAY, 1L)
        verifyInvokedExactlyOnce { wayDao.delete(1L) }
    }

    @Test fun putRelation() {
        val relation = rel()
        dao.put(relation)
        verifyInvokedExactlyOnce { relationDao.put(relation) }
    }

    @Test fun getRelation() {
        every { relationDao.get(1L) }.returns(rel())
        dao.get(RELATION, 1L)
        verifyInvokedExactlyOnce { relationDao.get(1L) }
    }

    @Test fun deleteRelation() {
        every { relationDao.delete(1L) }.returns(true)
        dao.delete(RELATION, 1L)
        verifyInvokedExactlyOnce { relationDao.delete(1L) }
    }

    @Test fun putAllRelations() {
        dao.putAll(listOf(rel()))
        verifyInvokedExactlyOnce { relationDao.putAll(any()) }
    }

    @Test fun putAllWays() {
        dao.putAll(listOf(way()))
        verifyInvokedExactlyOnce { wayDao.putAll(any()) }
    }

    @Test fun putAllNodes() {
        dao.putAll(listOf(node()))
        verifyInvokedExactlyOnce { nodeDao.putAll(any()) }
    }

    @Test fun putAllElements() {
        dao.putAll(listOf(node(), way(), rel()))

        verifyInvokedExactlyOnce { nodeDao.putAll(any()) }
        verifyInvokedExactlyOnce { wayDao.putAll(any()) }
        verifyInvokedExactlyOnce { relationDao.putAll(any()) }
    }

    @Test fun deleteAllElements() {
        every { nodeDao.deleteAll(listOf(0L)) }.returns(1)
        every { wayDao.deleteAll(listOf(0L)) }.returns(1)
        every { relationDao.deleteAll(listOf(0L)) }.returns(1)

        dao.deleteAll(listOf(
            ElementKey(NODE, 0),
            ElementKey(WAY, 0),
            ElementKey(RELATION, 0)
        ))

        verifyInvokedExactlyOnce { nodeDao.deleteAll(listOf(0L)) }
        verifyInvokedExactlyOnce { wayDao.deleteAll(listOf(0L)) }
        verifyInvokedExactlyOnce { relationDao.deleteAll(listOf(0L)) }
    }

    @Test fun clear() {
        dao.clear()
        verifyInvokedExactlyOnce { nodeDao.clear() }
        verifyInvokedExactlyOnce { wayDao.clear() }
        verifyInvokedExactlyOnce { relationDao.clear() }
    }

    @Test fun getAllElements() {
        every { nodeDao.getAll(listOf(0L)) }.returns(listOf(node()))
        every { wayDao.getAll(listOf(0L)) }.returns(listOf(way()))
        every { relationDao.getAll(listOf(0L)) }.returns(listOf(rel()))

        dao.getAll(listOf(
            ElementKey(NODE, 0),
            ElementKey(WAY, 0),
            ElementKey(RELATION, 0)
        ))

        verifyInvokedExactlyOnce { nodeDao.getAll(listOf(0L)) }
        verifyInvokedExactlyOnce { wayDao.getAll(listOf(0L)) }
        verifyInvokedExactlyOnce { relationDao.getAll(listOf(0L)) }
    }

    @Test fun getAllElementsByBbox() {
        val bbox = BoundingBox(0.0, 0.0, 1.0, 1.0)
        val nodes = listOf(node(1), node(2), node(3))
        val nodeIds = nodes.map { it.id }
        val ways = listOf(way(1), way(2))
        val wayIds = ways.map { it.id }
        val relations = listOf(rel(1))

        every { nodeDao.getAll(bbox) }.returns(nodes)
        every { nodeDao.getAll(eq(listOf())) }.returns(listOf())
        every { wayDao.getAllForNodes(eq(nodeIds.toSet())) }.returns(ways)
        every { relationDao.getAllForElements(
            nodeIds = eq(nodeIds),
            wayIds = eq(wayIds),
            relationIds = eq(emptyList())
        )}.returns(relations)
        assertEquals(
            nodes + ways + relations,
            dao.getAll(bbox)
        )
    }

    @Test fun `getAllElementsByBbox includes nodes that are not in bbox, but part of ways contained in bbox`() {
        val bbox = BoundingBox(0.0, 0.0, 1.0, 1.0)
        val bboxNodes = listOf(node(1), node(2), node(3))
        val bboxNodeIds = bboxNodes.map { it.id }
        val outsideBboxNodes = listOf(node(4), node(5))
        val outsideBboxNodeIds = outsideBboxNodes.map { it.id }
        val ways = listOf(way(1), way(2, nodes = listOf(3L, 4L, 5L)))
        val wayIds = ways.map { it.id }
        val relations = listOf(rel(1))

        every { nodeDao.getAll(bbox) }.returns(bboxNodes)
        every { nodeDao.getAll(outsideBboxNodeIds) }.returns(outsideBboxNodes)
        every { wayDao.getAllForNodes(eq(bboxNodeIds.toSet())) }.returns(ways)
        every { relationDao.getAllForElements(
            nodeIds = eq(outsideBboxNodeIds + bboxNodeIds),
            wayIds = eq(wayIds),
            relationIds = eq(emptyList())
        )}.returns(relations)
        assertEquals(
            bboxNodes + outsideBboxNodes + ways + relations,
            dao.getAll(bbox)
        )
    }

    @Test fun getAllElementKeysByBbox() {
        val bbox = BoundingBox(0.0, 0.0, 1.0, 1.0)
        val nodeIds = listOf<Long>(1, 2, 3)
        val wayIds = listOf<Long>(1, 2)
        val relationIds = listOf<Long>(1)

        every { nodeDao.getAllIds(bbox) }.returns(nodeIds)
        every { wayDao.getAllIdsForNodes(eq(nodeIds)) }.returns(wayIds)
        every { relationDao.getAllIdsForElements(
            nodeIds = eq(nodeIds),
            wayIds = eq(wayIds),
            relationIds = eq(emptyList())
        )}.returns(relationIds)
        assertEquals(
            nodeIds.map { ElementKey(NODE, it) } +
                wayIds.map { ElementKey(WAY, it) } +
                relationIds.map { ElementKey(RELATION, it) },
            dao.getAllKeys(bbox)
        )
    }
}
