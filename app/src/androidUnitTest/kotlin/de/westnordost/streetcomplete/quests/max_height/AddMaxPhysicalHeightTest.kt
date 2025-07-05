package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddMaxPhysicalHeightTest {

    private val questType = AddMaxPhysicalHeight(mock())

    @Test fun `applicable if maxheight is below default`() {
        val tags = mapOf("maxheight" to "below_default")
        assertTrue(isApplicableToHeightRestrictor(tags))
        assertTrue(isApplicableToRoad(tags))
    }

    @Test fun `applicable if maxheight is only estimated`() {
        val tags = mapOf("maxheight" to "3", "source:maxheight" to "estimation")
        assertTrue(isApplicableToHeightRestrictor(tags))
        assertTrue(isApplicableToRoad(tags))
    }

    @Test fun `not applicable if maxheight is only estimated but default`() {
        val tags = mapOf("maxheight" to "default", "source:maxheight" to "estimation")
        assertFalse(isApplicableToHeightRestrictor(tags))
        assertFalse(isApplicableToRoad(tags))
    }

    @Test fun `applicable if maxheight is not signed`() {
        val tags = mapOf("maxheight:signed" to "no")
        assertTrue(isApplicableToHeightRestrictor(tags))
        assertTrue(isApplicableToRoad(tags))
    }

    @Test fun `not applicable if maxheight is not signed but maxheight is defined`() {
        val tags = mapOf("maxheight" to "3", "maxheight:signed" to "no")
        assertFalse(isApplicableToHeightRestrictor(tags))
        assertFalse(isApplicableToRoad(tags))
    }

    @Test fun `not applicable if maxheight is default`() {
        val tags = mapOf("maxheight" to "default")
        assertFalse(isApplicableToHeightRestrictor(tags))
        assertFalse(isApplicableToRoad(tags))
    }

    @Test fun `not applicable if physical maxheight is already defined`() {
        val tags = mapOf("maxheight:physical" to "3")
        assertFalse(isApplicableToHeightRestrictor(tags))
        assertFalse(isApplicableToRoad(tags))
    }

    @Test fun `not applicable when on motorroad`() {
        assertFalse(isApplicableToRoad(mapOf("motorroad" to "yes")))
    }
    @Test fun `not applicable when on motorway`() {
        assertFalse(isApplicableToRoad(mapOf("motorway" to "yes")))
    }
    @Test fun `not applicable when on expressway`() {
        assertFalse(isApplicableToRoad(mapOf("expressway" to "yes")))
    }
    @Test fun `not applicable when on motorway_link`() {
        assertFalse(isApplicableToRoad(mapOf("highway" to "motorway_link")))
    }

    private fun isApplicableToHeightRestrictor(tags: Map<String, String>): Boolean =
        questType.isApplicableTo(node(
            tags = mapOf("barrier" to "height_restrictor") + tags
        ))

    private fun isApplicableToRoad(tags: Map<String, String>): Boolean =
        questType.isApplicableTo(way(
            tags = mapOf("highway" to "service") + tags
        ))
}
