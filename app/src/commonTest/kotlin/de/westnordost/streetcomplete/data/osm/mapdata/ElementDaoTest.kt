package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.node
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import dev.mokkery.matcher.any
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementDaoTest {
    private lateinit var nodeDao: NodeDao
    private lateinit var wayDao: WayDao
    private lateinit var relationDao: RelationDao
    private lateinit var dao: ElementDao

    @BeforeTest fun setUp() {
        nodeDao = mock()
        wayDao = mock()
        relationDao = mock()
        dao = ElementDao(nodeDao, wayDao, relationDao)
    }

    @Test fun putNode() {
        val node = node(1)
        dao.put(node)
        verify { nodeDao.put(node) }
    }

    @Test fun getNode() {
        dao.get(NODE, 1L)
        verify { nodeDao.get(1L) }
    }

    @Test fun deleteNode() {
        dao.delete(NODE, 1L)
        verify { nodeDao.delete(1L) }
    }

    @Test fun putWay() {
        val way = way()
        dao.put(way)
        verify { wayDao.put(way) }
    }

    @Test fun getWay() {
        dao.get(WAY, 1L)
        verify { wayDao.get(1L) }
    }

    @Test fun deleteWay() {
        dao.delete(WAY, 1L)
        verify { wayDao.delete(1L) }
    }

    @Test fun putRelation() {
        val relation = rel()
        dao.put(relation)
        verify { relationDao.put(relation) }
    }

    @Test fun getRelation() {
        dao.get(RELATION, 1L)
        verify { relationDao.get(1L) }
    }

    @Test fun deleteRelation() {
        dao.delete(RELATION, 1L)
        verify { relationDao.delete(1L) }
    }

    @Test fun putAllRelations() {
        dao.putAll(listOf(rel()))
        verify { relationDao.putAll(any()) }
    }

    @Test fun putAllWays() {
        dao.putAll(listOf(way()))
        verify { wayDao.putAll(any()) }
    }

    @Test fun putAllNodes() {
        dao.putAll(listOf(node()))
        verify { nodeDao.putAll(any()) }
    }

    @Test fun putAllElements() {
        dao.putAll(listOf(node(), way(), rel()))

        verify { nodeDao.putAll(any()) }
        verify { wayDao.putAll(any()) }
        verify { relationDao.putAll(any()) }
    }

    @Test fun deleteAllElements() {
        dao.deleteAll(listOf(
            ElementKey(NODE, 0),
            ElementKey(WAY, 0),
            ElementKey(RELATION, 0)
        ))

        verify { nodeDao.deleteAll(listOf(0L)) }
        verify { wayDao.deleteAll(listOf(0L)) }
        verify { relationDao.deleteAll(listOf(0L)) }
    }

    @Test fun clear() {
        dao.clear()
        verify { nodeDao.clear() }
        verify { wayDao.clear() }
        verify { relationDao.clear() }
    }

    @Test fun getAllElements() {
        val node = node(1L)
        val way = way(2L)
        val rel = rel(3L)
        every { nodeDao.getAll(listOf(1L)) } returns listOf(node)
        every { wayDao.getAll(listOf(2L)) } returns listOf(way)
        every { relationDao.getAll(listOf(3L)) } returns listOf(rel)

        assertEquals(
            listOf(node, way, rel),
            dao.getAll(listOf(
                ElementKey(NODE, 1),
                ElementKey(WAY, 2),
                ElementKey(RELATION, 3)
            ))
        )
    }

    @Test fun getAllElementsByBbox() {
        val bbox = BoundingBox(0.0, 0.0, 1.0, 1.0)
        val nodes = listOf(node(1), node(2), node(3))
        val nodeIds = nodes.map { it.id }
        val ways = listOf(way(1, listOf(1, 2, 4)), way(2, listOf(3, 5)))
        val wayIds = ways.map { it.id }
        val waysNodes = listOf(node(4), node(5))
        val waysNodesIds = waysNodes.map { it.id }
        val relations = listOf(rel(1))

        every { nodeDao.getAll(bbox) } returns nodes
        every { wayDao.getAllForNodes(nodeIds.toSet()) } returns ways
        every { nodeDao.getAll(listOf(4, 5)) } returns waysNodes
        every { relationDao.getAllForElements(
            nodeIds = waysNodesIds + nodeIds,
            wayIds = wayIds,
            relationIds = emptyList()
        ) } returns relations

        assertEquals(
            nodes + waysNodes + ways + relations,
            dao.getAll(bbox)
        )
    }

    @Test fun `getAllElementsByBbox includes nodes that are not in bbox but part of ways contained in bbox`() {
        val bbox = BoundingBox(0.0, 0.0, 1.0, 1.0)
        val bboxNodes = listOf(node(1), node(2), node(3))
        val bboxNodeIds = bboxNodes.map { it.id }
        val outsideBboxNodes = listOf(node(4), node(5))
        val outsideBboxNodeIds = outsideBboxNodes.map { it.id }
        val ways = listOf(way(1), way(2, nodes = listOf(3L, 4L, 5L)))
        val wayIds = ways.map { it.id }
        val relations = listOf(rel(1))

        every { nodeDao.getAll(bbox) } returns bboxNodes
        every { nodeDao.getAll(outsideBboxNodeIds) } returns outsideBboxNodes
        every { wayDao.getAllForNodes(bboxNodeIds.toSet()) } returns ways
        every { relationDao.getAllForElements(
            nodeIds = outsideBboxNodeIds + bboxNodeIds,
            wayIds = wayIds,
            relationIds = emptyList()
        ) } returns relations
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

        every { nodeDao.getAllIds(bbox) } returns nodeIds
        every { wayDao.getAllIdsForNodes(nodeIds) } returns wayIds
        every { relationDao.getAllIdsForElements(
            nodeIds = nodeIds,
            wayIds = wayIds,
            relationIds = emptyList()
        ) } returns relationIds
        assertEquals(
            nodeIds.map { ElementKey(NODE, it) } +
                wayIds.map { ElementKey(WAY, it) } +
                relationIds.map { ElementKey(RELATION, it) },
            dao.getAllKeys(bbox)
        )
    }
}
