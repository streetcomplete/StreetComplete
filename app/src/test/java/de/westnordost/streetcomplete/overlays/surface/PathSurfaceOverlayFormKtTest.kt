package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.surface.Surface
import org.assertj.core.api.Assertions
import org.junit.Test

class PathSurfaceOverlayFormKtTest {
    private fun verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags: Map<String, String>, cyclewaySurfaceAnswer: Surface, cyclewayNote: String?, footwaySurfaceAnswer: Surface, footwayNote: String?, noteAnswer: String?, vararg expectedChanges: StringMapEntryChange) {
        val cb = StringMapChangesBuilder(tags)
        PathSurfaceOverlayForm.editTagsWithSeparateCyclewayAndFootwayAnswer(cb, cyclewaySurfaceAnswer, cyclewayNote, footwaySurfaceAnswer, footwayNote, noteAnswer)
        val changes = cb.create().changes
        Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
    }

    private fun verifyAnswerWithMainSurfaceOnly(tags: Map<String, String>, surfaceAnswer: Surface, noteAnswer: String?, vararg expectedChanges: StringMapEntryChange) {
        val cb = StringMapChangesBuilder(tags)
        RoadSurfaceOverlayForm.editTags(cb, surfaceAnswer, noteAnswer)
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
        val tags = mapOf("highway" to "path",  "segregated" to "yes", "foot" to "designated", "bicycle" to "designated")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("surface", "compacted"),
            StringMapEntryAdd("cycleway:surface", "compacted"),
            StringMapEntryAdd("footway:surface", "compacted"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags,
            Surface.COMPACTED, null,
            Surface.COMPACTED, null, null, *expectedChanges)
    }

    @Test
    fun `applying new path surface with footway and cycleway parts, with paved surface as shared value`() {
        val tags = mapOf("highway" to "path",  "segregated" to "yes", "foot" to "designated", "bicycle" to "designated")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("surface", "paved"),
            StringMapEntryAdd("cycleway:surface", "asphalt"),
            StringMapEntryAdd("footway:surface", "paving_stones"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags,
            Surface.ASPHALT, null,
            Surface.PAVING_STONES, null, null, *expectedChanges)
    }

    @Test
    fun `applying new path surface with unpaved footway and paved cycleway part`() {
        val tags = mapOf("highway" to "path",  "segregated" to "yes", "foot" to "designated", "bicycle" to "designated")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("cycleway:surface", "asphalt"),
            StringMapEntryAdd("footway:surface", "gravel"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags,
            Surface.ASPHALT, null,
            Surface.GRAVEL, null, null, *expectedChanges)
    }

    @Test
    fun `note tag is removed if no longer supplied`() {
        val tags = mapOf("highway" to "path", "surface" to "unpaved", "surface:note" to "you get what you reward for")
        val expectedChanges = arrayOf(
            StringMapEntryModify("surface", "unpaved", "ground"),
            StringMapEntryDelete("surface:note", "you get what you reward for"),
        )
        verifyAnswerWithMainSurfaceOnly(tags, Surface.GROUND_AREA, null, *expectedChanges)
    }

    @Test
    fun `note tags are removed if no longer supplied`() {
        val tags = mapOf("highway" to "path", "segregated" to "yes",
            "surface" to "unpaved", "surface:note" to "you get what you reward for",
            "cycleway:surface" to "paving_stones", "cycleway:surface:note" to "a",
            "footway:surface" to "asphalt", "footway:surface:note" to "🤷")
        val expectedChanges = arrayOf(
            StringMapEntryModify("surface", "unpaved", "paved"),
            StringMapEntryModify("cycleway:surface", "paving_stones", "sett"),
            StringMapEntryModify("footway:surface", "asphalt", "unhewn_cobblestone"),
            StringMapEntryDelete("surface:note", "you get what you reward for"),
            StringMapEntryDelete("cycleway:surface:note", "a"),
            StringMapEntryDelete("footway:surface:note", "🤷"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags,
            Surface.SETT, null,
            Surface.UNHEWN_COBBLESTONE, null, null, *expectedChanges)
    }

    @Test
    fun `note tags are kept if in answer, even while surface tag is removed`() {
        val tags = mapOf("highway" to "path", "segregated" to "yes",
            "surface" to "unpaved", "surface:note" to "How do you eat an elephant? One bite at a time")
        val expectedChanges = arrayOf(
            StringMapEntryDelete("surface", "unpaved"),
            StringMapEntryAdd("cycleway:surface", "paving_stones"),
            StringMapEntryAdd("footway:surface", "sand"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags,
            Surface.PAVING_STONES, null,
            Surface.SAND, null, "How do you eat an elephant? One bite at a time", *expectedChanges)
    }

    @Test
    fun `note tags are added`() {
        val tags = mapOf("highway" to "path",
            "surface" to "unpaved", "segregated" to "yes",
            "cycleway:surface" to "paving_stones",
            "footway:surface" to "asphalt",)
        val expectedChanges = arrayOf(
            StringMapEntryModify("surface", "unpaved", "paved"),
            StringMapEntryModify("cycleway:surface", "paving_stones", "paved"),
            StringMapEntryModify("footway:surface", "asphalt", "paved"),
            StringMapEntryAdd("cycleway:surface:note", "foo"),
            StringMapEntryAdd("footway:surface:note", "bar"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags,
            Surface.PAVED_AREA, "foo",
            Surface.PAVED_ROAD, "bar", null, *expectedChanges)
    }

    @Test
    fun `note tags are modified`() {
        val tags = mapOf("highway" to "path",
            "surface" to "unpaved", "segregated" to "yes",
            "cycleway:surface" to "paving_stones", "cycleway:surface:note" to "old",
            "footway:surface" to "asphalt", "footway:surface:note" to "old",)
        val expectedChanges = arrayOf(
            StringMapEntryModify("surface", "unpaved", "paved"),
            StringMapEntryModify("cycleway:surface", "paving_stones", "paved"),
            StringMapEntryModify("footway:surface", "asphalt", "paved"),
            StringMapEntryModify("cycleway:surface:note", "old", "foo"),
            StringMapEntryModify("footway:surface:note", "old", "bar"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags,
            Surface.PAVED_AREA, "foo",
            Surface.PAVED_ROAD, "bar", null, *expectedChanges)
    }

    @Test
    fun `segregated=yes tag is added`() {
        val tags = mapOf("highway" to "path")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("cycleway:surface", "paving_stones"),
            StringMapEntryAdd("footway:surface", "sand"),
            StringMapEntryAdd("segregated", "yes"),
        )
        verifyAnswerWithSeparateFootwayCyclewaySurfaces(tags,
            Surface.PAVING_STONES, null, Surface.SAND, null, null, *expectedChanges)
    }
}

