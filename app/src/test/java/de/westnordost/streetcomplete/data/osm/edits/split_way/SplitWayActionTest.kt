package de.westnordost.streetcomplete.data.osm.edits.split_way

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapData
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.testutils.waysAsMembers
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.util.math.createTranslated
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.reset

class SplitWayActionTest {

    private val repos: MapDataRepository = mock()

    private val p = arrayOf(
        p(0.0, 0.0),
        p(0.0, 1.0),
        p(1.0, 1.0),
        p(1.0, 0.0)
    )
    private val n = arrayOf(
        node(0, p[0]),
        node(1, p[1]),
        node(2, p[2]),
        node(3, p[3])
    )

    private val outsidePoints = arrayOf(
        p(5.0, 0.0),
        p(6.0, 1.0)
    )

    private var way = way(0, mutableListOf(0, 1, 2, 3))
        set(value) {
            field = value
            updateRepos(value)
        }
    private val split = SplitAtLinePosition(p[1], p[2], 0.5)

    private fun updateRepos(way: Way) {
        // let the repos return this way and all its nodes on getWayComplete
        on(repos.getWayComplete(0))
            .thenReturn(MutableMapData(way.nodeIds.map { nodeId -> n[nodeId.toInt()] }.shuffled() + way))
        on(repos.getRelationsForWay(0)).thenReturn(listOf())
    }

