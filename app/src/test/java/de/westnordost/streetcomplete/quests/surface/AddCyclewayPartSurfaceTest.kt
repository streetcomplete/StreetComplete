package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.toCheckDateString
import de.westnordost.streetcomplete.quests.verifyAnswer
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AddSidewalkSurfaceTest {
    private val questType = AddCyclewayPartSurface()

    @Test fun `applicable to segregated cycleway`() {
        assertIsApplicable("highway" to "cycleway", "segregated" to "yes")
        assertIsApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "yes")
    }

    @Test fun `not applicable to non-segregated cycleway`() {
        assertIsNotApplicable("highway" to "cycleway")
        assertIsNotApplicable("highway" to "path", "bicycle" to "designated", "segregated" to "no")
    }


    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
