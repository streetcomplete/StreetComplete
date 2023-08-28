package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import org.assertj.core.api.Assertions
import kotlin.test.Test

internal class SidewalkSurfaceCreatorKtTest {

    @Test fun `apply asphalt surface on both sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalkSurface(SurfaceAndNote(Surface.ASPHALT), SurfaceAndNote(Surface.ASPHALT)),
            arrayOf(
                StringMapEntryAdd("sidewalk:both:surface", "asphalt")
            )
        )
    }

    @Test fun `apply different surface on each side`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalkSurface(SurfaceAndNote(Surface.ASPHALT), SurfaceAndNote(Surface.PAVING_STONES)),
            arrayOf(
                StringMapEntryAdd("sidewalk:left:surface", "asphalt"),
                StringMapEntryAdd("sidewalk:right:surface", "paving_stones")
            )
        )
    }

    @Test fun `apply generic surface on both sides`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalkSurface(
                SurfaceAndNote(Surface.PAVED, "note"),
                SurfaceAndNote(Surface.PAVED, "note")
            ),
            arrayOf(
                StringMapEntryAdd("sidewalk:both:surface", "paved"),
                StringMapEntryAdd("sidewalk:both:surface:note", "note")
            )
        )
    }

    @Test fun `updates check_date`() {
        verifyAnswer(
            mapOf("sidewalk:both:surface" to "asphalt", "check_date:sidewalk:surface" to "2000-10-10"),
            LeftAndRightSidewalkSurface(SurfaceAndNote(Surface.ASPHALT), SurfaceAndNote(Surface.ASPHALT)),
            arrayOf(
                StringMapEntryModify("sidewalk:both:surface", "asphalt", "asphalt"),
                StringMapEntryModify("check_date:sidewalk:surface", "2000-10-10", nowAsCheckDateString()),
            )
        )
    }

    @Test fun `sidewalk surface changes to be the same on both sides`() {
        verifyAnswer(
            mapOf("sidewalk:left:surface" to "asphalt", "sidewalk:right:surface" to "paving_stones"),
            LeftAndRightSidewalkSurface(SurfaceAndNote(Surface.CONCRETE), SurfaceAndNote(Surface.CONCRETE)),
            arrayOf(
                StringMapEntryDelete("sidewalk:left:surface", "asphalt"),
                StringMapEntryDelete("sidewalk:right:surface", "paving_stones"),
                StringMapEntryAdd("sidewalk:both:surface", "concrete")
            )
        )
    }

    @Test fun `sidewalk surface changes on each side`() {
        verifyAnswer(
            mapOf("sidewalk:left:surface" to "asphalt", "sidewalk:right:surface" to "paving_stones"),
            LeftAndRightSidewalkSurface(SurfaceAndNote(Surface.CONCRETE), SurfaceAndNote(Surface.GRAVEL)),
            arrayOf(
                StringMapEntryModify("sidewalk:left:surface", "asphalt", "concrete"),
                StringMapEntryModify("sidewalk:right:surface", "paving_stones", "gravel"),
            )
        )
    }

    @Test fun `smoothness tag removed when surface changes, same on both sides`() {
        verifyAnswer(
            mapOf("sidewalk:both:surface" to "asphalt", "sidewalk:both:smoothness" to "excellent"),
            LeftAndRightSidewalkSurface(SurfaceAndNote(Surface.PAVING_STONES), SurfaceAndNote(Surface.PAVING_STONES)),
            arrayOf(
                StringMapEntryDelete("sidewalk:both:smoothness", "excellent"),
                StringMapEntryModify("sidewalk:both:surface", "asphalt", "paving_stones")
            )
        )
    }

    @Test fun `remove smoothness when surface changes, different on each side`() {
        verifyAnswer(
            mapOf("sidewalk:left:surface" to "asphalt",
                "sidewalk:right:surface" to "concrete",
                "sidewalk:left:smoothness" to "excellent",
                "sidewalk:right:smoothness" to "good"
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(Surface.PAVING_STONES), SurfaceAndNote(Surface.PAVING_STONES)),
            arrayOf(
                StringMapEntryDelete("sidewalk:left:surface", "asphalt"),
                StringMapEntryDelete("sidewalk:right:surface", "concrete"),
                StringMapEntryDelete("sidewalk:left:smoothness", "excellent"),
                StringMapEntryDelete("sidewalk:right:smoothness", "good"),
                StringMapEntryAdd("sidewalk:both:surface", "paving_stones")
            )
        )
    }

    @Test fun `carriageway properties not affected by sidewalk answer`() {
        verifyAnswer(
            mapOf("sidewalk" to "both",
                "surface" to "concrete",
                "smoothness" to "excellent",
            ),
            LeftAndRightSidewalkSurface(SurfaceAndNote(Surface.PAVING_STONES), SurfaceAndNote(Surface.PAVING_STONES)),
            arrayOf(
                StringMapEntryAdd("sidewalk:both:surface", "paving_stones")
            )
        )
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: LeftAndRightSidewalkSurface, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
