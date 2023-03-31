package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.osm.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.node
import org.junit.Assert
import org.junit.Test

class AddDrinkingWaterTest {
    private val questType = AddDrinkingWater()

    @Test
    fun `applicable to springs`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, tags = mapOf("natural" to "spring")),
            ),
        )
        Assert.assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to inaccessible springs`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, tags = mapOf("natural" to "spring", "access" to "no")),
            ),
        )
        Assert.assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}
