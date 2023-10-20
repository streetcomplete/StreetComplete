package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class AddMaxPhysicalHeightTest {

    private val questType = AddMaxPhysicalHeight(mock())

    @Test fun `applicable if maxheight is below default`() {
        assertTrue(isApplicableTo(mapOf("maxheight" to "below_default")))
    }

    @Test fun `applicable if maxheight is only estimated`() {
        assertTrue(isApplicableTo(mapOf(
            "maxheight" to "3",
            "source:maxheight" to "estimation"
        )))
    }

    @Test fun `not applicable if maxheight is only estimated but default`() {
        assertFalse(isApplicableTo(mapOf(
            "maxheight" to "default",
            "source:maxheight" to "estimation"
        )))
    }

    @Test fun `applicable if maxheight is not signed`() {
        assertTrue(isApplicableTo(mapOf("maxheight:signed" to "no")))
    }

    @Test fun `not applicable if maxheight is not signed but maxheight is defined`() {
        assertFalse(isApplicableTo(mapOf(
            "maxheight:signed" to "no",
            "maxheight" to "3"
        )))
    }

    @Test fun `not applicable if maxheight is default`() {
        assertFalse(isApplicableTo(mapOf("maxheight" to "default")))
    }

    @Test fun `not applicable if physical maxheight is already defined`() {
        assertFalse(isApplicableTo(mapOf("maxheight:physical" to "3")))
    }

    private fun isApplicableTo(tags: Map<String, String>): Boolean {
        // since the node and way filter is so similar, it makes sense to always test both
        val nodes = questType.isApplicableTo(node(
            tags = mapOf("barrier" to "height_restrictor") + tags
        ))
        val ways = questType.isApplicableTo(way(
            tags = mapOf("highway" to "service") + tags
        ))
        if (nodes != ways) {
            fail("Result of isApplicableTo is not the same for nodes and ways")
        }
        return nodes
    }
}
