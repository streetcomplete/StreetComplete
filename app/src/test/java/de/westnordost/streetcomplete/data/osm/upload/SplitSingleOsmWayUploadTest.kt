package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.argumentCaptor
import de.westnordost.streetcomplete.data.osm.changes.SplitWayAtPosition
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.util.SphericalEarthMath
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class SplitSingleOsmWayUploadTest {
    private val uploader: SplitSingleOsmWayUpload
    private val osmDao: MapDataDao

    private val node1 = OsmNode(1, 1, 0.0, 0.0, null)
    private val node2 = OsmNode(2, 1, 0.0, 1.0, null)
    private val node3 = OsmNode(3, 1, 1.0, 1.0, null)
    private val node4 = OsmNode(4, 1, 1.0, 0.0, null)
    private var way = OsmWay(1,1, mutableListOf(1,2,3,4), null)
        set(value) {
            field = value
            on(osmDao.getWay(1)).thenReturn(way)
        }
    private val split =
        SplitWayAtPosition(way, node2, node3, 0.5)

    init {
        osmDao = mock(MapDataDao::class.java)
        uploader = SplitSingleOsmWayUpload(osmDao)
    }

    @Before fun setUp() {
        way = OsmWay(1,1, mutableListOf(1,2,3,4), null)
        reset(osmDao)
        on(osmDao.getWay(1)).thenReturn(way)
        on(osmDao.getNode(1)).thenReturn(node1)
        on(osmDao.getNode(2)).thenReturn(node2)
        on(osmDao.getNode(3)).thenReturn(node3)
        on(osmDao.getNode(4)).thenReturn(node4)
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throw if less than two split positions on closed way`() {
        way = OsmWay(1, 1, mutableListOf(1,2,3,1), null)
        doSplit(SplitWayAtPosition(way, node1, node2, 0.5))
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if way was deleted`() {
        on(osmDao.getWay(1)).thenReturn(null)
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if updated way was cut at the start`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(2,3,4))
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if updated way was cut at the end`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(1,2,3))
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if updated way has split position at its very start`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(2,3,4))
        doSplit(SplitWayAtPosition(way, node2, node3, 0.0))
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if first node is not in the updated way`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(1,3,4))
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if second node is not in the updated way`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(1,2,4))
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if the second node is not directly after the first one in the updated way`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(1,3,2,4))
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if first node of split position was deleted`() {
        on(osmDao.getNode(2)).thenReturn(null)
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if second node of split position was deleted`() {
        on(osmDao.getNode(3)).thenReturn(null)
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if first node of split position has been moved`() {
        on(osmDao.getNode(2)).thenReturn(OsmNode(2, 2, 0.333, 0.333, null))
        doSplit()
    }

    @Test(expected = ElementConflictException::class)
    fun `raise conflict if second node of split position has been moved`() {
        on(osmDao.getNode(3)).thenReturn(OsmNode(3, 2, 0.333, 0.333, null))
        doSplit()
    }

    @Test fun `merge last and first chunk for closed ways`() {
        way = OsmWay(1,1, mutableListOf(1,2,3,4,1), null)
        val elements = doSplit(
            SplitWayAtPosition(way, node2, node3, 0.0),
            SplitWayAtPosition(way, node3, node4, 0.0)
        )
        assertEquals(2, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(3,4,1,2),
            listOf(2,3)
        )))
    }

    @Test fun `split way with one split position at vertex`() {
        val elements = doSplit(SplitWayAtPosition(way, node2, node3, 0.0))
        assertTrue(elements.nodes.isEmpty()) // no nodes were added
        assertEquals(2, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(1,2),
            listOf(2,3,4)
        )))
    }

    @Test fun `split way with one split position`() {
        val elements = doSplit(SplitWayAtPosition(way, node2, node3, 0.5))
        assertEquals(1, elements.nodes.size)
        assertEquals(2, elements.ways.size)
        val node = elements.nodes.single()
        val p1 = node2.position
        val p2 = node3.position
        assertEquals(
            SphericalEarthMath.createTranslated(
                p1.latitude + 0.5 * (p2.latitude - p1.latitude),
                p1.longitude + 0.5 * (p2.longitude - p1.longitude)),
            node.position
        )

        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(1,2,-1),
            listOf(-1,3,4)
        )))
    }

    @Test fun `split way with several split position at vertices`() {
        // 1   2   3   4
        //     |   |
        val elements = doSplit(
            SplitWayAtPosition(way, node2, node3, 0.0),
            SplitWayAtPosition(way, node3, node4, 0.0)
        )

        assertTrue(elements.nodes.isEmpty()) // no nodes were added
        assertEquals(3, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(1,2),
            listOf(2,3),
            listOf(3,4)
        )))
    }

    @Test fun `split way with multiple split positions`() {
        // 1   2   3   4
        //       |   |
        val elements = doSplit(
            SplitWayAtPosition(way, node2, node3, 0.5),
            SplitWayAtPosition(way, node3, node4, 0.5)
        )

        assertEquals(2, elements.nodes.size)
        assertEquals(3, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(1,2,-1),
            listOf(-1,3,-2),
            listOf(-2,4)
        )))
    }

    @Test fun `split way with multiple split positions, one of which is at vertices`() {
        // 1   2   3   4
        //   | |
        val elements = doSplit(
            SplitWayAtPosition(way, node1, node2, 0.5),
            SplitWayAtPosition(way, node2, node3, 0.0)
        )

        assertEquals(1, elements.nodes.size)
        assertEquals(3, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(1,-1),
            listOf(-1,2),
            listOf(2,3,4)
        )))
    }

    @Test fun `split way with multiple unordered split positions between the same nodes`() {
        // 1  2  3  4
        //    ||||
        val elements = doSplit(
            SplitWayAtPosition(way, node2, node3, 0.66),
            SplitWayAtPosition(way, node3, node4, 0.0),
            SplitWayAtPosition(way, node2, node3, 0.0),
            SplitWayAtPosition(way, node2, node3, 0.33)
        )

        assertEquals(2, elements.nodes.size)
        assertEquals(5, elements.ways.size)
        assertTrue(elements.waysNodeIds.containsAll(listOf<List<Long>>(
            listOf(1,2),
            listOf(2,-1),
            listOf(-1,-2),
            listOf(-2,3),
            listOf(3,4)
        )))
    }

    @Test fun `reuse object id of longest split chunk (= second chunk)`() {
        val elements = doSplit(SplitWayAtPosition(way, node2, node3, 0.0))
        assertEquals(way.id, elements.ways.maxBy { it.nodeIds.size }?.id)
    }

    @Test fun `reuse object id of longest split chunk (= first chunk)`() {
        val elements = doSplit(SplitWayAtPosition(way, node3, node4, 0.0))
        assertEquals(way.id, elements.ways.maxBy { it.nodeIds.size }?.id)
    }

    @Test fun `insert all way chunks into relation the way is a member of`() {
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(1)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(1,2,-1), listOf(-1,3,4)),
            elements.memberNodeIdsByRelationId[1]
        )
    }

    @Test fun `insert all way chunks into multiple relations the way is a member of`() {
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(1)), null),
            OsmRelation(2,1, membersForWays(listOf(1)), null)
        ))
        val elements = doSplit()

        assertEquals(2, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(1,2,-1), listOf(-1,3,4)),
            elements.memberNodeIdsByRelationId[1]
        )
        assertEquals(
            listOf<List<Long>>(listOf(1,2,-1), listOf(-1,3,4)),
            elements.memberNodeIdsByRelationId[2]
        )
    }

    @Test fun `insert all way chunks multiple times into relation the way is a member of multiple times`() {
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(1,6,1), null))
        on(osmDao.getWay(3)).thenReturn(OsmWay(3, 1, listOf(4,5,4), null))
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(1,2,1,3)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(
                listOf(-1,3,4),
                listOf(1,2,-1),
                listOf(1,6,1),
                listOf(1,2,-1),
                listOf(-1,3,4),
                listOf(4,5,4)
            ),
            elements.memberNodeIdsByRelationId[1]
        )
    }

    @Test fun `all way chunks in updated relations have the same role as the original way`() {
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(1), "cool role"), null),
            OsmRelation(2,1, membersForWays(listOf(1), "not so cool role"), null)
        ))
        val relationsById = doSplit().relationsById

        assertTrue(relationsById.getValue(1).members.all { it.role == "cool role" })
        assertTrue(relationsById.getValue(2).members.all { it.role == "not so cool role" })
    }

    @Test fun `insert way chunks at correct position in the updated relation`() {
        // 5 6 | 1 2 3 4 | 7 8  => 5 6 | 1 2 -1 | -1 3 4 | 7 8
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(5,6), null))
        on(osmDao.getWay(3)).thenReturn(OsmWay(3, 1, listOf(7,8), null))
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(2,1,3)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(5,6), listOf(1,2,-1), listOf(-1,3,4), listOf(7,8)),
            elements.memberNodeIdsByRelationId[1]
        )
    }

    @Test fun `ignore if a way has been deleted when determining way orientation in relation`() {
        /* while determining the orientation of the way in the relation, the neighbouring ways are
           downloaded and analyzed - if they do not exist anymore, this should not lead to a
           nullpointer exception */
        on(osmDao.getWay(2)).thenReturn(null)
        on(osmDao.getWay(3)).thenReturn(null)
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(2,1,3)), null)
        ))
        doSplit()
    }

    @Test fun `insert way chunks backwards in the updated relation as end of reverse chain`() {
        // 5 4 | 1 2 3 4  =>  5 4 | -1 3 4 | 1 2 -1
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(5,4), null))
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(2,1)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(5,4), listOf(-1,3,4), listOf(1,2,-1)),
            elements.memberNodeIdsByRelationId[1]
        )
    }

    @Test fun `ignore non-way relation members when determining way orientation in relation`() {
        // 5 4 | 1 2 3 4  =>  5 4 | -1 3 4 | 1 2 -1
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(5,4), null))
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, mutableListOf<RelationMember>(
                OsmRelationMember(2, "", WAY),
                OsmRelationMember(1, "", NODE),
                OsmRelationMember(2, "", RELATION),
                OsmRelationMember(1, "", WAY),
                OsmRelationMember(1, "", NODE),
                OsmRelationMember(2, "", RELATION)
            ), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(5,4), listOf(-1,3,4), listOf(1,2,-1)),
            elements.memberNodeIdsByRelationId[1]
        )
    }

    @Test fun `insert way chunks forwards in the updated relation as end of chain`() {
        // 5 1 | 1 2 3 4  =>  5 1 | 1 2 -1 | -1 3 4
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(5,1), null))
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(2,1)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(5,1), listOf(1,2,-1), listOf(-1,3,4)),
            elements.memberNodeIdsByRelationId[1]
        )
    }

    @Test fun `insert way chunks backwards in the updated relation as start of reverse chain`() {
        // 1 2 3 4 | 5 1  =>  -1 3 4 | 1 2 -1 | 5 1
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(5,1), null))
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(1,2)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(-1,3,4), listOf(1,2,-1), listOf(5,1)),
            elements.memberNodeIdsByRelationId[1]
        )
    }

    @Test fun `insert way chunks forwards in the updated relation as start of chain`() {
        // 1 2 3 4 | 5 4  =>  1 2 -1 | -1 3 4 | 5 4
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(5,4), null))
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, membersForWays(listOf(1,2)), null)
        ))
        val elements = doSplit()

        assertEquals(1, elements.relations.size)
        assertEquals(
            listOf<List<Long>>(listOf(1,2,-1), listOf(-1,3,4), listOf(5,4)),
            elements.memberNodeIdsByRelationId[1]
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
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(4,5), null))
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, mutableListOf<RelationMember>(
                OsmRelationMember(1, role, WAY),
                OsmRelationMember(2, otherRole, WAY),
                OsmRelationMember(4, via, NODE)
            ), mapOf("type" to relationType))
        ))
        val elements = doSplit()

        val relation = elements.relations.single()
        assertEquals(3, relation.members.size)

        val relationMember = relation.members[0]!!
        val newWay = elements.waysById.getValue(relationMember.ref)
        assertEquals(listOf<Long>(-1,3,4), newWay.nodeIds.toList())
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
        on(osmDao.getWay(2)).thenReturn(OsmWay(2, 1, listOf(6,8), null))
        on(osmDao.getWay(3)).thenReturn(OsmWay(2, 1, listOf(6,5,4), null))
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, mutableListOf<RelationMember>(
                OsmRelationMember(1, role, WAY),
                OsmRelationMember(2, otherRole, WAY),
                OsmRelationMember(3, via, WAY)
            ), mapOf("type" to relationType))
        ))
        val elements = doSplit()

        val relation = elements.relations.single()
        assertEquals(3, relation.members.size)

        val fromRelationMember = relation.members[0]!!
        val fromWay = elements.waysById.getValue(fromRelationMember.ref)
        assertEquals(listOf<Long>(-1,3,4), fromWay.nodeIds.toList())
        assertEquals(role, fromRelationMember.role)
        assertEquals(WAY, fromRelationMember.type)
    }

    @Test fun `no special treatment of restriction relation if the way has another role`() {
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, mutableListOf<RelationMember>(
                OsmRelationMember(1, "another role", WAY),
                OsmRelationMember(2, "from", WAY),
                OsmRelationMember(4, "via", NODE),
                OsmRelationMember(4, "to", WAY)
            ), mapOf("type" to "restriction"))
        ))
        val elements = doSplit()

        val relation = elements.relations.single()
        assertEquals(5, relation.members.size)
    }

    @Test fun `no special treatment of restriction relation if there is no via`() {
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, mutableListOf<RelationMember>(
                OsmRelationMember(1, "from", WAY),
                OsmRelationMember(2, "to", WAY)
            ), mapOf("type" to "restriction"))
        ))
        val elements = doSplit()

        val relation = elements.relations.single()
        assertEquals(3, relation.members.size)
    }

    @Test fun `no special treatment of restriction relation if from-way does not touch via`() {
        on(osmDao.getRelationsForWay(1)).thenReturn(listOf(
            OsmRelation(1,1, mutableListOf<RelationMember>(
                OsmRelationMember(1, "from", WAY),
                OsmRelationMember(5, "via", NODE),
                OsmRelationMember(2, "to", WAY)
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

    private fun doSplit(vararg splits: SplitWayAtPosition = arrayOf(split)) : Elements {
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

    private fun wayWithNodes(vararg nodes: Long) = OsmWay(1, 2, nodes.asList(), null)

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
