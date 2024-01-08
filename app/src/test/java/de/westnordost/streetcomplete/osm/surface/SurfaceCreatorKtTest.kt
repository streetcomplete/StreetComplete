package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import org.assertj.core.api.Assertions
import kotlin.test.Test

class SurfaceCreatorKtTest {

    @Test fun `apply surface`() {
        verify(
            mapOf("highway" to "residential"),
            SurfaceAndNote(Surface.ASPHALT),
            arrayOf(StringMapEntryAdd("surface", "asphalt"))
        )
    }

    @Test fun `apply non-changed surface updates check date`() {
        verify(
            mapOf(
                "highway" to "residential",
                "surface" to "asphalt"
            ),
            SurfaceAndNote(Surface.ASPHALT),
            arrayOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString())
            )
        )
    }

    @Test fun `remove mismatching tracktype`() {
        verify(
            mapOf(
                "highway" to "residential",
                "tracktype" to "grade5",
                "check_date:tracktype" to "2011-11-11"
            ),
            SurfaceAndNote(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("tracktype", "grade5"),
                StringMapEntryDelete("check_date:tracktype", "2011-11-11"),
            )
        )
    }

    @Test fun `keep matching tracktype`() {
        verify(
            mapOf(
                "highway" to "residential",
                "tracktype" to "grade1"
            ),
            SurfaceAndNote(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt")
            )
        )
    }

    @Test fun `remove associated tags when surface changed`() {
        verify(
            mapOf(
                "highway" to "residential",
                "surface" to "compacted",
                "surface:grade" to "3",
                "smoothness" to "well",
                "smoothness:date" to "2011-11-11",
                "check_date:smoothness" to "2011-11-11",
                "tracktype" to "grade5",
                "surface:colour" to "pink"
            ),
            SurfaceAndNote(Surface.ASPHALT),
            arrayOf(
                StringMapEntryModify("surface", "compacted", "asphalt"),
                StringMapEntryDelete("surface:grade", "3"),
                StringMapEntryDelete("surface:colour", "pink"),
                StringMapEntryDelete("smoothness", "well"),
                StringMapEntryDelete("smoothness:date", "2011-11-11"),
                StringMapEntryDelete("check_date:smoothness", "2011-11-11"),
                StringMapEntryDelete("tracktype", "grade5"),
            )
        )
    }

    @Test fun `keep associated tags when surface did not change`() {
        verify(
            mapOf(
                "highway" to "residential",
                "surface" to "asphalt",
                "surface:grade" to "3",
                "smoothness" to "well",
                "smoothness:date" to "2011-11-11",
                "check_date:smoothness" to "2011-11-11",
                "tracktype" to "grade1",
                "surface:colour" to "pink"
            ),
            SurfaceAndNote(Surface.ASPHALT),
            arrayOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString()),
            )
        )
    }

    @Test fun `always remove source-surface`() {
        verify(
            mapOf("highway" to "residential", "source:surface" to "bing"),
            SurfaceAndNote(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("source:surface", "bing"),
            )
        )
    }

    @Test fun `add note when specified`() {
        verify(
            mapOf(),
            SurfaceAndNote(Surface.ASPHALT, "gurgle"),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryAdd("surface:note", "gurgle"),
            )
        )
    }

    @Test fun `remove note when not specified`() {
        verify(
            mapOf("surface:note" to "nurgle"),
            SurfaceAndNote(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("surface:note", "nurgle"),
            )
        )
    }

    @Test fun `sidewalk surface marked as tag on road is not touched`() {
        verify(
            mapOf("highway" to "tertiary", "sidewalk:surface" to "paving_stones"),
            SurfaceAndNote(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
            )
        )
    }
}

private fun verify(tags: Map<String, String>, value: SurfaceAndNote, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    value.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
