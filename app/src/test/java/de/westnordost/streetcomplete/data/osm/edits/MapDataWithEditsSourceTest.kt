package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementConflictException
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.util.intersect
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions

class MapDataWithEditsSourceTest {

    private lateinit var editsCtrl: ElementEditsController
    private lateinit var mapDataCtrl: MapDataController
    private val geometryCreator = ElementGeometryCreator()

    private lateinit var mapData: MutableMapDataWithGeometry

    private lateinit var editsListener: ElementEditsSource.Listener
    private lateinit var mapDataListener: MapDataController.Listener

    @Before
    fun setUp() {
        editsCtrl = mock()
        mapDataCtrl = mock()
        mapData = MutableMapDataWithGeometry()
        // a trick to get the edit action to apply to anything
        mapData.putElement(nd(-1L, lat = 60.0, lon = 60.0))

        on(editsCtrl.getIdProvider(anyLong())).thenReturn(ElementIdProvider(listOf()))


        on(mapDataCtrl.get(any(), anyLong())).thenAnswer { invocation ->
            val elementType = invocation.getArgument<Element.Type>(0)!!
            val elementId = invocation.getArgument<Long>(1)
            when(elementType) {
                NODE -> mapData.getNode(elementId)
                WAY -> mapData.getWay(elementId)
                RELATION -> mapData.getRelation(elementId)
            }
        }
        on(mapDataCtrl.getNode(anyLong())).thenAnswer { invocation ->
            mapData.getNode(invocation.getArgument(0))
        }
        on(mapDataCtrl.getWay(anyLong())).thenAnswer { invocation ->
            mapData.getWay(invocation.getArgument(0))
        }
        on(mapDataCtrl.getRelation(anyLong())).thenAnswer { invocation ->
            mapData.getRelation(invocation.getArgument(0))
        }
        on(mapDataCtrl.getNodes(any())).thenAnswer { invocation ->
            invocation.getArgument<Collection<Long>>(0).mapNotNull { mapData.getNode(it) }
        }
        on(mapDataCtrl.getWays(any())).thenAnswer { invocation ->
            invocation.getArgument<Collection<Long>>(0).mapNotNull { mapData.getWay(it) }
        }
        on(mapDataCtrl.getRelations(any())).thenAnswer { invocation ->
            invocation.getArgument<Collection<Long>>(0).mapNotNull { mapData.getRelation(it) }
        }
        on(mapDataCtrl.getAll(any())).thenAnswer { invocation ->
            invocation.getArgument<Collection<ElementKey>>(0).mapNotNull {
                when(it.elementType) {
                    NODE -> mapData.getNode(it.elementId)
                    WAY -> mapData.getWay(it.elementId)
                    RELATION -> mapData.getRelation(it.elementId)
                }
            }
        }
        on(mapDataCtrl.getWaysForNode(anyLong())).thenAnswer { invocation ->
            val nodeId = invocation.getArgument<Long>(0)
            mapData.ways.filter { it.nodeIds.contains(nodeId) }
        }
        on(mapDataCtrl.getRelationsForNode(anyLong())).thenAnswer { invocation ->
            val nodeId = invocation.getArgument<Long>(0)
            mapData.relations.filter { r -> r.members.any { it.ref == nodeId && it.type == NODE } }
        }
        on(mapDataCtrl.getRelationsForWay(anyLong())).thenAnswer { invocation ->
            val wayId = invocation.getArgument<Long>(0)
            mapData.relations.filter { r -> r.members.any { it.ref == wayId && it.type == WAY } }
        }
        on(mapDataCtrl.getRelationsForRelation(anyLong())).thenAnswer { invocation ->
            val relationId = invocation.getArgument<Long>(0)
            mapData.relations.filter { r -> r.members.any { it.ref == relationId && it.type == RELATION } }
        }
        on(mapDataCtrl.getGeometry(any(), anyLong())).thenAnswer { invocation ->
            val elementType = invocation.getArgument<Element.Type>(0)!!
            val elementId = invocation.getArgument<Long>(1)
            when(elementType) {
                NODE -> mapData.getNodeGeometry(elementId)
                WAY -> mapData.getWayGeometry(elementId)
                RELATION -> mapData.getRelationGeometry(elementId)
            }
        }
        on(mapDataCtrl.getMapDataWithGeometry(any())).thenAnswer { invocation ->
            val bbox = invocation.getArgument<BoundingBox>(0)
            val result = MutableMapDataWithGeometry()
            for (element in mapData) {
                val geometry = when(element.type!!) {
                    NODE -> mapData.getNodeGeometry(element.id)
                    WAY -> mapData.getWayGeometry(element.id)
                    RELATION -> mapData.getRelationGeometry(element.id)
                }
                if (geometry != null && geometry.getBounds().intersect(bbox)) {
                    result.put(element, geometry)
                }
            }
            result
        }

        on(editsCtrl.addListener(any())).then { invocation ->
            editsListener = invocation.getArgument(0)
            Unit
        }

        on(mapDataCtrl.addListener(any())).then { invocation ->
            mapDataListener = invocation.getArgument(0)
            Unit
        }
    }

