package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.data.Element.Type.*
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.testutils.*
import de.westnordost.streetcomplete.util.intersect
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*

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
        mapData.putElement(node(-1, pos = p(60.0, 60.0)))

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
                when(it.type) {
                    NODE -> mapData.getNode(it.id)
                    WAY -> mapData.getWay(it.id)
                    RELATION -> mapData.getRelation(it.id)
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
        on(mapDataCtrl.getGeometries(any())).thenAnswer { invocation ->
            val keys = invocation.getArgument<Collection<ElementKey>>(0)!!
            keys.mapNotNull { key ->
                when(key.type) {
                    NODE -> mapData.getNodeGeometry(key.id)
                    WAY -> mapData.getWayGeometry(key.id)
                    RELATION -> mapData.getRelationGeometry(key.id)
                }?.let {
                    ElementGeometryEntry(
                        key.type,
                        key.id,
                        it
                    )
                }
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
        assertNull(s.get(NODE, 1))
    }

    @Test
    fun `get returns original element`() {
        val nd = node(1)

        originalElementsAre(nd)
        thereAreNoEditedElements()

        val s = create()
        assertEquals(nd, s.get(NODE, 1))
    }

    @Test
    fun `get returns updated element`() {
        val nd = node(1)
        val nd2 = node(1, tags = mapOf("bla" to "blub"))

        originalElementsAre(nd)
        editedElementsAre(nd2)

        val s = create()
        assertEquals(nd2, s.get(NODE, 1))
    }

    @Test
    fun `get returns updated element updated from updated element`() {
        val nd = node(1)
        val nd2 = node(1, tags = mapOf("bla" to "blub"))
        val nd3 = node(1, tags = mapOf("bla" to "flop"))

        originalElementsAre(nd)

        val action2 = mock<ElementEditAction>()
        on(action2.createUpdates(eq(nd), any(), any())).thenReturn(listOf(nd2))
        val action3 = mock<ElementEditAction>()
        on(action3.createUpdates(eq(nd2), any(), any())).thenReturn(listOf(nd3))
        on(editsCtrl.getAllUnsynced()).thenReturn(listOf(
            edit(
                elementType = NODE,
                elementId = 1,
                action = action2
            ),
            edit(
                elementType = NODE,
                elementId = 1,
                action = action3
            ),
        ))

        val s = create()
        assertEquals(nd3, s.get(NODE, 1))
    }

    @Test
    fun `get returns null if updated element was deleted`() {
        val nd = node(1)
        val nd2 = node(1)
        nd2.isDeleted = true

        originalElementsAre(nd)
        editedElementsAre(nd2)

        val s = create()
        assertNull(s.get(NODE, 1))
    }

    @Test
    fun `conflict on applying edit is ignored`() {
        val nd = node(1)

        originalElementsAre(nd)

        val action = mock<ElementEditAction>()
        on(action.createUpdates(eq(nd), any(), any())).thenThrow(ConflictException())
        on(editsCtrl.getAllUnsynced()).thenReturn(listOf(edit(
            elementType = NODE,
            elementId = 1,
            action = action
        )))

        val s = create()
        assertEquals(nd, s.get(NODE, 1))
    }

    //endregion

    //region getGeometry

    @Test
    fun `getGeometry returns nothing`() {
        thereAreNoOriginalGeometries()
        thereAreNoEditedElements()

        val s = create()
        assertNull(s.getGeometry(NODE, 1))

        assertEquals(emptyList<ElementGeometryEntry>(), s.getGeometries(listOf(ElementKey(NODE, 1))))
    }

    @Test
    fun `getGeometry returns original geometry`() {
        val p = pGeom(0.0, 0.0)

        originalGeometriesAre(ElementGeometryEntry(NODE, 1, p))
        thereAreNoEditedElements()

        val s = create()
        assertEquals(p, s.getGeometry(NODE, 1))

        assertEquals(
            listOf(ElementGeometryEntry(NODE, 1, p)),
            s.getGeometries(listOf(ElementKey(NODE, 1)))
        )
    }

    @Test
    fun `getGeometry returns updated geometry`() {
        val nd = node(1, pos = p(0.0, 0.0))
        val p = pGeom(0.0, 0.0)
        val nd2 = node(1, pos = p(1.0, 2.0))
        val p2 = pGeom(1.0, 2.0)

        originalElementsAre(nd)
        originalGeometriesAre(ElementGeometryEntry(NODE, 1, p))
        editedElementsAre(nd2)

        val s = create()
        assertEquals(p2, s.getGeometry(NODE, 1))

        assertEquals(
            listOf(ElementGeometryEntry(NODE, 1, p2)),
            s.getGeometries(listOf(ElementKey(NODE, 1)))
        )
    }

    @Test
    fun `getGeometry returns updated geometry updated from updated element`() {
        val nd = node(1)
        val nd2 = node(1, pos = p(1.0,2.0))
        val nd3 = node(1, pos = p(55.0,56.0))
        val p = pGeom(0.0, 0.0)
        val p3 = pGeom(55.0, 56.0)

        originalElementsAre(nd)
        originalGeometriesAre(ElementGeometryEntry(NODE, 1, p))

        val action2 = mock<ElementEditAction>()
        on(action2.createUpdates(eq(nd), any(), any())).thenReturn(listOf(nd2))
        val action3 = mock<ElementEditAction>()
        on(action3.createUpdates(eq(nd2), any(), any())).thenReturn(listOf(nd3))
        on(editsCtrl.getAllUnsynced()).thenReturn(listOf(
            edit(
                elementType = NODE,
                elementId = 1,
                action = action2
            ),
            edit(
                elementType = NODE,
                elementId = 1,
                action = action3
            ),
        ))

        val s = create()
        assertEquals(p3, s.getGeometry(NODE, 1))

        assertEquals(
            listOf(ElementGeometryEntry(NODE, 1, p3)),
            s.getGeometries(listOf(ElementKey(NODE, 1)))
        )
    }

    @Test
    fun `getGeometry returns null if element was deleted`() {
        val nd = node(1)
        val nd2 = node(1)
        val p = pGeom(0.0, 0.0)
        nd2.isDeleted = true

        originalElementsAre(nd)
        originalGeometriesAre(ElementGeometryEntry(NODE, 1, p))
        editedElementsAre(nd2)

        val s = create()
        assertNull(s.getGeometry(NODE, 1))

        assertEquals(
            emptyList<ElementGeometryEntry>(),
            s.getGeometries(listOf(ElementKey(NODE, 1)))
        )
    }

    @Test
    fun `getGeometry returns null if element was updated with invalid geometry`() {
        val way = way(1, listOf(1,2))
        val wayNew = way(1, listOf())
        val p1 = pGeom(0.0, 0.0)
        val p2 = pGeom(1.0, 0.0)

        originalElementsAre(way)
        originalGeometriesAre(
            ElementGeometryEntry(NODE, 1, p1),
            ElementGeometryEntry(NODE, 2, p2)
        )
        editedElementsAre(wayNew)

        val s = create()
        assertNull(s.getGeometry(WAY, 1))

        assertEquals(
            emptyList<ElementGeometryEntry>(),
            s.getGeometries(listOf(ElementKey(WAY, 1)))
        )
    }

    //endregion

    //region getWayComplete

    @Test
    fun `getWayComplete returns null`() {
        thereAreNoOriginalElements()
        thereAreNoEditedElements()

        val s = create()
        assertNull(s.getWayComplete(1))
    }

    @Test
    fun `getWayComplete returns null because it is not complete`() {
        val w = way(1, listOf(1,2,3))

        originalElementsAre(w, node(1), node(2))
        thereAreNoEditedElements()

        val s = create()
        assertNull(s.getWayComplete(1))
    }

    @Test
    fun `getWayComplete returns original way with original node ids`() {
        val w = way(1, listOf(1,2,3))
        val n1 = node(1)
        val n2 = node(2)
        val n3 = node(3)

        originalElementsAre(w, n1, n2, n3)
        thereAreNoEditedElements()

        val s = create()
        val data = s.getWayComplete(1)!!
        assertEquals(w, data.ways.single())
        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(n1,n2,n3)))
    }

    @Test
    fun `getWayComplete returns original way with updated node ids`() {
        val w = way(1, listOf(1,2,3))
        val nd1 = node(1)
        val nd2 = node(2)
        val nd3 = node(3)
        val nd1New = node(1, tags = mapOf("foo" to "bar"))

        originalElementsAre(nd1, nd2, nd3, w)
        editedElementsAre(nd1New)

        val s = create()
        val data = s.getWayComplete(1)!!
        assertEquals(w, data.ways.single())
        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(nd1New, nd2, nd3)))
    }

    @Test
    fun `getWayComplete returns updated way with updated node ids`() {
        val w = way(1, listOf(1,2))
        val wNew = way(1, listOf(3, 1))

        val nd1 = node(1)
        val nd2 = node(2)

        val nd1New = node(1, tags = mapOf("foo" to "bar"))
        val nd2New = node(2)
        nd2New.isDeleted = true
        val nd3New = node(3)

        originalElementsAre(nd1, nd2, w)
        editedElementsAre(nd1New, nd2New, nd3New, wNew)

        val s = create()
        val data = s.getWayComplete(1)!!
        assertEquals(wNew, data.ways.single())
        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(nd1New, nd3New)))
    }

    @Test
    fun `getWayComplete returns null because a node of the way was deleted`() {
        val w = way(1, listOf(1,2))
        val nd1 = node(1)
        val nd1New = node(1)
        nd1New.isDeleted = true

        originalElementsAre(w, nd1)
        editedElementsAre(nd1New)

        val s = create()
        assertNull(s.getWayComplete(1))
    }

    //endregion

    //region getRelationComplete

    @Test
    fun `getRelationComplete returns null`() {
        thereAreNoOriginalElements()
        thereAreNoEditedElements()

        val s = create()
        assertNull(s.getRelationComplete(1))
    }

    @Test
    fun `getRelationComplete returns incomplete relation`() {
        val r = rel(1, listOf(
            member(WAY, 1),
            member(WAY, 2),
        ))
        val w = way(1, listOf(1,2))
        val n1 = node(1)
        val n2 = node(2)

        originalElementsAre(r, w, n1, n2)
        thereAreNoEditedElements()

        val s = create()
        val data = s.getRelationComplete(1)!!
        assertEquals(w, data.ways.single())
        assertEquals(r, data.relations.single())
    }

    @Test
    fun `getRelationComplete returns original relation with original members`() {
        val n1 = node(1)
        val n2 = node(2)
        val n3 = node(3)
        val n4 = node(4)
        val n5 = node(5)

        val w1 = way(1, listOf(1,2))
        val w2 = way(2, listOf(3,4))

        val r = rel(1, listOf(
            member(WAY, 1),
            member(WAY, 2),
            member(NODE, 5),
            member(RELATION, 2),
        ))

        val r2 = rel(2)

        originalElementsAre(r, r2, w1, w2, n1, n2, n3, n4, n5)
        thereAreNoEditedElements()

        val s = create()
        val data = s.getRelationComplete(1)!!

        assertTrue(data.relations.containsExactlyInAnyOrder(listOf(r, r2)))
        assertTrue(data.ways.containsExactlyInAnyOrder(listOf(w1,w2)))
        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(n1,n2,n3,n4,n5)))
    }

    @Test
    fun `getRelationComplete returns relation with updated members`() {
        val n1 = node(1)
        val n2 = node(2)
        val n3 = node(3)

        val w = way(1, listOf(1,2))

        val r = rel(1, listOf(
            member(WAY, 1),
            member(NODE, 3),
            member(RELATION,2),
        ))

        val r2 = rel(2)

        originalElementsAre(r, r2, w, n1, n2, n3)

        val n4 = node(4)
        val n1New = node(1, tags = mapOf("ha" to "huff"))
        val wNew = way(1, listOf(1, 4))
        val r2New = rel(2)
        r2New.isDeleted = true

        editedElementsAre(n4, wNew, r2New, n1New)

        val s = create()
        val data = s.getRelationComplete(1)!!

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
        val ways = s.getWaysForNode(1)

        assertTrue(ways.isEmpty())
    }

    @Test
    fun `getWaysForNode returns an original way`() {
        val w = way(1, listOf(1, 2))

        originalElementsAre(w)
        thereAreNoEditedElements()

        val s = create()
        val ways = s.getWaysForNode(1)

        assertEquals(w, ways.single())
    }

    @Test
    fun `getWaysForNode returns nothing because the updated way has been deleted`() {
        val w = way(1, listOf(1, 2))
        val wNew = way(1, listOf(1, 2))
        wNew.isDeleted = true

        originalElementsAre(w)
        editedElementsAre(wNew)

        val s = create()
        val ways = s.getWaysForNode(1)

        assertTrue(ways.isEmpty())
    }

    @Test
    fun `getWaysForNode returns an updated way`() {
        val w = way(1, listOf(1, 2))
        val wNew = way(1, listOf(1, 2, 3))

        originalElementsAre(w)
        editedElementsAre(wNew)

        val s = create()
        val ways = s.getWaysForNode(1)

        assertEquals(wNew, ways.single())
    }

    @Test
    fun `getWaysForNode returns nothing because the updated way does not contain the node anymore`() {
        val w = way(1, listOf(1, 2))
        val wNew = way(1, listOf(2, 3))

        originalElementsAre(w)
        editedElementsAre(wNew)

        val s = create()
        val ways = s.getWaysForNode(1)

        assertTrue(ways.isEmpty())
    }

    @Test
    fun `getWaysForNode returns an updated way that didn't contain the node before`() {
        val wNew = way(1,listOf(1, 2))

        thereAreNoOriginalElements()
        editedElementsAre(wNew)

        val s = create()
        val ways = s.getWaysForNode(1)

        assertEquals(wNew, ways.single())
    }

    //endregion

    //region getRelationsForElement

    @Test
    fun `getRelationsForElement returns nothing`() {
        thereAreNoOriginalElements()
        thereAreNoEditedElements()

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1)

        assertTrue(relations.isEmpty())
    }

    @Test
    fun `getRelationsForElement returns original relation`() {
        val r = rel(1, listOf(member(NODE, 1)))

        originalElementsAre(r)
        thereAreNoEditedElements()

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1)

        assertEquals(r, relations.single())
    }

    @Test
    fun `getRelationsForElement returns updated relation`() {
        val r = rel(1, listOf(member(NODE, 1)))
        val rNew = rel(1, listOf(
            member(NODE, 1),
            member(NODE, 2)
        ))

        originalElementsAre(r)
        editedElementsAre(rNew)

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1)

        assertEquals(rNew, relations.single())
    }

    @Test
    fun `getRelationsForElement returns nothing because the updated relation has been deleted`() {
        val r = rel(1, listOf(member(NODE, 1)))
        val rNew = rel(1, listOf(member(NODE, 1)))
        rNew.isDeleted = true

        originalElementsAre(r)
        editedElementsAre(rNew)

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1)

        assertTrue(relations.isEmpty())
    }

    @Test
    fun `getRelationsForElement returns nothing because the updated relation does not contain the element anymore`() {
        val r = rel(1, listOf(member(NODE, 1)))
        val rNew = rel(1, listOf(member(NODE, 2)))

        originalElementsAre(r)
        editedElementsAre(rNew)

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1)

        assertTrue(relations.isEmpty())
    }

    @Test
    fun `getRelationsForElement returns an updated relation that didn't contain the element before`() {
        val r = rel(1, listOf(member(NODE, 2)))
        val rNew = rel(1, listOf(member(NODE, 1)))

        originalElementsAre(r)
        editedElementsAre(rNew)

        val s = create()
        val relations = s.getRelationsForElement(NODE, 1)

        assertEquals(rNew, relations.single())
    }

    //endregion

    //region getMapDataWithGeometry

    @Test fun `getMapDataWithGeometry returns nothing`() {
        thereAreNoOriginalElements()
        thereAreNoOriginalGeometries()

        val s = create()
        val data = s.getMapDataWithGeometry(bbox())

        assertTrue(data.toList().isEmpty())
    }

    @Test fun `getMapDataWithGeometry returns original elements`() {
        val nd = node(1, p(0.5,0.5))
        val p = pGeom(0.5, 0.5)
        originalElementsAre(nd)
        originalGeometriesAre(ElementGeometryEntry(NODE, 1, p))

        val s = create()
        val data = s.getMapDataWithGeometry(bbox())

        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(nd)))
    }

    @Test fun `getMapDataWithGeometry returns updated elements`() {
        val nd = node(1, p(-0.5,0.5))
        val p = pGeom(0.5, 0.5)
        originalElementsAre(nd)
        originalGeometriesAre(
            ElementGeometryEntry(NODE, 1, p)
        )

        val ndInside = node(1, pos = p(0.1,0.1))
        val ndOutside = node(2, pos = p(-0.5,0.1))
        editedElementsAre(ndInside, ndOutside)

        val s = create()
        val data = s.getMapDataWithGeometry(bbox())

        assertTrue(data.nodes.containsExactlyInAnyOrder(listOf(ndInside)))
    }

    @Test fun `getMapDataWithGeometry returns nothing because updated element is not in bbox anymore`() {
        val nd = node(1, p(0.5,0.5))
        val p = pGeom(0.5, 0.5)
        originalElementsAre(nd)
        originalGeometriesAre(
            ElementGeometryEntry(NODE, 1, p)
        )

        val ndNew = node(1, p(-0.1,0.1))
        editedElementsAre(ndNew)

        val s = create()
        val data = s.getMapDataWithGeometry(bbox())

        assertTrue(data.nodes.isEmpty())
    }

    @Test fun `getMapDataWithGeometry returns nothing because element was deleted`() {
        val nd = node(1, p(0.5,0.5))
        val p = pGeom(0.5, 0.5)
        originalElementsAre(nd)
        originalGeometriesAre(
            ElementGeometryEntry(NODE, 1, p)
        )

        val ndNew = node(1, p(-0.1,0.1))
        ndNew.isDeleted = true
        editedElementsAre(ndNew)

        val s = create()
        val data = s.getMapDataWithGeometry(bbox())

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

        verifyNoInteractions(listener)
    }

    @Test
    fun `onAddedEdit relays updated elements`() {
        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val n = node(1, p(1.0,10.0))
        val p = ElementGeometryEntry(elementType = NODE, elementId = 1, geometry = pGeom(1.0,10.0))
        editsControllerNotifiesEditedElementsAdded(n)

        val expectedMapData = MutableMapDataWithGeometry(
            listOf(n),
            listOf(p)
        )

        verify(listener).onUpdated(updated = eq(expectedMapData), deleted = eq(listOf()))

        assertEquals(n, s.get(NODE, 1))
        assertEquals(p.geometry, s.getGeometry(NODE, 1))
    }

    @Test
    fun `onAddedEdit relays deleted elements`() {
        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val n = node(1, p(1.0,10.0))
        n.isDeleted = true
        editsControllerNotifiesEditedElementsAdded(n)

        verify(listener).onUpdated(
            updated = eq(MutableMapDataWithGeometry()),
            deleted = eq(listOf(ElementKey(NODE, 1)))
        )

        assertNull(s.get(NODE, 1))
        assertNull(s.getGeometry(NODE, 1))
    }

    //endregion

    //region ElementEditsSource.Listener ::onDeletedEdit

    @Test
    fun `onDeletedEdit relays updated element`() {
        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val n = node(1, p(1.0,10.0))
        val p = ElementGeometryEntry(NODE, 1, pGeom(1.0,10.0))

        editedElementsAre(n)

        editsControllerNotifiesDeletedEdit(NODE, 1, listOf())

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

        val n = node(1, p(1.0,10.0))
        val p = ElementGeometryEntry(NODE, 1, pGeom(1.0,10.0))

        val delElements = listOf(
            ElementKey(NODE, -10),
            ElementKey(WAY, -10),
            ElementKey(RELATION, -10),
        )

        editedElementsAre(n)
        editsControllerNotifiesDeletedEdit(NODE, 1, delElements)

        verify(listener).onUpdated(
            updated = eq(MutableMapDataWithGeometry(listOf(n), listOf(p))),
            deleted = eq(delElements)
        )
    }

    //endregion

    //region MapDataController.Listener ::onUpdate

    @Test
    fun `onUpdate passes through mapData because there are no edits`() {
        val ndNewOriginal = node(1, p(0.2,0.0), mapOf("Iam" to "server version"))
        val pNew = ElementGeometryEntry(NODE, 1, pGeom(0.2,0.0))

        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val updatedMapData = MutableMapDataWithGeometry(
            elements = listOf(ndNewOriginal),
            geometryEntries = listOf(pNew)
        )
        val deletions = listOf(
            ElementKey(NODE, 2)
        )
        mapDataListener.onUpdated(updatedMapData, deletions)

        val expectedMapDataWithGeometry = MutableMapDataWithGeometry(
            elements = listOf(ndNewOriginal),
            geometryEntries = listOf(pNew),
        )
        val expectedDeletions = listOf(
            ElementKey(NODE, 2)
        )
        verify(listener).onUpdated(eq(expectedMapDataWithGeometry), eq(expectedDeletions))
    }

    @Test
    fun `onUpdate applies edits on top of passed mapData`() {
        // 1 is modified,
        // 2 is modified to be deleted,
        // 3 is deleted,
        // 4 was deleted but was modified to be not

        val ndNewOriginal = node(1, p(0.2,0.0))
        val pNew = ElementGeometryEntry(NODE, 1, pGeom(0.2,0.0))
        val ndNewOriginal2 = node(2, p(0.2,1.0))
        val pNew2 = ElementGeometryEntry(NODE, 1, pGeom(0.2,1.0))

        val ndModified = node(1, p(0.3,0.0))
        val pModified = ElementGeometryEntry(NODE, 1, pGeom(0.3,0.0))
        val ndModified2 = node(2)
        ndModified2.isDeleted = true

        val ndModified4 = node(4, p(0.5,0.4))
        val pModified4 = ElementGeometryEntry(NODE, 4, pGeom(0.5,0.4))

        editedElementsAre(ndModified, ndModified2, ndModified4)

        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val updatedMapData = MutableMapDataWithGeometry(
            elements = listOf(ndNewOriginal, ndNewOriginal2),
            geometryEntries = listOf(pNew, pNew2)
        )
        val deletions = listOf(
            ElementKey(NODE, 3),
            ElementKey(NODE, 4)
        )
        mapDataListener.onUpdated(updatedMapData, deletions)

        val expectedMapDataWithGeometry = MutableMapDataWithGeometry(
            elements = listOf(ndModified, ndModified4),
            geometryEntries = listOf(pModified,pModified4),
        )
        val expectedDeletions = listOf(
            ElementKey(NODE, 2),
            ElementKey(NODE, 3)
        )
        verify(listener).onUpdated(eq(expectedMapDataWithGeometry), eq(expectedDeletions))
    }

    //endregion

    //region MapDataController.Listener ::onReplacedForBBox

    @Test
    fun `onReplacedForBBox passes through mapData because there are no edits`() {
        val ndNewOriginal = node(1, p(0.2,0.0), mapOf("Iam" to "server version"))
        val pNew = ElementGeometryEntry(NODE, 1, pGeom(0.2,0.0))

        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        val bbox = bbox()
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
        val ndModified = node(1, p(0.3,0.2), mapOf("Iam" to "modified"))
        val pModified = ElementGeometryEntry(NODE, 1, pGeom(0.3,0.2))

        val ndNewOriginal = node(1, p(0.2,0.0), mapOf("Iam" to "server version"))
        val pNew = ElementGeometryEntry(NODE, 1, pGeom(0.2,0.0))
        val ndNewOriginal2 = node(2, p(0.8,0.1), mapOf("Iam" to "server version"))
        val pNew2 = ElementGeometryEntry(NODE, 2, pGeom(0.8,0.1))

        val s = create()
        val listener = mock<MapDataWithEditsSource.Listener>()
        s.addListener(listener)

        editedElementsAre(ndModified)

        val bbox = bbox()
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
            elementId = -1,
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
            elementId = -1,
            action = action
        ))
    }

    private fun editsControllerNotifiesDeletedEdit(elementType: Element.Type, elementId: Long, createdElementKeys: List<ElementKey>) {
        on(editsCtrl.getIdProvider(anyLong())).thenReturn(ElementIdProvider(createdElementKeys))
        editsListener.onDeletedEdits(listOf(edit(
            elementType = elementType,
            elementId = elementId
        )))
    }

}
