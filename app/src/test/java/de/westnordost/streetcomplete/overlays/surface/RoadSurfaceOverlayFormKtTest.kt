package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.surface.Surface
import org.assertj.core.api.Assertions
import org.junit.Test

class RoadSurfaceOverlayFormKtTest {
    private fun verifyAnswer(tags: Map<String, String>, surfaceAnswer: Surface, noteAnswer: String?, vararg expectedChanges: StringMapEntryChange) {
        val cb = StringMapChangesBuilder(tags)
        RoadSurfaceOverlayForm.editTags(cb, surfaceAnswer, noteAnswer)
        val changes = cb.create().changes
        Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
    }

    @Test
    fun `new different value is applied, associated tags are removed`() {
        val tags = mapOf("highway" to "tertiary", "surface" to "asphalt", "smoothness" to "bad", "surface:colour" to "red", "tracktype" to "grade5")
        val expectedChanges = arrayOf(
            StringMapEntryDelete("smoothness", "bad"),
            StringMapEntryDelete("surface:colour", "red"),
            StringMapEntryDelete("tracktype", "grade5"),
            StringMapEntryModify("surface", "asphalt", "paving_stones"),
        )
        verifyAnswer(tags, Surface.PAVING_STONES, null, *expectedChanges)
    }

    @Test
    fun `note tag is applied`() {
        val tags = mapOf("highway" to "tertiary")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("surface", "paved"),
            StringMapEntryAdd("surface:note", "zażółć"),
        )
        verifyAnswer(tags, Surface.PAVED_ROAD, "zażółć", *expectedChanges)
    }

    @Test
    fun `note tag is applied also to specific surfaces if it was supplied`() {
        // may happen in cases where it was present already
        val tags = mapOf("highway" to "tertiary")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("surface", "asphalt"),
            StringMapEntryAdd("surface:note", "zażółć"),
        )
        verifyAnswer(tags, Surface.ASPHALT, "zażółć", *expectedChanges)
    }

    @Test
    fun `note tag is removed if no longer supplied`() {
        val tags = mapOf("highway" to "tertiary", "surface" to "unpaved", "surface:note" to "you get what you reward for")
        val expectedChanges = arrayOf(
            StringMapEntryModify("surface", "unpaved", "ground"),
            StringMapEntryDelete("surface:note", "you get what you reward for"),
        )
        verifyAnswer(tags, Surface.GROUND_AREA, null, *expectedChanges)
    }

    @Test
    fun `sidewalk surface marked as tag on road is not touched`() {
        val tags = mapOf("highway" to "tertiary", "sidewalk:surface" to "paving_stones")
        val expectedChanges = arrayOf(
            StringMapEntryAdd("surface", "asphalt"),
        )
        verifyAnswer(tags, Surface.ASPHALT, null, *expectedChanges)
    }
}
