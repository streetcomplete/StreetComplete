package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElementTest {
    @Test fun `relation with no tags is no area`() {
        assertFalse(rel().isArea())
    }

    @Test fun `way is closed`() {
        assertTrue(createRing().isClosed)
    }

    @Test fun `way is not closed`() {
        assertFalse(createWay().isClosed)
    }

    @Test fun `multipolygon relation is an area`() {
        assertTrue(rel(tags = mapOf("type" to "multipolygon")).isArea())
    }

    @Test fun `way with no tags is no area`() {
        assertFalse(createWay().isArea())
        assertFalse(createRing().isArea())
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

    private fun createWay(tags: Map<String, String> = emptyMap()) = way(nodes = listOf(0L, 1L), tags = tags)
    private fun createRing(tags: Map<String, String> = emptyMap()) = way(nodes = listOf(0L, 1L, 0L), tags = tags)
}