    //region get

    @Test
    fun `get returns nothing`() {
        thereAreNoOriginalElements()
        thereAreNoEditedElements()

        val s = create()
        assertNull(s.get(NODE, 1L))
    }

    @Test
    fun `get returns original element`() {
        val nd = nd(id = 1L)

        originalElementsAre(nd)
        thereAreNoEditedElements()

        val s = create()
        assertEquals(nd, s.get(NODE, 1L))
    }

    @Test
    fun `get returns updated element`() {
        val nd = nd(id = 1L)
        val nd2 = nd(id = 1L, tags = mapOf("bla" to "blub"))

        originalElementsAre(nd)
        editedElementsAre(nd2)

        val s = create()
        assertEquals(nd2, s.get(NODE, 1L))
    }

    @Test
    fun `get returns updated element updated from updated element`() {
        val nd = nd(id = 1L)
        val nd2 = nd(id = 1L, tags = mapOf("bla" to "blub"))
        val nd3 = nd(id = 1L, tags = mapOf("bla" to "flop"))

        originalElementsAre(nd)

        val action2 = mock<ElementEditAction>()
        on(action2.createUpdates(eq(nd), any(), any())).thenReturn(listOf(nd2))
        val action3 = mock<ElementEditAction>()
        on(action3.createUpdates(eq(nd2), any(), any())).thenReturn(listOf(nd3))
        on(editsCtrl.getAllUnsynced()).thenReturn(listOf(
            edit(
                elementType = NODE,
                elementId = 1L,
                action = action2
            ),
            edit(
                elementType = NODE,
                elementId = 1L,
                action = action3
            ),
        ))

        val s = create()
        assertEquals(nd3, s.get(NODE, 1L))
    }

    @Test
    fun `get returns null if updated element was deleted`() {
        val nd = nd(id = 1L)
        val nd2 = nd(id = 1L)
        nd2.isDeleted = true

        originalElementsAre(nd)
        editedElementsAre(nd2)

        val s = create()
        assertNull(s.get(NODE, 1L))
    }

    @Test
    fun `conflict on applying edit is ignored`() {
        val nd = nd(id = 1L)

        originalElementsAre(nd)

        val action = mock<ElementEditAction>()
        on(action.createUpdates(eq(nd), any(), any())).thenThrow(ElementConflictException())
        on(editsCtrl.getAllUnsynced()).thenReturn(listOf(edit(
            elementType = NODE,
            elementId = 1L,
            action = action
        )))

        val s = create()
        assertEquals(nd, s.get(NODE, 1L))
    }

    //endregion

    //region getGeometry

    @Test
    fun `getGeometry returns nothing`() {
        thereAreNoOriginalGeometries()
        thereAreNoEditedElements()

        val s = create()
        assertNull(s.getGeometry(NODE, 1L))
    }

    @Test
    fun `getGeometry returns original geometry`() {
        val p = pointGeom(0.0, 0.0)

        originalGeometriesAre(ElementGeometryEntry(NODE, 1L, p))
        thereAreNoEditedElements()

        val s = create()
        assertEquals(p, s.getGeometry(NODE, 1L))
    }

    @Test
    fun `getGeometry returns updated geometry`() {
        val nd = nd(1L, 1, 0.0, 0.0)
        val p = pointGeom(0.0, 0.0)
        val nd2 = nd(1L, 1, 1.0, 2.0)
        val p2 = pointGeom(1.0, 2.0)

        originalElementsAre(nd)
        originalGeometriesAre(ElementGeometryEntry(NODE, 1L, p))
        editedElementsAre(nd2)

        val s = create()
        assertEquals(p2, s.getGeometry(NODE, 1L))
    }

