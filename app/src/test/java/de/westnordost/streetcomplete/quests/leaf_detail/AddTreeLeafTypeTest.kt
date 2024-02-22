package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.answerApplied
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals

class AddTreeLeafTypeTest {
    private val questType = AddTreeLeafType()

    @Test fun `sets expected tags`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("leaf_type", "needleleaved"),
            ),
            questType.answerApplied(TreeLeafType.NEEDLELEAVED)
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("leaf_type", "broadleaved"),
            ),
            questType.answerApplied(TreeLeafType.BROADLEAVED)
        )
    }

    @Test fun `applicable to trees`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, tags = mapOf("natural" to "tree")),
            ),
        )
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to trees with leaf type set`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, tags = mapOf("natural" to "tree", "leaf_type" to "broadleaved")),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to trees with some tags indicating leaf type`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, tags = mapOf("natural" to "tree", "species:pl" to "dąb")),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}
