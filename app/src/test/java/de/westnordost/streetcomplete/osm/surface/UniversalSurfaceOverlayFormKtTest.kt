package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.overlays.surface.RoadSurfaceOverlayForm
import de.westnordost.streetcomplete.overlays.surface.UniversalSurfaceOverlayForm
import org.assertj.core.api.Assertions
import org.junit.Test

class UniversalSurfaceOverlayFormKtTest {
    private fun verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags: Map<String, String>, cyclewaySurfaceAnswer: Surface, cyclewayNote: String?, footwaySurfaceAnswer: Surface, footwayNote: String?, noteAnswer: String?, vararg expectedChanges: StringMapEntryChange) {
        val cb = StringMapChangesBuilder(tags)
        UniversalSurfaceOverlayForm.editTagsWithSeparateCyclewayAndFootwayAnswer(cb, tags, cyclewaySurfaceAnswer, cyclewayNote, footwaySurfaceAnswer, footwayNote, noteAnswer)
        val changes = cb.create().changes
        Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
    }

    private fun verifyAnswerWithMainSurfaceOnly(tags: Map<String, String>, surfaceAnswer: Surface, noteAnswer: String?, vararg expectedChanges: StringMapEntryChange) {
        val cb = StringMapChangesBuilder(tags)
        RoadSurfaceOverlayForm.editTags(cb, tags, surfaceAnswer, noteAnswer)
        val changes = cb.create().changes
        Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
    }

    @Test
    fun `single new different value is applied, associated tags are removed`() {
        // TODO SurfaceAnswer should get most of overlay edit code...
        val tags = mapOf("surface" to "asphalt", "smoothness" to "bad", "surface:colour" to "red", "tractype" to "grade5")
        val expectedChanges = arrayOf(
            StringMapEntryDelete("smoothness", "bad"),
            StringMapEntryDelete("surface:colour", "red"),
            StringMapEntryDelete("tractype", "grade5"),
            StringMapEntryModify("surface", "asphalt", "paving_stones")
        )
        verifyAnswerWithMainSurfaceOnly(tags, Surface.PAVING_STONES, null, *expectedChanges)
    }
}

