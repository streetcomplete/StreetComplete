package de.westnordost.streetcomplete.ktx

import de.westnordost.osmapi.map.data.OsmRelation
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Way
import org.junit.Assert.*
import org.junit.Test

class ElementTest {
    @Test fun `relation with no tags is no area`() {
        assertFalse(OsmRelation(0, 0, null, null).isArea())
    }

    @Test fun `way is closed`() {
        assertTrue(createRing(null).isClosed())
    }

    @Test fun `way is not closed`() {
        assertFalse(createWay(null).isClosed())
    }

    @Test fun `multipolygon relation is an area`() {
        assertTrue(OsmRelation(0, 0, null, mapOf("type" to "multipolygon")).isArea())
    }

    @Test fun `way with no tags is no area`() {
        assertFalse(createWay(null).isArea())
        assertFalse(createRing(null).isArea())
    }

    @Test fun `simple way with area=yes tag is no area`() {
        assertFalse(createWay(mapOf("area" to "yes")).isArea())
    }

    @Test fun `closed way with area=yes tag is an area`() {
        assertTrue(createRing(mapOf("area" to "yes")).isArea())
    }

    @Test fun `closed way with specific value of a key that is usually no area is an area`() {
        assertFalse(createRing(mapOf("railway" to "something")).isArea())
        assertTrue(createRing(mapOf("railway" to "station")).isArea())
    }

    @Test fun `closed way with a certain tag value is an area`() {
        assertFalse(createRing(mapOf("waterway" to "duck")).isArea())
        assertTrue(createRing(mapOf("waterway" to "dock")).isArea())
    }

    private fun createWay(tags: Map<String, String>?): Way = OsmWay(0, 0, listOf(0L, 1L), tags)
    private fun createRing(tags: Map<String, String>?): Way = OsmWay(0, 0, listOf(0L, 1L, 0L), tags)
}
