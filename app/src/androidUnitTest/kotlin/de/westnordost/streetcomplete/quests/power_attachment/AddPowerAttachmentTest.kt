package de.westnordost.streetcomplete.quests.power_attachment

import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddPowerAttachmentTest {

    private val questType = AddPowerAttachment()

    @Test fun `applicable only to power poles part of exactly one power line`() {
        val poleWithMultiplePowerLines = node(id = 2, tags = mapOf("power" to "pole"))
        val poleWithOnePowerLine = node(id = 1, tags = mapOf("power" to "pole"))
        val poleWithNoPowerLine = node(id = 6, tags = mapOf("power" to "pole"))
        val mapData = TestMapDataWithGeometry(listOf(
            poleWithOnePowerLine,
            poleWithMultiplePowerLines,
            way(1, listOf(1, 2, 3), mapOf("power" to "line")),
            way(2, listOf(4, 2, 5), mapOf("power" to "line")),
        ))

        assertEquals(
            poleWithOnePowerLine,
            questType.getApplicableElements(mapData).single()
        )
    }
}
