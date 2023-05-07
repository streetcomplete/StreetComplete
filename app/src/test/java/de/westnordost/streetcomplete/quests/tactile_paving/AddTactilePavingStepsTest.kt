package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTactilePavingStepsTest {
    private val questType = AddTactilePavingSteps()

    @Test
    fun `applicable to paved steps`() {
        assertIsApplicable("highway" to "steps", "surface" to "paved")
        assertIsApplicable("highway" to "steps", "surface" to "asphalt")
        assertIsApplicable("highway" to "steps", "surface" to "paving_stones")
    }

    @Test
    fun `not applicable to unpaved steps`() {
        assertIsNotApplicable("highway" to "steps", "surface" to "unpaved")
        assertIsNotApplicable("highway" to "steps", "surface" to "dirt")
        assertIsNotApplicable("highway" to "steps", "surface" to "grass")
    }

    @Test
    fun `not applicable to steps with tactile paving already tagged`() {
        assertIsNotApplicable("highway" to "steps", "surface" to "paved", "tactile_paving" to "yes")
        assertIsNotApplicable("highway" to "steps", "surface" to "paved", "tactile_paving" to "incorrect")
        assertIsNotApplicable("highway" to "steps", "surface" to "paved", "tactile_paving" to "somewhere")
    }

    @Test
    fun `applicable to steps with old tactile paving`() {
        assertIsApplicable("highway" to "steps", "surface" to "paved", "tactile_paving" to "yes", "check_date:tactile_paving" to "2010-01-01")
        assertIsApplicable("highway" to "steps", "surface" to "paved", "tactile_paving" to "no", "check_date:tactile_paving" to "2010-01-01")
    }

    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
