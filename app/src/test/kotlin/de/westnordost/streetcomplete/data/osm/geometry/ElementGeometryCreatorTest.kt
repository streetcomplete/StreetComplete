package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapData
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.RelationMember
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ElementGeometryCreatorTest {

    @Test fun `create for node`() {
        val g = create(node(0, P0))
        assertEquals(P0, g.center)
    }

    @Test fun `create for empty way`() {
        val geom = create(EMPTY_WAY)
        assertNull(geom)
    }

    @Test fun `create for way with duplicate nodes`() {
        val geom = create(WAY_DUPLICATE_NODES) as ElementPolylinesGeometry
        assertEquals(listOf(P0, P1, P2), geom.polylines.single())
        assertEquals(P1, geom.center)
    }

    @Test fun `create for simple area way`() {
        val geom = create(AREA_WAY) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
        assertEquals(O, geom.center)
    }

    @Test fun `create for simple clockwise-area way`() {
        val geom = create(AREA_WAY_CLOCKWISE) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
    }

    @Test fun `create for simple non-area way`() {
        val geom = create(SIMPLE_WAY1) as ElementPolylinesGeometry
        assertEquals(listOf(P0, P1), geom.polylines.single())
    }

    @Test fun `create for multipolygon relation with single empty way`() {
        val geom = create(areaRelation(asOuters(EMPTY_WAY)))
        assertNull(geom)
    }

    @Test fun `create for multipolygon relation with single way with no role`() {
        val geom = create(areaRelation(asMembers(AREA_WAY)))
        assertNull(geom)
    }

    @Test fun `create for multipolygon relation with single outer way`() {
        val geom = create(areaRelation(asOuters(AREA_WAY))) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
        assertEquals(O, geom.center)
    }

    @Test fun `create for multipolygon relation with single outer clockwise way`() {
        val geom = create(areaRelation(asOuters(AREA_WAY_CLOCKWISE))) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
    }

    @Test fun `create for multipolygon relation with outer composed of several ways`() {
        val geom = create(areaRelation(asOuters(SIMPLE_WAY1, SIMPLE_WAY3, SIMPLE_WAY2))) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
    }

    @Test fun `create for multipolygon relation consisting solely of a hole`() {
        val geom = create(areaRelation(asInners(AREA_WAY)))
        assertNull(geom)
    }

    @Test fun `create for multipolygon relation with hole consisting of single way`() {
        val geom = create(areaRelation(asOuters(AREA_WAY) + asInners(AREA_WAY_CLOCKWISE))) as ElementPolygonsGeometry
        assertEquals(listOf(CCW_RING, CW_RING), geom.polygons)
        assertEquals(O, geom.center)
    }

    @Test fun `create for multipolygon relation with hole consisting of single counterclockwise way`() {
        val geom = create(areaRelation(asOuters(AREA_WAY) + asInners(AREA_WAY))) as ElementPolygonsGeometry
        assertEquals(listOf(CCW_RING, CW_RING), geom.polygons)
        assertEquals(O, geom.center)
    }

    @Test fun `create for multipolygon relation with hole consisting of several ways`() {
        val geom = create(areaRelation(asOuters(AREA_WAY) + asInners(SIMPLE_WAY1, SIMPLE_WAY3, SIMPLE_WAY2))) as ElementPolygonsGeometry
        assertEquals(listOf(CCW_RING, CW_RING), geom.polygons)
        assertEquals(O, geom.center)
    }

    @Test fun `creating for multipolygon relation ignores unusable parts`() {
        val geom = create(areaRelation(
            asOuters(EMPTY_WAY, AREA_WAY, SIMPLE_WAY1) +
            asInners(EMPTY_WAY) +
            asMembers(AREA_WAY)
        )) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
        assertEquals(O, geom.center)
    }

    @Test fun `create for polyline relation with single empty way`() {
        val geom = create(rel(members = asMembers(EMPTY_WAY)))
        assertNull(geom)
    }

    @Test fun `create for polyline relation with single way`() {
        val geom = create(rel(members = asMembers(AREA_WAY))) as ElementPolylinesGeometry
        assertEquals(CCW_RING, geom.polylines.single())
    }

    @Test fun `create for polyline relation with two ways`() {
        val geom = create(rel(members = asMembers(AREA_WAY, SIMPLE_WAY1))) as ElementPolylinesGeometry
        assertTrue(geom.polylines.containsAll(listOf(CCW_RING, listOf(P0, P1))))
    }

    @Test fun `create for polyline relation with ways joined together`() {
        val geom = create(rel(members = asMembers(SIMPLE_WAY1, SIMPLE_WAY2, SIMPLE_WAY3, WAY_DUPLICATE_NODES))) as ElementPolylinesGeometry
        assertTrue(geom.polylines.containsAll(listOf(CCW_RING, listOf(P0, P1, P2))))
    }

    @Test fun `positions for way`() {
        val nodes = listOf(
            node(0, P0),
            node(1, P1)
        )
        val mapData = MutableMapData(nodes)
        val geom = create(SIMPLE_WAY1, mapData) as ElementPolylinesGeometry
        assertEquals(listOf(nodes.map { it.position }), geom.polylines)
    }

    @Test fun `returns null for non-existent way`() {
        val way = way(1, listOf(1, 2, 3))
        assertNull(create(way, MutableMapData()))
    }

    @Test fun `positions for relation`() {
        val relation = rel(1, listOf(
            member(WAY, 0),
            member(WAY, 1),
            member(NODE, 1)
        ))

        val ways = listOf(SIMPLE_WAY1, SIMPLE_WAY2)
        val nodesByWayId = mapOf(
            0L to listOf(
                node(0, P0),
                node(1, P1)
            ),
            1L to listOf(
                node(1, P1),
                node(2, P2),
                node(3, P3)
            )
        )
        val mapData = MutableMapData(nodesByWayId.values.flatten() + ways)
        val positions = listOf(P0, P1, P2, P3)
        val geom = create(relation, mapData) as ElementPolylinesGeometry
        assertEquals(listOf(positions), geom.polylines)
    }

    @Test fun `returns null for non-existent relation`() {
        val relation = rel(1, listOf(
            member(WAY, 1),
            member(WAY, 2),
            member(NODE, 1)
        ))
        assertNull(create(relation, MutableMapData()))
    }

    @Test fun `returns null for relation with a way that's missing from map data`() {
        val relation = rel(1, listOf(
            member(WAY, 0),
            member(WAY, 1)
        ))
        val mapData = MutableMapData(listOf(
            relation,
            way(0, listOf(0, 1)),
            node(0, P0),
            node(1, P1)
        ))

        assertNull(create(relation, mapData))
    }

    @Test fun `does not return null for relation with a way that's missing from map data if returning incomplete geometries is ok`() {
        val relation = rel(1, listOf(
            member(WAY, 0),
            member(WAY, 1)
        ))
        val way = way(0, listOf(0, 1))
        val mapData = MutableMapData(listOf(
            relation,
            way,
            node(0, P0),
            node(1, P1)
        ))

        assertEquals(
            create(way),
            create(relation, mapData, true)
        )
    }
}

