package de.westnordost.streetcomplete.data.osm.edits.insert

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapData
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.reset

internal class InsertNodeIntoWayActionTest {
    private var repos: MapDataRepository = mock()

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

    private var way = way(0, mutableListOf(0, 1, 2, 3))
        set(value) {
            field = value
            updateRepos(value)
        }

    private var changes = StringMapChanges(listOf(StringMapEntryAdd("a", "b")))

    private fun updateRepos(way: Way) {
        // let the repos return this way and all its nodes on getWayComplete
        on(repos.getWayComplete(0))
            .thenReturn(MutableMapData(way.nodeIds.map { nodeId -> n[nodeId.toInt()] }.shuffled() + way))
        on(repos.getRelationsForWay(0)).thenReturn(listOf())
    }

    @Before
    fun setUp() {
        reset(repos)
        updateRepos(way)
    }

    @Test fun `new element count`() {
        assertEquals(0, InsertNodeIntoWayAction(mock(), mock(), null).newElementsCount)
        assertEquals(1, InsertNodeIntoWayAction(mock(), mock(), mock()).newElementsCount)
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if way was deleted`() {
        on(repos.getWayComplete(0)).thenReturn(null)
        doInsert(p(0.0, 0.0))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if updated way was cut at the start`() {
        way = way(0, mutableListOf(1, 2, 3))
        doInsert(p(0.0, 0.0), originalWay = way(0, mutableListOf(0, 1, 2, 3)))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if updated way was cut at the end`() {
        way = way(0, mutableListOf(0, 1, 2))
        doInsert(p(0.0, 0.0), originalWay = way(0, mutableListOf(0, 1, 2, 3)))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if no node at given position exist anymore`() {
        doInsert(p(0.0, 0.9999999))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if no node at position 1 exist anymore`() {
        doInsert(
            position = p(0.0, 0.5),
            insertBetween = InsertBetween(p(0.0, 0.0000001), p(0.0, 1.0))
        )
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if no node at position 2 exist anymore`() {
        doInsert(
            position = p(0.0, 0.5),
            insertBetween = InsertBetween(p(0.0, 0.0), p(0.0, 0.9999999))
        )
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if node at position 2 is not directly successive to position 1`() {
        doInsert(
            position = p(0.0, 0.5),
            insertBetween = InsertBetween(p(0.0, 0.0), p(1.0, 1.0))
        )
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict changes to node cannot be applied`() {
        way = way(0, mutableListOf(0, 1, 2, 3), mapOf("a" to "c"))
        doInsert(
            position = p(0.0, 0.0),
            changes = StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
        )
    }

    @Test fun `update existing node`() {
        val data = doInsert(
            position = p(0.0, 0.0),
            changes = StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
        )
        assertEquals(4, data.getWay(0)!!.nodeIds.size) // didn't add a node
        assertEquals(
            mapOf("a" to "b"),
            data.getNode(0)!!.tags
        )
    }

    @Test fun `add node in-between two nodes`() {
        val data = doInsert(
            position = p(0.0, 0.5),
            changes = StringMapChanges(listOf(StringMapEntryAdd("a", "b"))),
            insertBetween = InsertBetween(p(0.0, 0.0), p(0.0, 1.0))
        )
        assertEquals(-1, data.getWay(0)!!.nodeIds[1]) // did add a node

        assertEquals(mapOf("a" to "b"), data.getNode(-1)!!.tags)
        assertEquals(p(0.0, 0.5), data.getNode(-1)!!.position)
    }

    private fun doInsert(
        position: LatLon,
        changes: StringMapChanges = this.changes,
        insertBetween: InsertBetween? = null,
        originalWay: Way = way,
    ): MapData {
        val action = InsertNodeIntoWayAction(position, changes, insertBetween)
        val counts = action.newElementsCount
        val elementKeys = ArrayList<ElementKey>()
        for (i in 1L..counts.nodes) { elementKeys.add(ElementKey(ElementType.NODE, -i)) }
        for (i in 1L..counts.ways) { elementKeys.add(ElementKey(ElementType.WAY, -i)) }
        for (i in 1L..counts.relations) { elementKeys.add(ElementKey(ElementType.RELATION, -i)) }
        val provider = ElementIdProvider(elementKeys)

        val data = action.createUpdates(originalWay, way, repos, provider)
        return MutableMapData(data.creations + data.modifications)
    }
}