    @Test
    fun `getGeometry returns updated geometry updated from updated element`() {
        val nd = nd(id = 1L)
        val nd2 = nd(id = 1L, lat = 1.0, lon = 2.0)
        val nd3 = nd(id = 1L, lat = 55.0, lon = 56.0)
        val p = pointGeom(0.0, 0.0)
        val p3 = pointGeom(55.0, 56.0)

        originalElementsAre(nd)
        originalGeometriesAre(ElementGeometryEntry(NODE, 1L, p))

        val action2 = mock<ElementEditAction>()
        on(action2.createUpdates(eq(nd), any(), any())).thenReturn(listOf(nd2))
        val action3 = mock<ElementEditAction>()
        on(action3.createUpdates(eq(nd2), any(), any())).thenReturn(listOf(nd3))
        on(editsCtrl.getAllUnsynced()).thenReturn(listOf(
            edit(
                elementType = NODE,
                elementId = 1L,
                action = action2
            ),
            edit(
                elementType = NODE,
                elementId = 1L,
                action = action3
            ),
        ))

        val s = create()
        assertEquals(p3, s.getGeometry(NODE, 1L))
    }

    @Test
    fun `getGeometry returns null if element was deleted`() {
        val nd = nd(id = 1L)
        val nd2 = nd(id = 1L)
        val p = pointGeom(0.0, 0.0)
        nd2.isDeleted = true

        originalElementsAre(nd)
        originalGeometriesAre(ElementGeometryEntry(NODE, 1L, p))
        editedElementsAre(nd2)

        val s = create()
        assertNull(s.getGeometry(NODE, 1L))
    }

    //endregion

    //region getWayComplete

    @Test
    fun `getWayComplete returns null`() {
        thereAreNoOriginalElements()
        thereAreNoEditedElements()

        val s = create()
        assertNull(s.getWayComplete(1L))
    }

    @Test
    fun `getWayComplete returns null because it is not complete`() {
        val w = way(id = 1L, nodeIds = listOf(1,2,3))

        originalElementsAre(w, nd(1L), nd(2L))
        thereAreNoEditedElements()

        val s = create()
        assertNull(s.getWayComplete(1L))
    }

