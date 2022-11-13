package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.overlays.surface.RoadSurfaceOverlayForm
import de.westnordost.streetcomplete.overlays.surface.PathSurfaceOverlayForm
import org.assertj.core.api.Assertions
import org.junit.Test

class PathSurfaceOverlayFormKtTest {
    private fun verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags: Map<String, String>, cyclewaySurfaceAnswer: Surface, cyclewayNote: String?, footwaySurfaceAnswer: Surface, footwayNote: String?, noteAnswer: String?, vararg expectedChanges: StringMapEntryChange) {
        val cb = StringMapChangesBuilder(tags)
        PathSurfaceOverlayForm.editTagsWithSeparateCyclewayAndFootwayAnswer(cb, tags, cyclewaySurfaceAnswer, cyclewayNote, footwaySurfaceAnswer, footwayNote, noteAnswer)
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
        val tags = mapOf("surface" to "asphalt", "smoothness" to "bad", "surface:colour" to "red", "tracktype" to "grade5")
        val expectedChanges = arrayOf(
            StringMapEntryDelete("smoothness", "bad"),
            StringMapEntryDelete("surface:colour", "red"),
            StringMapEntryDelete("tracktype", "grade5"),
            StringMapEntryModify("surface", "asphalt", "paving_stones")
        )
        verifyAnswerWithMainSurfaceOnly(tags, Surface.PAVING_STONES, null, *expectedChanges)
    }

    @Test
    fun `simplest possible applying new path surface`() {
        val tags = mapOf("highway" to "path")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("surface", "compacted"),
        )
        verifyAnswerWithMainSurfaceOnly(tags, Surface.COMPACTED, null, *expectedChanges)
    }

    @Test
    fun `simplest possible applying new path surface with footway and cycleway parts`() {
        val tags = mapOf("highway" to "path")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("surface", "compacted"),
            StringMapEntryAdd("cycleway:surface", "compacted"),
            StringMapEntryAdd("footway:surface", "compacted"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags, Surface.COMPACTED, null, Surface.COMPACTED, null, null, *expectedChanges)
    }

    @Test
    fun `applying new path surface with footway and cycleway parts, with paved surface as shared value`() {
        val tags = mapOf("highway" to "path")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("surface", "paved"),
            StringMapEntryAdd("cycleway:surface", "asphalt"),
            StringMapEntryAdd("footway:surface", "paving_stones"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags, Surface.ASPHALT, null, Surface.PAVING_STONES, null, null, *expectedChanges)
    }

    @Test
    fun `applying new path surface with unpaved footway and paved cycleway part`() {
        val tags = mapOf("highway" to "path")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("cycleway:surface", "asphalt"),
            StringMapEntryAdd("footway:surface", "gravel"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags, Surface.ASPHALT, null, Surface.GRAVEL, null, null, *expectedChanges)
    }
}

