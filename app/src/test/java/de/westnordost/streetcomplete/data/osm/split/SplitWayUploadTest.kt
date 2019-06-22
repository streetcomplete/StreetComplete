package de.westnordost.streetcomplete.data.osm.split

import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.argumentCaptor
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.util.SphericalEarthMath
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.verify

class SplitWayUploadTest {
    private val uploader: SplitWayUpload
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
    private val split = SplitWayAtPosition(way, node2, node3, 0.5)

    init {
        osmDao = Mockito.mock(MapDataDao::class.java)
        uploader = SplitWayUpload(osmDao)
    }

    @Before fun setUp() {
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
        uploader.upload(0, way, listOf(SplitWayAtPosition(way, node1, node2, 0.5)))
    }

    @Test fun `should merge last and first chunk for closed ways`() {
        way = OsmWay(1,1, mutableListOf(1,2,3,4,1), null)
        uploader.upload(0, way, listOf(
            SplitWayAtPosition(way, node2, node3, 0.0),
            SplitWayAtPosition(way, node3, node4, 0.0)
        ))
        val elements = getUploadedElements()
        assertEquals(2, elements.ways.size)
        assertTrue(elements.ways.map { it.nodeIds }.containsAll(listOf<List<Long>>(
            listOf(3,4,1,2),
            listOf(2,3)
        )))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if way was deleted`() {
        on(osmDao.getWay(1)).thenReturn(null)
        uploader.upload(0, way, listOf(split))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if updated way was cut at the start`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(2,3,4))
        uploader.upload(0, way, listOf(split))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if updated way was cut at the end`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(1,2,3))
        uploader.upload(0, way, listOf(split))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if updated way has split position at its very start`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(2,3,4))
        uploader.upload(0, way, listOf(SplitWayAtPosition(way, node2, node3, 0.0)))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if first node is not in the updated way`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(1,3,4))
        uploader.upload(0, way, listOf(split))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if second node is not in the updated way`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(1,2,4))
        uploader.upload(0, way, listOf(split))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if the second node is not directly after the first one in the updated way`() {
        on(osmDao.getWay(1)).thenReturn(wayWithNodes(1,3,2,4))
        uploader.upload(0, way, listOf(split))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if first node of split position was deleted`() {
        on(osmDao.getNode(2)).thenReturn(null)
        uploader.upload(0, way, listOf(split))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if second node of split position was deleted`() {
        on(osmDao.getNode(3)).thenReturn(null)
        uploader.upload(0, way, listOf(split))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if first node of split position has been moved`() {
        on(osmDao.getNode(2)).thenReturn(OsmNode(2, 2, 0.333, 0.333, null))
        uploader.upload(0, way, listOf(split))
    }

    @Test(expected = ConflictException::class)
    fun `raise conflict if second node of split position has been moved`() {
        on(osmDao.getNode(3)).thenReturn(OsmNode(3, 2, 0.333, 0.333, null))
        uploader.upload(0, way, listOf(split))
    }

    @Test fun `successfully split way with one split position at vertex`() {
        uploader.upload(0, way, listOf(SplitWayAtPosition(way, node2, node3, 0.0)))

        val elements = getUploadedElements()
        assertTrue(elements.nodes.isEmpty()) // no nodes were added
        assertEquals(2, elements.ways.size)
        assertTrue(elements.ways.map { it.nodeIds }.containsAll(listOf<List<Long>>(
            listOf(1,2),
            listOf(2,3,4)
        )))
    }

    @Test fun `successfully split way with one split position`() {
        uploader.upload(0, way, listOf(SplitWayAtPosition(way, node2, node3, 0.5)))

        val elements = getUploadedElements()
        assertEquals(1, elements.nodes.size)
        assertEquals(2, elements.ways.size)
        val node = elements.nodes.single()
        val p1 = node2.position
        val p2 = node3.position
        assertEquals(
            SphericalEarthMath.createTranslated(
                p1.latitude + 0.5 * (p2.latitude - p1.latitude),
                p1.longitude + 0.5 * (p2.longitude - p1.longitude)),
            node.position)

        assertTrue(elements.ways.map { it.nodeIds }.containsAll(listOf<List<Long>>(
            listOf(1,2,-1),
            listOf(-1,3,4)
        )))
    }

    @Test fun `successfully split way with several split position at vertices`() {
        // 1   2   3   4
        //     |   |
        uploader.upload(0, way, listOf(
            SplitWayAtPosition(way, node2, node3, 0.0),
            SplitWayAtPosition(way, node3, node4, 0.0)
        ))

        val elements = getUploadedElements()
        assertTrue(elements.nodes.isEmpty()) // no nodes were added
        assertEquals(3, elements.ways.size)
        assertTrue(elements.ways.map { it.nodeIds }.containsAll(listOf<List<Long>>(
            listOf(1,2),
            listOf(2,3),
            listOf(3,4)
        )))
    }

    @Test fun `successfully split way with multiple split positions`() {
        // 1   2   3   4
        //       |   |
        uploader.upload(0, way, listOf(
            SplitWayAtPosition(way, node2, node3, 0.5),
            SplitWayAtPosition(way, node3, node4, 0.5)
        ))

        val elements = getUploadedElements()
        assertEquals(2, elements.nodes.size)
        assertEquals(3, elements.ways.size)
        assertTrue(elements.ways.map { it.nodeIds }.containsAll(listOf<List<Long>>(
            listOf(1,2,-1),
            listOf(-1,3,-2),
            listOf(-2,4)
        )))
    }

    @Test fun `successfully split way with multiple split positions, one of which is at vertices`() {
        // 1   2   3   4
        //   | |
        uploader.upload(0, way, listOf(
            SplitWayAtPosition(way, node1, node2, 0.5),
            SplitWayAtPosition(way, node2, node3, 0.0)
        ))

        val elements = getUploadedElements()
        assertEquals(1, elements.nodes.size)
        assertEquals(3, elements.ways.size)
        assertTrue(elements.ways.map { it.nodeIds }.containsAll(listOf<List<Long>>(
            listOf(1,-1),
            listOf(-1,2),
            listOf(2,3,4)
        )))
    }

    @Test fun `successfully split way with multiple unordered split positions between the same nodes`() {
        // 1  2  3  4
        //    ||||
        uploader.upload(0, way, listOf(
            SplitWayAtPosition(way, node2, node3, 0.66),
            SplitWayAtPosition(way, node3, node4, 0.0),
            SplitWayAtPosition(way, node2, node3, 0.0),
            SplitWayAtPosition(way, node2, node3, 0.33)
        ))

        val elements = getUploadedElements()
        assertEquals(2, elements.nodes.size)
        assertEquals(5, elements.ways.size)
        assertTrue(elements.ways.map { it.nodeIds }.containsAll(listOf<List<Long>>(
            listOf(1,2),
            listOf(2,-1),
            listOf(-1,-2),
            listOf(-2,3),
            listOf(3,4)
        )))
    }

    @Test fun `should reuse object id of longest split chunk (= second chunk)`() {
        uploader.upload(0, way, listOf(SplitWayAtPosition(way, node2, node3, 0.0)))
        assertEquals(way.id, getUploadedElements().ways.maxBy { it.nodeIds.size }?.id)
    }

    @Test fun `should reuse object id of longest split chunk (= first chunk)`() {
        uploader.upload(0, way, listOf(SplitWayAtPosition(way, node3, node4, 0.0)))
        assertEquals(way.id, getUploadedElements().ways.maxBy { it.nodeIds.size }?.id)
    }

    // TODO relations...

    private fun getUploadedElements(): Elements {
        val arg: ArgumentCaptor<Iterable<Element>> = argumentCaptor()
        verify(osmDao).uploadChanges(eq(0), arg.capture(), eq(null))
        val elements = arg.value.toList()
        return Elements(
            elements.mapNotNull { it as? Node },
            elements.mapNotNull { it as? Way },
            elements.mapNotNull { it as? Relation }
        )
    }

    private fun wayWithNodes(vararg nodes: Long) = OsmWay(1, 2, nodes.asList(), null)
}

private data class Elements(
    val nodes: List<Node>,
    val ways: List<Way>,
    val relations: List<Relation>
)
