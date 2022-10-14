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
    private val questType = AddSidewalkSurface()

    @Test fun `not applicable to road with separate sidewalks`() {
        assertIsNotApplicable("sidewalk" to "separate")
    }

    @Test fun `not applicable to road with no sidewalks`() {
        assertIsNotApplicable("sidewalk" to "no")
    }

    @Test fun `applicable to road with sidewalk on both sides`() {
        assertIsApplicable("highway" to "residential", "sidewalk" to "both")
    }

    @Test fun `applicable to road with sidewalk on only one side`() {
        assertIsApplicable("highway" to "residential", "sidewalk" to "left")

        assertIsApplicable("highway" to "residential", "sidewalk" to "right")
    }

    @Test fun `applicable to road with sidewalk on one side and separate sidewalk on the other`() {
        assertIsApplicable("highway" to "residential", "sidewalk:left" to "yes", "sidewalk:right" to "separate")

        assertIsApplicable("highway" to "residential", "sidewalk:left" to "separate", "sidewalk:right" to "yes")
    }

    @Test fun `applicable to road with sidewalk on one side and no sidewalk on the other`() {
        assertIsApplicable("highway" to "residential", "sidewalk:left" to "yes", "sidewalk:right" to "no")

        assertIsApplicable("highway" to "residential", "sidewalk:left" to "no", "sidewalk:right" to "yes")
    }

    @Test fun `apply asphalt surface on both sides`() {
        questType.verifyAnswer(
            SidewalkSurfaceAnswer(SurfaceAnswer(Surface.ASPHALT), SurfaceAnswer(Surface.ASPHALT)),
            StringMapEntryAdd("sidewalk:both:surface", "asphalt")
        )
    }

    @Test fun `apply different surface on each side`() {
        questType.verifyAnswer(
            SidewalkSurfaceAnswer(SurfaceAnswer(Surface.ASPHALT), SurfaceAnswer(Surface.PAVING_STONES)),
            StringMapEntryAdd("sidewalk:left:surface", "asphalt"),
            StringMapEntryAdd("sidewalk:right:surface", "paving_stones")
        )
    }

    @Test fun `apply generic surface on both sides`() {
        questType.verifyAnswer(
            SidewalkSurfaceAnswer(
                SurfaceAnswer(Surface.PAVED_ROAD, "note"),
                SurfaceAnswer(Surface.PAVED_ROAD, "note")),
            StringMapEntryAdd("sidewalk:both:surface", "paved"),
            StringMapEntryAdd("sidewalk:both:surface:note", "note")
        )
    }

    @Test fun `updates check_date`() {
        questType.verifyAnswer(
            mapOf("sidewalk:both:surface" to "asphalt", "check_date:sidewalk:surface" to "2000-10-10"),
            SidewalkSurfaceAnswer(SurfaceAnswer(Surface.ASPHALT), SurfaceAnswer(Surface.ASPHALT)),
            StringMapEntryModify("sidewalk:both:surface", "asphalt", "asphalt"),
            StringMapEntryModify("check_date:sidewalk:surface", "2000-10-10", LocalDate.now().toCheckDateString()),
        )
    }

    @Test fun `sidewalk surface changes to be the same on both sides`() {
        questType.verifyAnswer(
            mapOf("sidewalk:left:surface" to "asphalt", "sidewalk:right:surface" to "paving_stones"),
            SidewalkSurfaceAnswer(SurfaceAnswer(Surface.CONCRETE), SurfaceAnswer(Surface.CONCRETE)),
            StringMapEntryDelete("sidewalk:left:surface", "asphalt"),
            StringMapEntryDelete("sidewalk:right:surface", "paving_stones"),
            StringMapEntryAdd("sidewalk:both:surface", "concrete")
        )
    }

    @Test fun `sidewalk surface changes on each side`() {
        questType.verifyAnswer(
            mapOf("sidewalk:left:surface" to "asphalt", "sidewalk:right:surface" to "paving_stones"),
            SidewalkSurfaceAnswer(SurfaceAnswer(Surface.CONCRETE), SurfaceAnswer(Surface.GRAVEL)),
            StringMapEntryModify("sidewalk:left:surface", "asphalt", "concrete"),
            StringMapEntryModify("sidewalk:right:surface", "paving_stones", "gravel"),
        )
    }

    @Test fun `smoothness tag removed when surface changes, same on both sides`() {
        questType.verifyAnswer(
            mapOf("sidewalk:both:surface" to "asphalt", "sidewalk:both:smoothness" to "excellent"),
            SidewalkSurfaceAnswer(SurfaceAnswer(Surface.PAVING_STONES), SurfaceAnswer(Surface.PAVING_STONES)),
            StringMapEntryDelete("sidewalk:both:smoothness", "excellent"),
            StringMapEntryModify("sidewalk:both:surface", "asphalt", "paving_stones")
        )
    }

    @Test fun `remove smoothness when surface changes, different on each side`() {
        questType.verifyAnswer(
            mapOf("sidewalk:left:surface" to "asphalt",
                "sidewalk:right:surface" to "concrete",
                "sidewalk:left:smoothness" to "excellent",
                "sidewalk:right:smoothness" to "good"
            ),
            SidewalkSurfaceAnswer(SurfaceAnswer(Surface.PAVING_STONES), SurfaceAnswer(Surface.PAVING_STONES)),
            StringMapEntryDelete("sidewalk:left:surface", "asphalt"),
            StringMapEntryDelete("sidewalk:right:surface", "concrete"),
            StringMapEntryDelete("sidewalk:left:smoothness", "excellent"),
            StringMapEntryDelete("sidewalk:right:smoothness", "good"),
            StringMapEntryAdd("sidewalk:both:surface", "paving_stones")
        )
    }

    @Test fun `remove all sidewalk information`() {
        questType.verifyAnswer(
            mapOf("sidewalk:left:surface" to "asphalt",
                "sidewalk:right:surface" to "concrete",
                "sidewalk:left:smoothness" to "excellent",
                "sidewalk:right:smoothness" to "good",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ),
            SidewalkSurfaceAnswer(SidewalkIsDifferent),
            StringMapEntryDelete("sidewalk:left:surface", "asphalt"),
            StringMapEntryDelete("sidewalk:right:surface", "concrete"),
            StringMapEntryDelete("sidewalk:left:smoothness", "excellent"),
            StringMapEntryDelete("sidewalk:right:smoothness", "good"),
            StringMapEntryDelete("sidewalk:left", "yes"),
            StringMapEntryDelete("sidewalk:right", "yes")
        )
    }

    private fun assertIsApplicable(vararg pairs: Pair<String, String>) {
        assertTrue(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }

    private fun assertIsNotApplicable(vararg pairs: Pair<String, String>) {
        assertFalse(questType.isApplicableTo(way(nodes = listOf(1, 2, 3), tags = mapOf(*pairs))))
    }
}
