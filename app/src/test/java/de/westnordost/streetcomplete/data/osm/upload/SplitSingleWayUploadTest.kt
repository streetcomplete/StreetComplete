package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.argumentCaptor
import de.westnordost.streetcomplete.data.osm.changes.SplitAtLinePosition
import de.westnordost.streetcomplete.data.osm.changes.SplitAtPoint
import de.westnordost.streetcomplete.data.osm.changes.SplitPolylineAtPosition
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.util.createTranslated
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class SplitSingleWayUploadTest {
    private val osmDao: MapDataDao = mock()
    private val uploader = SplitSingleWayUpload(osmDao)

    private val p = arrayOf(
        OsmLatLon(0.0, 0.0),
        OsmLatLon(0.0, 1.0),
        OsmLatLon(1.0, 1.0),
        OsmLatLon(1.0, 0.0)
    )
    private val n = arrayOf(
        OsmNode(0, 1, p[0], null),
        OsmNode(1, 1, p[1], null),
        OsmNode(2, 1, p[2], null),
        OsmNode(3, 1, p[3], null)
    )

    private var way = OsmWay(0,1, mutableListOf(0,1,2,3), null)
        set(value) {
            field = value
            on(osmDao.getWay(0)).thenReturn(way)
        }
    private val split = SplitAtLinePosition(p[1], p[2], 0.5)

    @Before fun setUp() {
        reset(osmDao)
        way = OsmWay(0,1, mutableListOf(0,1,2,3), null)
        on(osmDao.getNodes(any())).then { invocation ->
            val nodeIds = invocation.getArgument(0) as List<Long>
            nodeIds.map { nodeId -> n[nodeId.toInt()] }.shuffled()
        }
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf())
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if less than two split positions on closed way`() {
        way = OsmWay(0, 1, mutableListOf(0,1,2,0), null)
        doSplit(SplitAtPoint(p[1]))
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if way was deleted`() {
        on(osmDao.getWay(0)).thenReturn(null)
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if updated way was cut at the start`() {
        on(osmDao.getWay(0)).thenReturn(updatedWayWithNodes(1,2,3))
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if updated way was cut at the end`() {
        on(osmDao.getWay(0)).thenReturn(updatedWayWithNodes(0,1,2))
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if way has split position at its very start`() {
        doSplit(SplitAtPoint(p[0]))
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if way has split position at its very end`() {
        doSplit(SplitAtPoint(p[3]))
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if first split point of line split is not in the way`() {
        way = OsmWay(0,1, mutableListOf(1,2), null)
        doSplit(SplitAtLinePosition(p[0], p[1], 0.5))
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if second split point of line split is not in the way`() {
        way = OsmWay(0,1, mutableListOf(0,1), null)
        doSplit(SplitAtLinePosition(p[1], p[2], 0.5))
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if split point of point split is not in the way`() {
        way = OsmWay(0,1, mutableListOf(1,3), null)
        doSplit(SplitAtPoint(p[2]))
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if the second node is not directly after the first one in the updated way`() {
        way = OsmWay(0,1, mutableListOf(1,3,2), null)
        doSplit(SplitAtLinePosition(p[1], p[2], 0.3))
    }

    @Test(expected = ChangesetConflictException::class)
    fun `raise changeset conflict on conflict of uploadChanges`() {
        on(osmDao.uploadChanges(anyLong(), anyList(), any())).thenThrow(OsmConflictException(409, "jo", "ho"))
        doSplit()
    }

    @Test fun `find node to split at from several alternatives`() {
        way = OsmWay(0, 1, mutableListOf(0,1,2,0,3,0,1), null)
        val elements = doSplit(SplitAtPoint(p[0]))
        assertEquals(3, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(0,1,2,0),
            listOf(0,3,0),
            listOf(0,1)
        )))
    }

    @Test fun `find line to split at from several alternatives`() {
        way = OsmWay(0, 1, mutableListOf(0,1,2,0,3,0,1), null)
        val elements = doSplit(SplitAtLinePosition(p[0],p[3], 0.25))
        assertEquals(3, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(0,1,2,0,-1),
            listOf(-1,3,-2),
            listOf(-2,0,1)
        )))
    }

    @Test fun `the order in which SplitLineAtPosition is defined does not matter`() {
        val elements = doSplit(SplitAtLinePosition(p[2],p[1], 0.5))
        assertEquals(2, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(0,1,-1),
            listOf(-1,2,3)
        )))
    }

    @Test fun `merge last and first chunk for closed ways`() {
        way = OsmWay(0,1, mutableListOf(0,1,2,3,0), null)
        val elements = doSplit(SplitAtPoint(p[1]), SplitAtPoint(p[2]))
        assertEquals(2, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(2,3,0,1),
            listOf(1,2)
        )))
    }

    @Test fun `split way with one split position at vertex`() {
        val elements = doSplit(SplitAtPoint(p[1]))
        assertTrue(elements.nodes.isEmpty()) // no nodes were added
        assertEquals(2, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(0,1),
            listOf(1,2,3)
        )))
    }

    @Test fun `split way with one split position`() {
        val p1 = p[1]
        val p2 = p[2]
        val elements = doSplit(SplitAtLinePosition(p1, p2, 0.5))
        assertEquals(1, elements.nodes.size)
        assertEquals(2, elements.ways.size)
        val node = elements.nodes.single()
        assertEquals(
            createTranslated(
                p1.latitude + 0.5 * (p2.latitude - p1.latitude),
                p1.longitude + 0.5 * (p2.longitude - p1.longitude)),
            node.position
        )

        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(0,1,-1),
            listOf(-1,2,3)
        )))
    }

    @Test fun `split way with several split position at vertices`() {
        // 0   1   2   3
        //     |   |
        val elements = doSplit(SplitAtPoint(p[1]), SplitAtPoint(p[2]))

        assertTrue(elements.nodes.isEmpty()) // no nodes were added
        assertEquals(3, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(0,1),
            listOf(1,2),
            listOf(2,3)
        )))
    }

    @Test fun `split way with multiple split positions`() {
        // 0   1   2   3
        //       |   |
        val elements = doSplit(
            SplitAtLinePosition(p[1], p[2], 0.5),
            SplitAtLinePosition(p[2], p[3], 0.5)
        )

        assertEquals(2, elements.nodes.size)
        assertEquals(3, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(0,1,-1),
            listOf(-1,2,-2),
            listOf(-2,3)
        )))
    }

    @Test fun `split way with multiple split positions, one of which is at vertices`() {
        // 0   1   2   3
        //   | |
        val elements = doSplit(
            SplitAtLinePosition(p[0], p[1], 0.5),
            SplitAtPoint(p[1])
        )

        assertEquals(1, elements.nodes.size)
        assertEquals(3, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(0,-1),
            listOf(-1,1),
            listOf(1,2,3)
        )))
    }

    @Test fun `split way with multiple unordered split positions between the same nodes`() {
        // 0  1  2  3
        //    ||||
        val elements = doSplit(
            SplitAtLinePosition(p[1], p[2], 0.66),
            SplitAtPoint(p[2]),
            SplitAtPoint(p[1]),
            SplitAtLinePosition(p[1], p[2], 0.33)
        )

        assertEquals(2, elements.nodes.size)
        assertEquals(5, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(0,1),
            listOf(1,-1),
            listOf(-1,-2),
            listOf(-2,2),
            listOf(2,3)
        )))
    }

    @Test fun `reuse object id of longest split chunk (= second chunk)`() {
        val elements = doSplit(SplitAtPoint(p[1]))
        assertEquals(way.id, elements.ways.maxBy { it.nodeIds.size }?.id)
    }

    @Test fun `reuse object id of longest split chunk (= first chunk)`() {
        val elements = doSplit(SplitAtPoint(p[2]))
        assertEquals(way.id, elements.ways.maxBy { it.nodeIds.size }?.id)
    }

    @Test fun `insert all way chunks into relation the way is a member of`() {
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(0)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            elements.memberNodeIdsByRelationId[0],
            listOf<List<Long>>(listOf(0,1,-1), listOf(-1,2,3))
        )
    }

    @Test fun `insert all way chunks into multiple relations the way is a member of`() {
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(0)), null),
            OsmRelation(1,1, membersForWays(listOf(0)), null)
        ))
        val elements = doSplit()

        assertEquals(2, elements.relations.size)
        assertEquals(
            elements.memberNodeIdsByRelationId[0],
            listOf<List<Long>>(listOf(0,1,-1), listOf(-1,2,3))
        )
        assertEquals(
            elements.memberNodeIdsByRelationId[1],
            listOf<List<Long>>(listOf(0,1,-1), listOf(-1,2,3))
        )
    }

    @Test fun `insert all way chunks multiple times into relation the way is a member of multiple times`() {
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(0,5,0), null))
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(3,4,3), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(0,1,0,2)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            elements.memberNodeIdsByRelationId[0],
            listOf<List<Long>>(
                listOf(-1,2,3),
                listOf(0,1,-1),
                listOf(0,5,0),
                listOf(0,1,-1),
                listOf(-1,2,3),
                listOf(3,4,3)
            )
        )
    }

    @Test fun `all way chunks in updated relations have the same role as the original way`() {
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(0), "cool role"), null),
            OsmRelation(1,1, membersForWays(listOf(0), "not so cool role"), null)
        ))
        val relationsById = doSplit().relationsById

        assertTrue(relationsById.getValue(0).members.all { it.role == "cool role" })
        assertTrue(relationsById.getValue(1).members.all { it.role == "not so cool role" })
    }

    @Test fun `insert way chunks at correct position in the updated relation`() {
        // 4 5 | 0 1 2 3 | 6 7  => 4 5 | 0 1 -1 | -1 2 3 | 6 7
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(4,5), null))
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(6,7), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(1,0,2)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(4,5), listOf(0,1,-1), listOf(-1,2,3), listOf(6,7)),
            elements.memberNodeIdsByRelationId[0]
        )
    }

    @Test fun `ignore if a way has been deleted when determining way orientation in relation`() {
        /* while determining the orientation of the way in the relation, the neighbouring ways are
           downloaded and analyzed - if they do not exist anymore, this should not lead to a
           nullpointer exception */
        on(osmDao.getWay(1)).thenReturn(null)
        on(osmDao.getWay(2)).thenReturn(null)
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(1,0,2)), null)
        ))
        doSplit()
    }

    @Test fun `insert way chunks backwards in the updated relation as end of reverse chain`() {
        // 4 3 | 0 1 2 3  =>  4 3 | -1 2 3 | 0 1 -1
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(4,3), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(1,0)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(4,3), listOf(-1,2,3), listOf(0,1,-1)),
            elements.memberNodeIdsByRelationId[0]
        )
    }

    @Test fun `ignore non-way relation members when determining way orientation in relation`() {
        // 4 3 | 0 1 2 3  =>  4 3 | -1 2 3 | 0 1 -1
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(4,3), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, mutableListOf<RelationMember>(
                OsmRelationMember(1, "", WAY),
                OsmRelationMember(0, "", NODE),
                OsmRelationMember(1, "", RELATION),
                OsmRelationMember(0, "", WAY),
                OsmRelationMember(0, "", NODE),
                OsmRelationMember(1, "", RELATION)
            ), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            elements.memberNodeIdsByRelationId[0],
            listOf<List<Long>>(listOf(4,3), listOf(-1,2,3), listOf(0,1,-1))
        )
    }

    @Test fun `insert way chunks forwards in the updated relation as end of chain`() {
        // 4 0 | 0 1 2 3  =>  4 0 | 0 1 -1 | -1 2 3
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(4,0), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(1,0)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            elements.memberNodeIdsByRelationId[0],
            listOf<List<Long>>(listOf(4,0), listOf(0,1,-1), listOf(-1,2,3))
        )
    }

    @Test fun `insert way chunks backwards in the updated relation as start of reverse chain`() {
        // 0 1 2 3 | 4 0  =>  -1 2 3 | 0 1 -1 | 4 0
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(4,0), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(0,1)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            elements.memberNodeIdsByRelationId[0],
            listOf<List<Long>>(listOf(-1,2,3), listOf(0,1,-1), listOf(4,0))
        )
    }

    @Test fun `insert way chunks forwards in the updated relation as start of chain`() {
        // 0 1 2 3 | 4 3  =>  0 1 -1 | -1 2 3 | 4 3
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(4,3), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, membersForWays(listOf(0,1)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            elements.memberNodeIdsByRelationId[0],
            listOf<List<Long>>(listOf(0,1,-1), listOf(-1,2,3), listOf(4,3))
        )
    }

    @Test fun `update a restriction relation with split from-way and via node`() {
        `update a restriction-like relation with split-way and via node`("restriction", "via", "from")
    }

    @Test fun `update a restriction relation with split to-way and via node`() {
        `update a restriction-like relation with split-way and via node`("restriction", "via", "to")
    }

    private fun `update a restriction-like relation with split-way and via node`(
        relationType: String, via: String, role: String) {
        val otherRole = if (role == "from") "to" else "from"
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(3,4), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, mutableListOf<RelationMember>(
                OsmRelationMember(0, role, WAY),
                OsmRelationMember(1, otherRole, WAY),
                OsmRelationMember(3, via, NODE)
            ), mapOf("type" to relationType))
        ))
        val elements = doSplit()

        val relation = elements.relations.single()
        assertEquals(3, relation.members.size)

        val relationMember = relation.members[0]!!
        val newWay = elements.waysById.getValue(relationMember.ref)
        assertEquals(listOf<Long>(-1,2,3), newWay.nodeIds.toList())
        assertEquals(role, relationMember.role)
        assertEquals(WAY, relationMember.type)
    }

    @Test fun `update a restriction relation with split from-way and via way`() {
        `update a restriction-like relation with split-way and via way`("restriction", "via", "from")
    }

    @Test fun `update a restriction relation with split to-way and via way`() {
        `update a restriction-like relation with split-way and via way`("restriction", "via", "to")
    }

    @Test fun `update a restriction-like relation with another type`() {
        `update a restriction-like relation with split-way and via node`("some_weird_restriction", "via", "from")
    }

    private fun `update a restriction-like relation with split-way and via way`(
        relationType: String, via: String, role: String) {
        val otherRole = if (role == "from") "to" else "from"
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(5,7), null))
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(5,4,3), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, mutableListOf<RelationMember>(
                OsmRelationMember(0, role, WAY),
                OsmRelationMember(1, otherRole, WAY),
                OsmRelationMember(2, via, WAY)
            ), mapOf("type" to relationType))
        ))
        val elements = doSplit()

        val relation = elements.relations.single()
        assertEquals(3, relation.members.size)

        val fromRelationMember = relation.members[0]!!
        val fromWay = elements.waysById.getValue(fromRelationMember.ref)
        assertEquals(listOf<Long>(-1,2,3), fromWay.nodeIds.toList())
        assertEquals(role, fromRelationMember.role)
        assertEquals(WAY, fromRelationMember.type)
    }

    @Test fun `update a restriction-like relation with split-way and multiple via ways`() {
        on(osmDao.getWay(0)).thenReturn(OsmWay(0, 1, listOf(0,1,2,3), null))
        on(osmDao.getWay(1)).thenReturn(OsmWay(1, 1, listOf(6,7), null))
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(4,5,6), null))
        on(osmDao.getWay(3)).thenReturn(OsmWay(3, 1, listOf(3,4), null))
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, mutableListOf<RelationMember>(
                OsmRelationMember(0, "from", WAY),
                OsmRelationMember(1, "to", WAY),
                OsmRelationMember(2, "via", WAY),
                OsmRelationMember(3, "via", WAY)
            ), mapOf("type" to "restriction"))
        ))
        val elements = doSplit(SplitAtPoint(p[2]))

        val relation = elements.relations.single()
        assertEquals(4, relation.members.size)

        val fromRelationMember = relation.members[0]!!
        val fromWay = elements.waysById.getValue(fromRelationMember.ref)
        assertEquals(listOf<Long>(2,3), fromWay.nodeIds.toList())
        assertEquals("from", fromRelationMember.role)
        assertEquals(WAY, fromRelationMember.type)
    }

    @Test fun `no special treatment of restriction relation if the way has another role`() {
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, mutableListOf<RelationMember>(
                OsmRelationMember(0, "another role", WAY),
                OsmRelationMember(1, "from", WAY),
                OsmRelationMember(3, "via", NODE),
                OsmRelationMember(3, "to", WAY)
            ), mapOf("type" to "restriction"))
        ))
        val elements = doSplit()

        val relation = elements.relations.single()
        assertEquals(5, relation.members.size)
    }

    @Test fun `no special treatment of restriction relation if there is no via`() {
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, mutableListOf<RelationMember>(
                OsmRelationMember(0, "from", WAY),
                OsmRelationMember(1, "to", WAY)
            ), mapOf("type" to "restriction"))
        ))
        val elements = doSplit()

        val relation = elements.relations.single()
        assertEquals(3, relation.members.size)
    }

    @Test fun `no special treatment of restriction relation if from-way does not touch via`() {
        on(osmDao.getRelationsForWay(0)).thenReturn(listOf(
            OsmRelation(0,1, mutableListOf<RelationMember>(
                OsmRelationMember(0, "from", WAY),
                OsmRelationMember(4, "via", NODE),
                OsmRelationMember(3, "to", WAY)
            ), mapOf("type" to "restriction"))
        ))
        val elements = doSplit()

        val relation = elements.relations.single()
        assertEquals(4, relation.members.size)
    }

    @Test fun `update a destination sign relation with split to-way and intersection node`() {
        `update a restriction-like relation with split-way and via node`("destination_sign", "intersection", "to")
    }

    @Test fun `update a destination sign relation with split to-way and sign node`() {
        `update a restriction-like relation with split-way and via node`("destination_sign", "sign", "to")
    }

    @Test fun `update a destination sign relation with split from-way and intersection node`() {
        `update a restriction-like relation with split-way and via node`("destination_sign", "intersection", "from")
    }

    @Test fun `update a destination sign relation with split from-way and sign node`() {
        `update a restriction-like relation with split-way and via node`("destination_sign", "sign", "from")
    }

    private fun doSplit(vararg splits: SplitPolylineAtPosition = arrayOf(split)) : Elements {
        uploader.upload(0, way, splits.asList())
        val arg: ArgumentCaptor<Iterable<Element>> = argumentCaptor()
        verify(osmDao).uploadChanges(eq(0), arg.capture(), any())
        val elements = arg.value.toList()
        for (element in elements) {
            assertTrue(element.isModified || element.isNew )
        }
        return Elements(
            elements.mapNotNull { it as? Node },
            elements.mapNotNull { it as? Way },
            elements.mapNotNull { it as? Relation }
        )
    }

    private fun updatedWayWithNodes(vararg nodes: Long) = OsmWay(0, 2, nodes.asList(), null)

    private fun membersForWays(ids: List<Long>, role: String = ""): List<RelationMember> =
        ids.map { id -> OsmRelationMember(id, role, WAY) }.toMutableList()

    private inner class Elements(
        val nodes: List<Node>,
        val ways: List<Way>,
        val relations: List<Relation>
    ) {
        val waysNodeIds get() = ways.map { it.nodeIds }

        val waysById get() = ways.associateBy { it.id }
        val relationsById get() = relations.associateBy { it.id }

        val memberNodeIdsByRelationId: Map<Long, List<List<Long>>> get() =
            relations.associate { relation ->
                relation.id to relation.members.mapNotNull { member ->
                    if (member.type != WAY) null
                    else (waysById[member.ref] ?: osmDao.getWay(member.ref)).nodeIds.toList()
                }
            }
    }
}
