package de.westnordost.streetcomplete.data.meta

import org.junit.Test

import de.westnordost.osmapi.map.data.OsmRelation
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.meta.OsmAreas.isArea

import org.junit.Assert.*

class OsmAreasTest {
    @Test fun `relation with no tags is no area`() {
        assertFalse(isArea(OsmRelation(0, 0, null, null)))
    }

    @Test fun `multipolygon relation is an area`() {
        assertTrue(isArea(OsmRelation(0, 0, null, mapOf("type" to "multipolygon"))))
    }

    @Test fun `way with no tags is no area`() {
        assertFalse(isArea(createWay(false, null)))
        assertFalse(isArea(createWay(true, null)))
    }

    @Test fun `simple way with area=yes tag is no area`() {
        assertFalse(isArea(createWay(false, mapOf("area" to "yes"))))
    }

    @Test fun `closed way with area=yes tag is an area`() {
        assertTrue(isArea(createWay(true, mapOf("area" to "yes"))))
    }

    @Test fun `closed way with specific value of a key that is usually no area is an area`() {
        assertFalse(isArea(createWay(true, mapOf("railway" to "something"))))
        assertTrue(isArea(createWay(true, mapOf("railway" to "station"))))
    }

    @Test fun `closed way with a certain tag value is an area`() {
        assertFalse(isArea(createWay(true, mapOf("waterway" to "duck"))))
        assertTrue(isArea(createWay(true, mapOf("waterway" to "dock"))))
    }

    private fun createWay(ring: Boolean, tags: Map<String, String>?): Way {
        val nodes = if (ring) listOf(0L, 1L, 0L) else listOf(0L, 1L)
        return OsmWay(0, 0, nodes, tags)
    }
}
