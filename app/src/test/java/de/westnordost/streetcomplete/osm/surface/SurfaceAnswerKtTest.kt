package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import org.assertj.core.api.Assertions
import org.junit.Test

internal class SurfaceAnswerKtTest {

    @Test fun `apply surface`() {
        verifyAnswer(
            mapOf("highway" to "residential"),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(StringMapEntryAdd("surface", "asphalt"))
        )
    }

    @Test fun `apply non-changed surface`() {
        verifyAnswer(
            mapOf(
                "highway" to "residential",
                "surface" to "asphalt"
            ),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString())
            )
        )
    }

    @Test fun `remove mismatching tracktype`() {
        verifyAnswer(
            mapOf(
                "highway" to "residential",
                "tracktype" to "grade5",
                "check_date:tracktype" to "2011-11-11"
            ),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("tracktype", "grade5"),
                StringMapEntryDelete("check_date:tracktype", "2011-11-11"),
            )
        )
    }

    @Test fun `keep matching tracktype`() {
        verifyAnswer(
            mapOf(
                "highway" to "residential",
                "tracktype" to "grade1"
            ),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt")
            )
        )
    }

    @Test fun `removes associated values when surface changed`() {
        verifyAnswer(
            mapOf(
                "highway" to "residential",
                "surface" to "compacted",
                "surface:grade" to "3",
                "smoothness" to "well",
                "smoothness:date" to "2011-11-11",
                "check_date:smoothness" to "2011-11-11",
                "tracktype" to "grade5"
            ),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(
                StringMapEntryModify("surface", "compacted", "asphalt"),
                StringMapEntryDelete("surface:grade", "3"),
                StringMapEntryDelete("smoothness", "well"),
                StringMapEntryDelete("smoothness:date", "2011-11-11"),
                StringMapEntryDelete("check_date:smoothness", "2011-11-11"),
                StringMapEntryDelete("tracktype", "grade5"),
            )
        )
    }

    @Test fun `always removes source-surface`() {
        verifyAnswer(
            mapOf("highway" to "residential", "source:surface" to "bing"),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("source:surface", "bing"),
            )
        )
    }

    @Test fun `add note when specified`() {
        verifyAnswer(
            mapOf(),
            SurfaceAnswer(Surface.ASPHALT, "nurgle"),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryAdd("surface:note", "nurgle"),
            )
        )
    }

    @Test fun `remove note when not specified`() {
        verifyAnswer(
            mapOf("surface:note" to "nurgle"),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
                StringMapEntryDelete("surface:note", "nurgle"),
            )
        )
    }

    @Test fun `remove surface colour when changing surface`() {
        verifyAnswer(
            mapOf("surface:colour" to "transparent", "surface" to "mud"),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(
                StringMapEntryModify("surface", "mud", "asphalt"),
                StringMapEntryDelete("surface:colour", "transparent"),
            )
        )
    }

    @Test fun `keep surface colour when not changing surface`() {
        verifyAnswer(
            mapOf("surface:colour" to "transparent", "surface" to "asphalt"),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(
                StringMapEntryModify("surface", "asphalt", "asphalt"),
                StringMapEntryAdd("check_date:surface", nowAsCheckDateString()),
            )
        )
    }

    @Test fun `sidewalk surface marked as tag on road is not touched`() {
        verifyAnswer(
            mapOf("highway" to "tertiary", "sidewalk:surface" to "paving_stones"),
            SurfaceAnswer(Surface.ASPHALT),
            arrayOf(
                StringMapEntryAdd("surface", "asphalt"),
            )
        )
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: SurfaceAnswer, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
