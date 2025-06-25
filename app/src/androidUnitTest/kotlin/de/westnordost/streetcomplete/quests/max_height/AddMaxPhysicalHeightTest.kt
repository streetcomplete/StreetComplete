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

    private val MAXHEIGHT_BELOW_DEFAULT    = mapOf("maxheight" to "below_default")
    private val MAXHEIGHT_DEFAULT          = mapOf("maxheight" to "default")
    private val MAXHEIGHT_NUMBER_VALUE     = mapOf("maxheight" to "3")
    private val MAXHEIGHT_PHY_NUMBER_VALUE = mapOf("maxheight:physical" to "3")
    private val MAXHEIGHT_SIGN_NO          = mapOf("maxheight:signed" to "no")
    private val MAXHEIGHT_SOURCE_EST       = mapOf("source:maxheight" to "estimation")

    @Test fun `applicable if maxheight is below default`() {
        assertTrue(isApplicableToHeightRestrictor(MAXHEIGHT_BELOW_DEFAULT))
        assertTrue(isApplicableToRoad(MAXHEIGHT_BELOW_DEFAULT))
    }

    @Test fun `applicable if maxheight is only estimated`() {
        assertTrue(isApplicableToHeightRestrictor(MAXHEIGHT_NUMBER_VALUE + MAXHEIGHT_SOURCE_EST))
        assertTrue(isApplicableToRoad(MAXHEIGHT_NUMBER_VALUE + MAXHEIGHT_SOURCE_EST))
    }

    @Test fun `not applicable if maxheight is only estimated but default`() {
        assertFalse(isApplicableToHeightRestrictor(MAXHEIGHT_DEFAULT + MAXHEIGHT_SOURCE_EST))
        assertFalse(isApplicableToRoad(MAXHEIGHT_DEFAULT + MAXHEIGHT_SOURCE_EST))
    }

    @Test fun `applicable if maxheight is not signed`() {
        assertTrue(isApplicableToHeightRestrictor(MAXHEIGHT_SIGN_NO))
        assertTrue(isApplicableToRoad(MAXHEIGHT_SIGN_NO))
    }

    @Test fun `not applicable if maxheight is not signed but maxheight is defined`() {
        assertFalse(isApplicableToHeightRestrictor(MAXHEIGHT_SIGN_NO + MAXHEIGHT_NUMBER_VALUE))
        assertFalse(isApplicableToRoad(MAXHEIGHT_SIGN_NO + MAXHEIGHT_NUMBER_VALUE))
    }

    @Test fun `not applicable if maxheight is default`() {
        assertFalse(isApplicableToHeightRestrictor(MAXHEIGHT_DEFAULT))
        assertFalse(isApplicableToRoad(MAXHEIGHT_DEFAULT))
    }

    @Test fun `not applicable if physical maxheight is already defined`() {
        assertFalse(isApplicableToHeightRestrictor(MAXHEIGHT_PHY_NUMBER_VALUE))
        assertFalse(isApplicableToRoad(MAXHEIGHT_PHY_NUMBER_VALUE))
    }

    @Test fun `not applicable when on motorroad`() {
        assertTrue(isApplicableToHeightRestrictor(MAXHEIGHT_SIGN_NO))
        assertFalse(isApplicableToRoad(mapOf("motorroad" to "yes")))
    }
    @Test fun `not applicable when on motorway`() {
        assertTrue(isApplicableToHeightRestrictor(MAXHEIGHT_SIGN_NO))
        assertFalse(isApplicableToRoad(mapOf("motorway" to "yes")))
    }
    @Test fun `not applicable when on expressway`() {
        assertTrue(isApplicableToHeightRestrictor(MAXHEIGHT_SIGN_NO))
        assertFalse(isApplicableToRoad(mapOf("expressway" to "yes")))
    }
    @Test fun `not applicable when on motorway_link`() {
        assertTrue(isApplicableToHeightRestrictor(MAXHEIGHT_SIGN_NO))
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