    @Test
    fun `getWayComplete returns original way with original node ids`() {
        val w = way(id = 1L, nodeIds = listOf(1L,2L,3L))
        val n1 = nd(1L)
        val n2 = nd(2L)
        val n3 = nd(3L)

        originalElementsAre(w, n1, n2, n3)
        thereAreNoEditedElements()

        val s = create()
        val data = s.getWayComplete(1L)!!
        assertEquals(w, data.ways.single())
        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(n1,n2,n3)))
    }

    @Test
    fun `getWayComplete returns original way with updated node ids`() {
        val w = way(id = 1L, nodeIds = listOf(1L,2L,3L))
        val nd1 = nd(1L)
        val nd2 = nd(2L)
        val nd3 = nd(3L)
        val nd1New = nd(1L, tags = mapOf("foo" to "bar"))

        originalElementsAre(nd1, nd2, nd3, w)
        editedElementsAre(nd1New)

        val s = create()
        val data = s.getWayComplete(1L)!!
        assertEquals(w, data.ways.single())
        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(nd1New, nd2, nd3)))
    }

    @Test
    fun `getWayComplete returns updated way with updated node ids`() {
        val w = way(id = 1L, nodeIds = listOf(1L,2L))
        val wNew = way(id = 1L, nodeIds = listOf(3L, 1L))

        val nd1 = nd(1L)
        val nd2 = nd(2L)

        val nd1New = nd(1L, tags = mapOf("foo" to "bar"))
        val nd2New = nd(2L)
        nd2New.isDeleted = true
        val nd3New = nd(3L)

        originalElementsAre(nd1, nd2, w)
        editedElementsAre(nd1New, nd2New, nd3New, wNew)

        val s = create()
        val data = s.getWayComplete(1L)!!
        assertEquals(wNew, data.ways.single())
        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(nd1New, nd3New)))
    }

    @Test
    fun `getWayComplete returns null because a node of the way was deleted`() {
        val w = way(id = 1L, nodeIds = listOf(1L,2L))
        val nd1 = nd(1L)
        val nd1New = nd(1L)
        nd1New.isDeleted = true

        originalElementsAre(w, nd1)
        editedElementsAre(nd1New)

        val s = create()
        assertNull(s.getWayComplete(1L))
    }

    //endregion

    //region getRelationComplete

    @Test
    fun `getRelationComplete returns null`() {
        thereAreNoOriginalElements()
        thereAreNoEditedElements()

        val s = create()
        assertNull(s.getRelationComplete(1L))
    }

    @Test
    fun `getRelationComplete returns incomplete relation`() {
        val r = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", WAY),
            OsmRelationMember(2L, "", WAY),
        ))
        val w = way(id = 1L, nodeIds = listOf(1L,2L))
        val n1 = nd(id = 1L)
        val n2 = nd(id = 2L)

        originalElementsAre(r, w, n1, n2)
        thereAreNoEditedElements()

        val s = create()
        val data = s.getRelationComplete(1L)!!
        assertEquals(w, data.ways.single())
        assertEquals(r, data.relations.single())
    }

    @Test
    fun `getRelationComplete returns original relation with original members`() {
        val n1 = nd(id = 1L)
        val n2 = nd(id = 2L)
        val n3 = nd(id = 3L)
        val n4 = nd(id = 4L)
        val n5 = nd(id = 5L)

        val w1 = way(id = 1L, nodeIds = listOf(1L,2L))
        val w2 = way(id = 2L, nodeIds = listOf(3L,4L))

        val r = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", WAY),
            OsmRelationMember(2L, "", WAY),
            OsmRelationMember(5L, "", NODE),
            OsmRelationMember(2L, "", RELATION),
        ))

        val r2 = rel(id = 2L, members = listOf())

        originalElementsAre(r, r2, w1, w2, n1, n2, n3, n4, n5)
        thereAreNoEditedElements()

        val s = create()
        val data = s.getRelationComplete(1L)!!

        assertTrue(data.relations.containsExactlyInAnyOrder(listOf(r, r2)))
        assertTrue(data.ways.containsExactlyInAnyOrder(listOf(w1,w2)))
        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(n1,n2,n3,n4,n5)))
    }

    @Test
    fun `getRelationComplete returns relation with updated members`() {
        val n1 = nd(id = 1L)
        val n2 = nd(id = 2L)
        val n3 = nd(id = 3L)

        val w = way(id = 1L, nodeIds = listOf(1L,2L))

        val r = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", WAY),
            OsmRelationMember(3L, "", NODE),
            OsmRelationMember(2L, "", RELATION),
        ))

        val r2 = rel(id = 2L, members = listOf())

        originalElementsAre(r, r2, w, n1, n2, n3)

        val n4 = nd(id = 4L)
        val n1New = nd(id = 1L, tags = mapOf("ha" to "huff"))
        val wNew = way(id = 1L, nodeIds = listOf(1L, 4L))
        val r2New = rel(id = 2L, members = listOf())
        r2New.isDeleted = true

        editedElementsAre(n4, wNew, r2New, n1New)

        val s = create()
        val data = s.getRelationComplete(1L)!!

        assertTrue(data.relations.containsExactlyInAnyOrder(listOf(r)))
        assertTrue(data.ways.containsExactlyInAnyOrder(listOf(wNew)))
        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(n1New,n4,n3)))
    }

    //endregion

    //region getWaysForNode

    @Test
    fun `getWaysForNode returns nothing`() {
        thereAreNoOriginalElements()
        thereAreNoEditedElements()

        val s = create()
        val ways = s.getWaysForNode(1L)

        assertTrue(ways.isEmpty())
    }

    @Test
    fun `getWaysForNode returns an original way`() {
        val w = way(id = 1L, nodeIds = listOf(1L, 2L))

        originalElementsAre(w)
        thereAreNoEditedElements()

        val s = create()
        val ways = s.getWaysForNode(1L)

        assertEquals(w, ways.single())
    }

    @Test
    fun `getWaysForNode returns nothing because the updated way has been deleted`() {
        val w = way(id = 1L, nodeIds = listOf(1L, 2L))
        val wNew = way(id = 1L, nodeIds = listOf(1L, 2L))
        wNew.isDeleted = true

        originalElementsAre(w)
        editedElementsAre(wNew)

        val s = create()
        val ways = s.getWaysForNode(1L)

        assertTrue(ways.isEmpty())
    }

    @Test
    fun `getWaysForNode returns an updated way`() {
        val w = way(id = 1L, nodeIds = listOf(1L, 2L))
        val wNew = way(id = 1L, nodeIds = listOf(1L, 2L, 3L))

        originalElementsAre(w)
        editedElementsAre(wNew)

        val s = create()
        val ways = s.getWaysForNode(1L)

        assertEquals(wNew, ways.single())
    }

    @Test
    fun `getWaysForNode returns nothing because the updated way does not contain the node anymore`() {
        val w = way(id = 1L, nodeIds = listOf(1L, 2L))
        val wNew = way(id = 1L, nodeIds = listOf(2L, 3L))

        originalElementsAre(w)
        editedElementsAre(wNew)

        val s = create()
        val ways = s.getWaysForNode(1L)

        assertTrue(ways.isEmpty())
    }

    @Test
    fun `getWaysForNode returns an updated way that didn't contain the node before`() {
        val wNew = way(id = 1L, nodeIds = listOf(1L, 2L))

        thereAreNoOriginalElements()
        editedElementsAre(wNew)

        val s = create()
        val ways = s.getWaysForNode(1L)

        assertEquals(wNew, ways.single())
    }

    //endregion

    //region getRelationsForElement

    @Test
    fun `getRelationsForElement returns nothing`() {
        thereAreNoOriginalElements()
        thereAreNoEditedElements()

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1L)

        assertTrue(relations.isEmpty())
    }

    @Test
    fun `getRelationsForElement returns original relation`() {
        val r = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", NODE)
        ))

        originalElementsAre(r)
        thereAreNoEditedElements()

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1L)

        assertEquals(r, relations.single())
    }

    @Test
    fun `getRelationsForElement returns updated relation`() {
        val r = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", NODE)
        ))

        val rNew = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", NODE),
            OsmRelationMember(2L, "", NODE)
        ))

        originalElementsAre(r)
        editedElementsAre(rNew)

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1L)

        assertEquals(rNew, relations.single())
    }

    @Test
    fun `getRelationsForElement returns nothing because the updated relation has been deleted`() {
        val r = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", NODE)
        ))

        val rNew = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", NODE)
        ))
        rNew.isDeleted = true

        originalElementsAre(r)
        editedElementsAre(rNew)

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1L)

        assertTrue(relations.isEmpty())
    }

    @Test
    fun `getRelationsForElement returns nothing because the updated relation does not contain the element anymore`() {
        val r = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", NODE)
        ))

        val rNew = rel(id = 1L, members = listOf(
            OsmRelationMember(2L, "", NODE)
        ))

        originalElementsAre(r)
        editedElementsAre(rNew)

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1L)

        assertTrue(relations.isEmpty())
    }

    @Test
    fun `getRelationsForElement returns an updated relation that didn't contain the element before`() {
        val r = rel(id = 1L, members = listOf(
            OsmRelationMember(2L, "", NODE)
        ))

        val rNew = rel(id = 1L, members = listOf(
            OsmRelationMember(1L, "", NODE)
        ))

        originalElementsAre(r)
        editedElementsAre(rNew)

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1L)

        assertEquals(rNew, relations.single())
    }

    //endregion

    //region getMapDataWithGeometry

    @Test fun `getMapDataWithGeometry returns nothing`() {
        thereAreNoOriginalElements()
        thereAreNoOriginalGeometries()

        val s = create()
        val bbox = BoundingBox(0.0,0.0,1.0,1.0)
        val data = s.getMapDataWithGeometry(bbox)

        assertTrue(data.toList().isEmpty())
    }

    @Test fun `getMapDataWithGeometry returns original elements`() {
        val nd = nd(1L, lat = 0.5, lon = 0.5)
        val p = pointGeom(0.5, 0.5)
        originalElementsAre(nd)
        originalGeometriesAre(ElementGeometryEntry(NODE, 1L, p))

        val s = create()
        val bbox = BoundingBox(0.0,0.0,1.0,1.0)
        val data = s.getMapDataWithGeometry(bbox)

        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(nd)))
    }

    @Test fun `getMapDataWithGeometry returns updated elements`() {
        val nd = nd(1L, lat = -0.5, lon = 0.5)
        val p = pointGeom(0.5, 0.5)
        originalElementsAre(nd)
        originalGeometriesAre(
            ElementGeometryEntry(NODE, 1L, p)
        )

        val ndInside = nd(1L, lat = 0.1, lon = 0.1)
        val ndOutside = nd(2L, lat = -0.5, lon = 0.1)
        editedElementsAre(ndInside, ndOutside)

        val s = create()
        val bbox = BoundingBox(0.0,0.0,1.0,1.0)
        val data = s.getMapDataWithGeometry(bbox)

        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(ndInside)))
    }

    @Test fun `getMapDataWithGeometry returns nothing because updated element is not in bbox anymore`() {
        val nd = nd(1L, lat = 0.5, lon = 0.5)
        val p = pointGeom(0.5, 0.5)
        originalElementsAre(nd)
        originalGeometriesAre(
            ElementGeometryEntry(NODE, 1L, p)
        )

        val ndNew = nd(1L, lat = -0.1, lon = 0.1)
        editedElementsAre(ndNew)

        val s = create()
        val bbox = BoundingBox(0.0,0.0,1.0,1.0)
        val data = s.getMapDataWithGeometry(bbox)

        assertTrue(data.nodes.isEmpty())
    }

    @Test fun `getMapDataWithGeometry returns nothing because element was deleted`() {
        val nd = nd(1L, lat = 0.5, lon = 0.5)
        val p = pointGeom(0.5, 0.5)
        originalElementsAre(nd)
        originalGeometriesAre(
            ElementGeometryEntry(NODE, 1L, p)
        )

        val ndNew = nd(1L, lat = -0.1, lon = 0.1)
        ndNew.isDeleted = true
        editedElementsAre(ndNew)

        val s = create()
        val bbox = BoundingBox(0.0,0.0,1.0,1.0)
        val data = s.getMapDataWithGeometry(bbox)

        assertTrue(data.nodes.isEmpty())
    }

    //endregion

    //region ElementEditsSource.Listener ::onAddedEdit

    @Test
    fun `onAddedEdit does not relay if no elements were updated`() {
        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        editsControllerNotifiesEditedElementsAdded()

        verifyZeroInteractions(listener)
    }

    @Test
    fun `onAddedEdit relays updated elements`() {
        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val n = nd(1L, lat = 1.0, lon = 10.0)
        val p = ElementGeometryEntry(elementType = NODE, elementId = 1L, geometry = pointGeom(lat = 1.0, lon = 10.0))
        editsControllerNotifiesEditedElementsAdded(n)

        val expectedMapData = MutableMapDataWithGeometry(
            listOf(n),
            listOf(p)
        )

        verify(listener).onUpdated(updated = eq(expectedMapData), deleted = eq(listOf()))

        assertEquals(n, s.get(NODE, 1L))
        assertEquals(p.geometry, s.getGeometry(NODE, 1L))
    }

    @Test
    fun `onAddedEdit relays deleted elements`() {
        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val n = nd(1L, lat = 1.0, lon = 10.0)
        n.isDeleted = true
        editsControllerNotifiesEditedElementsAdded(n)

        verify(listener).onUpdated(
            updated = eq(MutableMapDataWithGeometry()),
            deleted = eq(listOf(ElementKey(NODE, 1L)))
        )

        assertNull(s.get(NODE, 1L))
        assertNull(s.getGeometry(NODE, 1L))
    }

    //endregion

    //region ElementEditsSource.Listener ::onDeletedEdit

    @Test
    fun `onDeletedEdit relays updated element`() {
        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val n = nd(1L, lat = 1.0, lon = 10.0)
        val p = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 1.0, lon = 10.0))

        editedElementsAre(n)

        editsControllerNotifiesDeletedEdit(NODE, 1L, listOf())

        verify(listener).onUpdated(
            updated = eq(MutableMapDataWithGeometry(listOf(n), listOf(p))),
            deleted = eq(listOf())
        )
    }

    @Test
    fun `onDeletedEdit relays elements created by edit as deleted elements`() {
        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val n = nd(1L, lat = 1.0, lon = 10.0)
        val p = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 1.0, lon = 10.0))

        val delElements = listOf(
            ElementKey(NODE, -10),
            ElementKey(WAY, -10),
            ElementKey(RELATION, -10),
        )

        editedElementsAre(n)
        editsControllerNotifiesDeletedEdit(NODE, 1L, delElements)

        verify(listener).onUpdated(
            updated = eq(MutableMapDataWithGeometry(listOf(n), listOf(p))),
            deleted = eq(delElements)
        )
    }

    @Test
    fun `onDeletedEdit does not relay elements created by edit as deleted elements that are not edited as added elements`() {
        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val n = nd(1L, lat = 1.0, lon = 10.0)
        val p = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 1.0, lon = 10.0))

        val n10 = nd(-10)
        val w10 = way(-10)
        val r10 = rel(-10)

        val delElements = listOf(
            ElementKey(NODE, -10),
            ElementKey(WAY, -10),
            ElementKey(RELATION, -10),
        )

        editedElementsAre(n, n10, w10, r10)
        editsControllerNotifiesDeletedEdit(NODE, 1L, delElements)

        verify(listener).onUpdated(
            updated = eq(MutableMapDataWithGeometry(listOf(n), listOf(p))),
            deleted = eq(listOf())
        )
    }

    //endregion

    //region MapDataController.Listener ::onUpdate

    @Test
    fun `onUpdate passes through mapData because there are no edits`() {
        val ndNewOriginal = nd(1L, lat = 0.2, lon = 0.0, tags = mapOf("Iam" to "server version"))
        val pNew = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 0.2, lon = 0.0))

        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val updatedMapData = MutableMapDataWithGeometry(
            elements = listOf(ndNewOriginal),
            geometryEntries = listOf(pNew)
        )
        val deletions = listOf(
            ElementKey(NODE, 2L)
        )
        mapDataListener.onUpdated(updatedMapData, deletions)

        val expectedMapDataWithGeometry = MutableMapDataWithGeometry(
            elements = listOf(ndNewOriginal),
            geometryEntries = listOf(pNew),
        )
        val expectedDeletions = listOf(
            ElementKey(NODE, 2L)
        )
        verify(listener).onUpdated(eq(expectedMapDataWithGeometry), eq(expectedDeletions))
    }

    @Test
    fun `onUpdate applies edits on top of passed mapData`() {
        // 1 is modified,
        // 2 is modified to be deleted,
        // 3 is deleted,
        // 4 was deleted but was modified to be not

        val ndNewOriginal = nd(1L, lat = 0.2, lon = 0.0)
        val pNew = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 0.2, lon = 0.0))
        val ndNewOriginal2 = nd(2L, lat = 0.2, lon = 1.0)
        val pNew2 = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 0.2, lon = 1.0))

        val ndModified = nd(1L, lat = 0.3, lon = 0.0)
        val pModified = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 0.3, lon = 0.0))
        val ndModified2 = nd(2L)
        ndModified2.isDeleted = true

        val ndModified4 = nd(4L, lat = 0.5, lon = 0.4)
        val pModified4 = ElementGeometryEntry(NODE, 4L, pointGeom(lat = 0.5, lon = 0.4))

        editedElementsAre(ndModified, ndModified2, ndModified4)

        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val updatedMapData = MutableMapDataWithGeometry(
            elements = listOf(ndNewOriginal, ndNewOriginal2),
            geometryEntries = listOf(pNew, pNew2)
        )
        val deletions = listOf(
            ElementKey(NODE, 3L),
            ElementKey(NODE, 4L)
        )
        mapDataListener.onUpdated(updatedMapData, deletions)

        val expectedMapDataWithGeometry = MutableMapDataWithGeometry(
            elements = listOf(ndModified, ndModified4),
            geometryEntries = listOf(pModified,pModified4),
        )
        val expectedDeletions = listOf(
            ElementKey(NODE, 2L),
            ElementKey(NODE, 3L)
        )
        verify(listener).onUpdated(eq(expectedMapDataWithGeometry), eq(expectedDeletions))
    }

    //endregion

    //region MapDataController.Listener ::onReplacedForBBox

    @Test
    fun `onReplacedForBBox passes through mapData because there are no edits`() {
        val ndNewOriginal = nd(1L, lat = 0.2, lon = 0.0, tags = mapOf("Iam" to "server version"))
        val pNew = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 0.2, lon = 0.0))

        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val bbox = BoundingBox(0.0,0.0,1.0,1.0)
        val updatedMapData = MutableMapDataWithGeometry(
            elements = listOf(ndNewOriginal),
            geometryEntries = listOf(pNew)
        )
        mapDataListener.onReplacedForBBox(bbox, updatedMapData)

        val expectedMapDataWithGeometry = MutableMapDataWithGeometry(
            elements = listOf(ndNewOriginal),
            geometryEntries = listOf(pNew),
        )
        verify(listener).onReplacedForBBox(eq(bbox), eq(expectedMapDataWithGeometry))
    }

    @Test
    fun `onReplacedForBBox applies edits on top of passed mapData`() {
        val ndModified = nd(1L, lat = 0.3, lon = 0.2, tags = mapOf("Iam" to "modified"))
        val pModified = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 0.3, lon = 0.2))

        val ndNewOriginal = nd(1L, lat = 0.2, lon = 0.0, tags = mapOf("Iam" to "server version"))
        val pNew = ElementGeometryEntry(NODE, 1L, pointGeom(lat = 0.2, lon = 0.0))
        val ndNewOriginal2 = nd(2L, lat = 0.8, lon = 0.1, tags = mapOf("Iam" to "server version"))
        val pNew2 = ElementGeometryEntry(NODE, 2L, pointGeom(lat = 0.8, lon = 0.1))

        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        editedElementsAre(ndModified)

        val bbox = BoundingBox(0.0,0.0,1.0,1.0)
        val updatedMapData = MutableMapDataWithGeometry(
            elements = listOf(ndNewOriginal, ndNewOriginal2),
            geometryEntries = listOf(pNew, pNew2)
        )
        mapDataListener.onReplacedForBBox(bbox, updatedMapData)

        val expectedMapDataWithGeometry = MutableMapDataWithGeometry(
            elements = listOf(ndModified, ndNewOriginal2),
            geometryEntries = listOf(pModified, pNew2),
        )
        verify(listener).onReplacedForBBox(eq(bbox), eq(expectedMapDataWithGeometry))
    }

    // I spare myself more tests for onReplacedForBBox here because it internally does the same as
    // getMapDataWithGeometry

    //endregion

    private fun create() = MapDataWithEditsSource(mapDataCtrl, editsCtrl, geometryCreator)

    /** Feed mock MapDataController the data */
    private fun originalGeometriesAre(vararg elementGeometryEntries: ElementGeometryEntry) {
        for (entry in elementGeometryEntries) {
            mapData.putGeometry(entry.elementType, entry.elementId, entry.geometry)
        }
    }

    private fun thereAreNoOriginalGeometries() {
        originalGeometriesAre()
    }

    /** Feed mock MapDataController the data */
    private fun originalElementsAre(vararg elements: Element) {
        for (element in elements) {
            mapData.putElement(element)
        }
    }

    private fun thereAreNoOriginalElements() {
        originalElementsAre()
    }

    private fun editedElementsAre(vararg elements: Element) {
        val action = mock<ElementEditAction>()
        on(action.createUpdates(any(), any(), any())).thenReturn(elements.toList())
        on(editsCtrl.getAllUnsynced()).thenReturn(listOf(edit(
            elementType = NODE,
            elementId = -1L,
            action = action
        )))
    }

    private fun thereAreNoEditedElements() {
        editedElementsAre()
    }

    private fun editsControllerNotifiesEditedElementsAdded(vararg elements: Element) {
        val action = mock<ElementEditAction>()
        on(action.createUpdates(any(), any(), any())).thenReturn(elements.toList())
        editsListener.onAddedEdit(edit(
            elementType = NODE,
            elementId = -1L,
            action = action
        ))
    }

    private fun editsControllerNotifiesDeletedEdit(elementType: Element.Type, elementId: Long, createdElementKeys: List<ElementKey>) {
        on(editsCtrl.getIdProvider(anyLong())).thenReturn(ElementIdProvider(createdElementKeys))
        val action = mock<ElementEditAction>()
        on(action.newElementsCount).thenReturn(NewElementsCount(
            nodes = createdElementKeys.count { it.elementType == NODE },
            ways = createdElementKeys.count { it.elementType == WAY },
            relations = createdElementKeys.count { it.elementType == RELATION }
        ))
        editsListener.onDeletedEdit(edit(
            elementType = elementType,
            elementId = elementId,
            action = action
        ))
    }

}

private fun nd(
    id: Long = 1L,
    version: Int = 1,
    lat: Double = 1.0,
    lon: Double = 2.0,
    tags: Map<String,String>? = null
) = OsmNode(id, version, lat, lon, tags)

private fun way(
    id: Long = 1L,
    version: Int = 1,
    nodeIds: List<Long> = listOf(),
    tags: Map<String,String>? = null
) = OsmWay(id, version, nodeIds, tags)

private fun rel(
    id: Long = 1L,
    version: Int = 1,
    members: List<RelationMember> = listOf(),
    tags: Map<String, String>? = null
) = OsmRelation(id, version, members, tags)

private fun pointGeom(lat: Double, lon: Double) = ElementPointGeometry(OsmLatLon(lat, lon))

private fun edit(
    elementType: Element.Type = NODE,
    elementId: Long = -1L,
    pos: OsmLatLon = OsmLatLon(0.0,0.0),
    timestamp: Long = 123L,
    action: ElementEditAction
) = ElementEdit(
    1L,
    TEST_QUEST_TYPE,
    elementType,
    elementId,
    "survey",
    pos,
    timestamp,
    false,
    action
)

private val TEST_QUEST_TYPE = TestQuestTypeA()
