package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.overlays.surface.RoadSurfaceOverlayForm
import org.assertj.core.api.Assertions
import org.junit.Test

class RoadSurfaceOverlayFormKtTest {
    private fun verifyAnswer(tags: Map<String, String>, surfaceAnswer: Surface, noteAnswer: String?, vararg expectedChanges: StringMapEntryChange) {
        val cb = StringMapChangesBuilder(tags)
        RoadSurfaceOverlayForm.editTags(cb, tags, surfaceAnswer, noteAnswer)
        val changes = cb.create().changes
        Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
    }

    @Test
    fun `new different value is applied, associated tags are removed`() {
        // TODO SurfaceAnswer should get most of overlay edit code...
        val tags = mapOf("surface" to "asphalt", "smoothness" to "bad", "surface:colour" to "red", "tractype" to "grade5")
        val expectedChanges = arrayOf(
            StringMapEntryDelete("smoothness", "bad"),
            StringMapEntryDelete("surface:colour", "red"),
            StringMapEntryDelete("tractype", "grade5"),
            StringMapEntryModify("surface", "asphalt", "paving_stones")
        )
        verifyAnswer(tags, Surface.PAVING_STONES, null, *expectedChanges)
    }
}

