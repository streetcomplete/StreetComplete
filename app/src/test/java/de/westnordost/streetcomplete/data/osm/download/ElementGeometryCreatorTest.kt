package de.westnordost.streetcomplete.data.osm.download

import de.westnordost.osmapi.map.data.*
import org.junit.Test

import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry

import org.junit.Assert.*

class ElementGeometryCreatorTest {

    @Test fun `create for node`() {
        val g = create(OsmNode(0L, 0, P0, null))
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
        val geom = create(areaRelation(outer(EMPTY_WAY)))
        assertNull(geom)
    }

    @Test fun `create for multipolygon relation with single way with no role`() {
        val geom = create(areaRelation(member(AREA_WAY)))
        assertNull(geom)
    }

    @Test fun `create for multipolygon relation with single outer way`() {
        val geom = create(areaRelation(outer(AREA_WAY))) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
        assertEquals(O, geom.center)
    }

    @Test fun `create for multipolygon relation with single outer clockwise way`() {
        val geom = create(areaRelation(outer(AREA_WAY_CLOCKWISE))) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
    }

    @Test fun `create for multipolygon relation with outer composed of several ways`() {
        val geom = create(areaRelation(outer(SIMPLE_WAY1, SIMPLE_WAY3, SIMPLE_WAY2))) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
    }

    @Test fun `create for multipolygon relation consisting solely of a hole`() {
        val geom = create(areaRelation(inner(AREA_WAY)))
        assertNull(geom)
    }

    @Test fun `create for multipolygon relation with hole consisting of single way`() {
        val geom = create(areaRelation(outer(AREA_WAY) + inner(AREA_WAY_CLOCKWISE))) as ElementPolygonsGeometry
        assertEquals(listOf(CCW_RING, CW_RING), geom.polygons)
        assertEquals(O, geom.center)
    }

    @Test fun `create for multipolygon relation with hole consisting of single counterclockwise way`() {
        val geom = create(areaRelation(outer(AREA_WAY) + inner(AREA_WAY))) as ElementPolygonsGeometry
        assertEquals(listOf(CCW_RING, CW_RING), geom.polygons)
        assertEquals(O, geom.center)
    }

    @Test fun `create for multipolygon relation with hole consisting of several ways`() {
        val geom = create(areaRelation(outer(AREA_WAY) + inner(SIMPLE_WAY1, SIMPLE_WAY3, SIMPLE_WAY2))) as ElementPolygonsGeometry
        assertEquals(listOf(CCW_RING, CW_RING), geom.polygons)
        assertEquals(O, geom.center)
    }

    @Test fun `creating for multipolygon relation ignores unusable parts`() {
        val geom = create(areaRelation(
            outer(EMPTY_WAY, AREA_WAY, SIMPLE_WAY1) +
            inner(EMPTY_WAY) +
            member(AREA_WAY))) as ElementPolygonsGeometry
        assertEquals(CCW_RING, geom.polygons.single())
        assertEquals(O, geom.center)
    }

    @Test fun `create for polyline relation with single empty way`() {
        val geom = create(relation(member(EMPTY_WAY)))
        assertNull(geom)
    }

    @Test fun `create for polyline relation with single way`() {
        val geom = create(relation(member(AREA_WAY))) as ElementPolylinesGeometry
        assertEquals(CCW_RING, geom.polylines.single())
    }

    @Test fun `create for polyline relation with two ways`() {
        val geom = create(relation(member(AREA_WAY, SIMPLE_WAY1))) as ElementPolylinesGeometry
        assertTrue(geom.polylines.containsAll(listOf(CCW_RING, listOf(P0, P1))))
    }

    @Test fun `create for polyline relation with ways joined together`() {
        val geom = create(relation(member(SIMPLE_WAY1, SIMPLE_WAY2, SIMPLE_WAY3, WAY_DUPLICATE_NODES))) as ElementPolylinesGeometry
        assertTrue(geom.polylines.containsAll(listOf(CCW_RING, listOf(P0, P1, P2))))
    }
}

private fun create(node: Node) =
    ElementGeometryCreator().create(node)

private fun create(way: Way) =
    ElementGeometryCreator().create(way, WAY_GEOMETRIES[way.id] ?: emptyList())

private fun create(relation: Relation) =
    ElementGeometryCreator().create(relation, WAY_GEOMETRIES)

private val WAY_AREA = mapOf("area" to "yes")

private val O: LatLon = OsmLatLon(1.0, 1.0)
private val P0: LatLon = OsmLatLon(0.0, 2.0)
private val P1: LatLon = OsmLatLon(0.0, 0.0)
private val P2: LatLon = OsmLatLon(2.0, 0.0)
private val P3: LatLon = OsmLatLon(2.0, 2.0)

private val SIMPLE_WAY1 = OsmWay(0, 0, listOf(0,1), null)
private val SIMPLE_WAY2 = OsmWay(1, 0, listOf(1,2,3), null)
private val SIMPLE_WAY3 = OsmWay(2, 0, listOf(0,3), null)
private val AREA_WAY = OsmWay(4, 0, listOf(0,1,2,3,0), WAY_AREA)
private val AREA_WAY_CLOCKWISE = OsmWay(5, 0, listOf(0,3,2,1,0), WAY_AREA)
private val WAY_DUPLICATE_NODES = OsmWay(6, 0, listOf(0,1,1,2), null)
private val EMPTY_WAY = OsmWay(7, 0, emptyList(), null)

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
    OsmRelation(0,0, members, mapOf("type" to "multipolygon"))

private fun relation(members: List<RelationMember>) = OsmRelation(0,0, members, null)

private fun outer(vararg ways: Way) = ways.map { OsmRelationMember(it.id, "outer", Element.Type.WAY) }
private fun inner(vararg ways: Way) = ways.map { OsmRelationMember(it.id, "inner", Element.Type.WAY) }
private fun member(vararg ways: Way) = ways.map { OsmRelationMember(it.id, "", Element.Type.WAY) }