private fun create(node: Node) =
    ElementGeometryCreator().create(node)

private fun create(way: Way) =
    ElementGeometryCreator().create(way, WAY_GEOMETRIES[way.id] ?: emptyList())

private fun create(relation: Relation) =
    ElementGeometryCreator().create(relation, WAY_GEOMETRIES)

private fun create(element: Element, mapData: MapData, allowIncomplete: Boolean = false) =
    ElementGeometryCreator().create(element, mapData, allowIncomplete)

private val WAY_AREA = mapOf("area" to "yes")

private val O = p(1.0, 1.0)
private val P0 = p(0.0, 2.0)
private val P1 = p(0.0, 0.0)
private val P2 = p(2.0, 0.0)
private val P3 = p(2.0, 2.0)

private val SIMPLE_WAY1 = way(0, listOf(0, 1))
private val SIMPLE_WAY2 = way(1, listOf(1, 2, 3))
private val SIMPLE_WAY3 = way(2, listOf(0, 3))
private val AREA_WAY = way(4, listOf(0, 1, 2, 3, 0), WAY_AREA)
private val AREA_WAY_CLOCKWISE = way(5, listOf(0, 3, 2, 1, 0), WAY_AREA)
private val WAY_DUPLICATE_NODES = way(6, listOf(0, 1, 1, 2))
private val EMPTY_WAY = way(7, emptyList())

private val CCW_RING = listOf(P0, P1, P2, P3, P0)
private val CW_RING = listOf(P0, P3, P2, P1, P0)

private val WAY_GEOMETRIES = mapOf(
    0L to listOf(P0, P1),
    1L to listOf(P1, P2, P3),
    2L to listOf(P0, P3),
    4L to listOf(P0, P1, P2, P3, P0),
    5L to listOf(P0, P3, P2, P1, P0),
    6L to listOf(P0, P1, P1, P2),
    7L to listOf()
)

private fun areaRelation(members: List<RelationMember>) =
    rel(0, members, mapOf("type" to "multipolygon"))

private fun asOuters(vararg ways: Way) = ways.map { member(WAY, it.id, "outer") }
private fun asInners(vararg ways: Way) = ways.map { member(WAY, it.id, "inner") }
private fun asMembers(vararg ways: Way) = ways.map { member(WAY, it.id, "") }