    @Before fun setUp() {
        reset(repos)
        updateRepos(way)
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if less than two split positions on closed way`() {
        way = way(0, mutableListOf(0, 1, 2, 0))
        doSplit(SplitAtPoint(p[1]))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if way was deleted`() {
        on(repos.getWayComplete(0)).thenReturn(null)
        doSplit(SplitAtPoint(p[1]))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if updated way was cut at the start`() {
        way = way(0, mutableListOf(1, 2, 3))
        val originalWay = way(0, mutableListOf(0, 1, 2, 3))
        doSplit(split, originalWay = originalWay)
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if updated way was cut at the end`() {
        way = way(0, mutableListOf(0, 1, 2))
        val originalWay = way(0, mutableListOf(0, 1, 2, 3))
        doSplit(split, originalWay = originalWay)
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if way has split position at its very start`() {
        doSplit(SplitAtPoint(p[0]))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if way has split position at its very end`() {
        doSplit(SplitAtPoint(p[3]))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if first split point of line split is not in the way`() {
        doSplit(SplitAtLinePosition(outsidePoints[0], p[1], 0.5))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if second split point of line split is not in the way`() {
        doSplit(SplitAtLinePosition(p[1], outsidePoints[0], 0.5))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if split point of point split is not in the way`() {
        doSplit(SplitAtPoint(outsidePoints[0]))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if the second node is not directly after the first one in the updated way`() {
        doSplit(SplitAtLinePosition(p[0], p[2], 0.3))
    }

    @Test fun `find node to split at from several alternatives`() {
        way = way(0, mutableListOf(0, 1, 2, 0, 3, 0, 1))
        val data = doSplit(SplitAtPoint(p[0]))
        data.checkWaysNodes(
            listOf(0, 1, 2, 0),
            listOf(0, 3, 0, 1)
        )
    }

    @Test fun `find line to split at from several alternatives`() {
        way = way(0, mutableListOf(0, 1, 2, 0, 3, 0, 1))
        val data = doSplit(SplitAtLinePosition(p[0], p[3], 0.25))
        data.checkWaysNodes(
            listOf(0, 1, 2, 0, -1),
            listOf(-1, 3, 0, 1)
        )
    }

    @Test fun `the order in which SplitLineAtPosition is defined does not matter`() {
        val data = doSplit(SplitAtLinePosition(p[2], p[1], 0.5))
        data.checkWaysNodes(
            listOf(0, 1, -1),
            listOf(-1, 2, 3)
        )
    }

    @Test fun `merge last and first chunk for closed ways`() {
        way = way(0, mutableListOf(0, 1, 2, 3, 0))
        val data = doSplit(SplitAtPoint(p[1]), SplitAtPoint(p[2]))
        data.checkWaysNodes(
            listOf(2, 3, 0, 1),
            listOf(1, 2)
        )
    }

    @Test fun `split way copies tags to all resulting elements`() {
        val tags = mapOf(
            "highway" to "residential",
            "surface" to "asphalt"
        )
        way = way(0, mutableListOf(0, 1, 2, 3), tags)

        val ways = doSplit(SplitAtPoint(p[1])).ways
        for (way in ways) {
            assertEquals(tags, way.tags)
        }
    }

    @Test fun `split way deletes tags that may be wrong after split`() {
        val tags = mapOf(
            "seats" to "55",
            "step_count" to "12",
            "steps" to "4",
            "capacity" to "33",
            "parking:lane:both:capacity" to "5",
            "parking:lane:both:capacity:disabled" to "1",
            "capacity:fat_persons" to "1",
            "incline" to "5.1%"
        )
        way = way(0, mutableListOf(0, 1, 2, 3), tags)

        val ways = doSplit(SplitAtPoint(p[1])).ways
        for (way in ways) {
            assertEquals(mapOf<String, String>(), way.tags)
        }
    }

    @Test fun `split way does not delete tags that may be wrong after split under certain conditions`() {
        val tags = mapOf(
            "capacityaspartofaname:yes" to "ok",
            "aspartofanamecapacity:yes" to "ok",
            "steps" to "yes",
            "incline" to "up"
        )
        way = way(0, mutableListOf(0, 1, 2, 3), tags)

        val ways = doSplit(SplitAtPoint(p[1])).ways
        for (way in ways) {
            assertEquals(tags, way.tags)
        }
    }

    @Test fun `split way with one split position at vertex`() {
        val data = doSplit(SplitAtPoint(p[1]))
        assertTrue(data.nodes.isEmpty()) // no nodes were added
        data.checkWaysNodes(
            listOf(0, 1),
            listOf(1, 2, 3)
        )
    }

    @Test fun `split way with one split position`() {
        val p1 = p[1]
        val p2 = p[2]
        val data = doSplit(SplitAtLinePosition(p1, p2, 0.5))

        val node = data.nodes.single()
        assertEquals(
            createTranslated(
                p1.latitude + 0.5 * (p2.latitude - p1.latitude),
                p1.longitude + 0.5 * (p2.longitude - p1.longitude)
            ),
            node.position
        )
        data.checkWaysNodes(
            listOf(0, 1, -1),
            listOf(-1, 2, 3)
        )
    }

    @Test fun `split way with several split position at vertices`() {
        // 0   1   2   3
        //     |   |
        val data = doSplit(SplitAtPoint(p[1]), SplitAtPoint(p[2]))

        assertTrue(data.nodes.isEmpty()) // no nodes were added
        data.checkWaysNodes(
            listOf(0, 1),
            listOf(1, 2),
            listOf(2, 3)
        )
    }

    @Test fun `split way with multiple split positions`() {
        // 0   1   2   3
        //       |   |
        val data = doSplit(
            SplitAtLinePosition(p[1], p[2], 0.5),
            SplitAtLinePosition(p[2], p[3], 0.5)
        )

        assertEquals(2, data.nodes.size)
        data.checkWaysNodes(
            listOf(0, 1, -1),
            listOf(-1, 2, -2),
            listOf(-2, 3)
        )
    }

    @Test fun `split way with multiple split positions, one of which is at vertices`() {
        // 0   1   2   3
        //   | |
        val data = doSplit(
            SplitAtLinePosition(p[0], p[1], 0.5),
            SplitAtPoint(p[1])
        )

        assertEquals(1, data.nodes.size)
        data.checkWaysNodes(
            listOf(0, -1),
            listOf(-1, 1),
            listOf(1, 2, 3)
        )
    }

    @Test fun `split way with multiple unordered split positions between the same nodes`() {
        // 0  1  2  3
        //    ||||
        val data = doSplit(
            SplitAtLinePosition(p[1], p[2], 0.66),
            SplitAtPoint(p[2]),
            SplitAtPoint(p[1]),
            SplitAtLinePosition(p[1], p[2], 0.33)
        )

        assertEquals(2, data.nodes.size)
        data.checkWaysNodes(
            listOf(0, 1),
            listOf(1, -1),
            listOf(-1, -2),
            listOf(-2, 2),
            listOf(2, 3)
        )
    }

    @Test fun `reuse object id of longest split chunk (= second chunk)`() {
        val data = doSplit(SplitAtPoint(p[1]))
        assertEquals(way.id, data.ways.maxByOrNull { it.nodeIds.size }?.id)
    }

    @Test fun `reuse object id of longest split chunk (= first chunk)`() {
        val data = doSplit(SplitAtPoint(p[2]))
        assertEquals(way.id, data.ways.maxByOrNull { it.nodeIds.size }?.id)
    }

    @Test fun `insert all way chunks into relation the way is a member of`() {
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(0)))
        ))
        val data = doSplit()

        data.checkRelationWayMemberNodeIds(
            0L to listOf(listOf(0, 1, -1), listOf(-1, 2, 3))
        )
    }

    @Test fun `insert all way chunks into multiple relations the way is a member of`() {
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(0))),
            rel(1, waysAsMembers(listOf(0)))
        ))
        val data = doSplit()

        data.checkRelationWayMemberNodeIds(
            0L to listOf(listOf(0, 1, -1), listOf(-1, 2, 3)),
            1L to listOf(listOf(0, 1, -1), listOf(-1, 2, 3))
        )
    }

    @Test fun `insert all way chunks multiple times into relation the way is a member of multiple times`() {
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(0, 5, 0)))
        on(repos.getWay(2)).thenReturn(way(2, mutableListOf(3, 4, 3)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(0, 1, 0, 2)))
        ))
        val data = doSplit()

        data.checkRelationWayMemberNodeIds(
            0L to listOf(
                listOf(-1, 2, 3),
                listOf(0, 1, -1),
                listOf(0, 5, 0),
                listOf(0, 1, -1),
                listOf(-1, 2, 3),
                listOf(3, 4, 3)
            )
        )
    }

    @Test fun `all way chunks in updated relations have the same role as the original way`() {
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(0), "cool role")),
            rel(1, waysAsMembers(listOf(0), "not so cool role"))
        ))
        val data = doSplit()

        assertTrue(data.getRelation(0)!!.members.all { it.role == "cool role" })
        assertTrue(data.getRelation(1)!!.members.all { it.role == "not so cool role" })
    }

    @Test fun `insert way chunks at correct position in the updated relation`() {
        // 4 5 | 0 1 2 3 | 6 7  => 4 5 | 0 1 -1 | -1 2 3 | 6 7
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(4, 5)))
        on(repos.getWay(2)).thenReturn(way(2, mutableListOf(6, 7)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(1, 0, 2)))
        ))
        val data = doSplit()

        data.checkRelationWayMemberNodeIds(
            0L to listOf(
                listOf(4, 5),
                listOf(0, 1, -1),
                listOf(-1, 2, 3),
                listOf(6, 7)
            )
        )
    }

    @Test fun `ignore if a way has been deleted when determining way orientation in relation`() {
        /* while determining the orientation of the way in the relation, the neighbouring ways are
           downloaded and analyzed - if they do not exist anymore, this should not lead to a
           nullpointer exception */
        on(repos.getWay(1)).thenReturn(null)
        on(repos.getWay(2)).thenReturn(null)
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(1, 0, 2)))
        ))
        doSplit()
    }

    @Test fun `insert way chunks backwards in the updated relation as end of reverse chain`() {
        // 4 3 | 0 1 2 3  =>  4 3 | -1 2 3 | 0 1 -1
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(4, 3)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(1, 0)))
        ))
        val data = doSplit()

        data.checkRelationWayMemberNodeIds(
            0L to listOf(
                listOf(4, 3),
                listOf(-1, 2, 3),
                listOf(0, 1, -1)
            )
        )
    }

    @Test fun `ignore non-way relation members when determining way orientation in relation`() {
        // 4 3 | 0 1 2 3  =>  4 3 | -1 2 3 | 0 1 -1
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(4, 3)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, listOf(
                member(WAY, 1),
                member(NODE, 0),
                member(RELATION, 1),
                member(WAY, 0),
                member(NODE, 0),
                member(RELATION, 1)
            ))
        ))
        val data = doSplit()

        data.checkRelationWayMemberNodeIds(
            0L to listOf(
                listOf(4, 3),
                listOf(-1, 2, 3),
                listOf(0, 1, -1)
            )
        )
    }

    @Test fun `insert way chunks forwards in the updated relation as end of chain`() {
        // 4 0 | 0 1 2 3  =>  4 0 | 0 1 -1 | -1 2 3
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(4, 0)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(1, 0)))
        ))
        val data = doSplit()

        data.checkRelationWayMemberNodeIds(
            0L to listOf(
                listOf(4, 0),
                listOf(0, 1, -1),
                listOf(-1, 2, 3)
            )
        )
    }

    @Test fun `insert way chunks backwards in the updated relation as start of reverse chain`() {
        // 0 1 2 3 | 4 0  =>  -1 2 3 | 0 1 -1 | 4 0
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(4, 0)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(0, 1)))
        ))
        val data = doSplit()

        data.checkRelationWayMemberNodeIds(
            0L to listOf(
                listOf(-1, 2, 3),
                listOf(0, 1, -1),
                listOf(4, 0)
            )
        )
    }

    @Test fun `insert way chunks forwards in the updated relation as start of chain`() {
        // 0 1 2 3 | 4 3  =>  0 1 -1 | -1 2 3 | 4 3
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(4, 3)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, waysAsMembers(listOf(0, 1)))
        ))
        val data = doSplit()

        data.checkRelationWayMemberNodeIds(
            0L to listOf(
                listOf(0, 1, -1),
                listOf(-1, 2, 3),
                listOf(4, 3)
            )
        )
    }

    @Test fun `update a restriction relation with split from-way and via node`() {
        `update a restriction-like relation with split-way and via node`("restriction", "via", "from")
    }

    @Test fun `update a restriction relation with split to-way and via node`() {
        `update a restriction-like relation with split-way and via node`("restriction", "via", "to")
    }

    private fun `update a restriction-like relation with split-way and via node`(
        relationType: String,
        via: String,
        role: String
    ) {
        val otherRole = if (role == "from") "to" else "from"
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(3, 4)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, listOf(
                member(WAY, 0, role),
                member(WAY, 1, otherRole),
                member(NODE, 3, via)
            ), mapOf("type" to relationType))
        ))
        val data = doSplit()

        val relation = data.relations.single()
        assertEquals(3, relation.members.size)

        val relationMember = relation.members[0]
        val newWay = data.getWay(relationMember.ref)!!
        assertEquals(listOf<Long>(-1, 2, 3), newWay.nodeIds.toList())
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
        relationType: String,
        via: String,
        role: String
    ) {
        val otherRole = if (role == "from") "to" else "from"
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(5, 7)))
        on(repos.getWay(2)).thenReturn(way(2, mutableListOf(5, 4, 3)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, listOf(
                member(WAY, 0, role),
                member(WAY, 1, otherRole),
                member(WAY, 2, via)
            ), mapOf("type" to relationType))
        ))
        val data = doSplit()

        val relation = data.relations.single()
        assertEquals(3, relation.members.size)

        val fromRelationMember = relation.members[0]
        val fromWay = data.getWay(fromRelationMember.ref)!!
        assertEquals(listOf<Long>(-1, 2, 3), fromWay.nodeIds.toList())
        assertEquals(role, fromRelationMember.role)
        assertEquals(WAY, fromRelationMember.type)
    }

    @Test fun `update a restriction-like relation with split-way and multiple via ways`() {
        on(repos.getWay(0)).thenReturn(way(0, mutableListOf(0, 1, 2, 3)))
        on(repos.getWay(1)).thenReturn(way(1, mutableListOf(6, 7)))
        on(repos.getWay(2)).thenReturn(way(2, mutableListOf(4, 5, 6)))
        on(repos.getWay(3)).thenReturn(way(3, mutableListOf(3, 4)))
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, listOf(
                member(WAY, 0, "from"),
                member(WAY, 1, "to"),
                member(WAY, 2, "via"),
                member(WAY, 3, "via")
            ), mapOf("type" to "restriction"))
        ))
        val data = doSplit(SplitAtPoint(p[2]))

        val relation = data.relations.single()
        assertEquals(4, relation.members.size)

        val fromRelationMember = relation.members[0]
        val fromWay = data.getWay(fromRelationMember.ref)!!
        assertEquals(listOf<Long>(2, 3), fromWay.nodeIds.toList())
        assertEquals("from", fromRelationMember.role)
        assertEquals(WAY, fromRelationMember.type)
    }

    @Test fun `no special treatment of restriction relation if the way has another role`() {
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, listOf(
                member(WAY, 0, "another role"),
                member(WAY, 1, "from"),
                member(NODE, 3, "via"),
                member(WAY, 3, "to")
            ), mapOf("type" to "restriction"))
        ))
        val data = doSplit()

        val relation = data.relations.single()
        assertEquals(5, relation.members.size)
    }

    @Test fun `no special treatment of restriction relation if there is no via`() {
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, listOf(
                member(WAY, 0, "from"),
                member(WAY, 1, "to")
            ), mapOf("type" to "restriction"))
        ))
        val data = doSplit()

        val relation = data.relations.single()
        assertEquals(3, relation.members.size)
    }

    @Test fun `no special treatment of restriction relation if from-way does not touch via`() {
        on(repos.getRelationsForWay(0)).thenReturn(listOf(
            rel(0, listOf(
                member(WAY, 0, "from"),
                member(NODE, 4, "via"),
                member(WAY, 3, "to")
            ), mapOf("type" to "restriction"))
        ))
        val data = doSplit()

        val relation = data.relations.single()
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

    @Test fun idsUpdatesApplied() {
        val way = way(id = -1)
        val action = SplitWayAction(way, listOf())
        val idUpdates = mapOf(ElementKey(WAY, -1) to 5L)

        assertEquals(
            SplitWayAction(way.copy(id = 5), listOf()),
            action.idsUpdatesApplied(idUpdates)
        )
    }

    @Test fun elementKeys() {
        assertEquals(
            listOf(ElementKey(WAY, -1)),
            SplitWayAction(way(id = -1), listOf()).elementKeys
        )
    }

    private fun doSplit(
        vararg splits: SplitPolylineAtPosition = arrayOf(split),
        originalWay: Way = way
    ): MapData {
        val action = SplitWayAction(originalWay, ArrayList(splits.toList()))
        val counts = action.newElementsCount
        val elementKeys = ArrayList<ElementKey>()
        for (i in 1L..counts.nodes) { elementKeys.add(ElementKey(NODE, -i)) }
        for (i in 1L..counts.ways) { elementKeys.add(ElementKey(WAY, -i)) }
        for (i in 1L..counts.relations) { elementKeys.add(ElementKey(RELATION, -i)) }
        val provider = ElementIdProvider(elementKeys)
        on(repos.getWay(way.id)).thenReturn(way)
        val data = action.createUpdates(repos, provider)
        return MutableMapData(data.creations + data.modifications)
    }

    private fun MapData.checkWaysNodes(vararg chunks: List<Long>) {
        assertTrue(ways.map { it.nodeIds }.containsExactlyInAnyOrder(chunks.toList()))
    }

    private fun MapData.checkRelationWayMemberNodeIds(vararg chunksByRelationId: Pair<Long, List<List<Long>>>) {
        assertEquals(
            mapOf(*chunksByRelationId),
            relations.associate { rel -> rel.id to
                rel.members
                    .filter { it.type == WAY }
                    .map { (getWay(it.ref) ?: repos.getWay(it.ref))!!.nodeIds }
            }
        )
    }
}
