package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import org.assertj.core.api.Assertions
import org.junit.Test

class SmoothnessAnswerKtTest {

    @Test fun `apply smoothness answer`() {
        verifyAnswer(
            mapOf(),
            SmoothnessValueAnswer(Smoothness.EXCELLENT),
            arrayOf(StringMapEntryAdd("smoothness", "excellent"))
        )
    }

    @Test fun `deletes possibly out of date info`() {
        verifyAnswer(
            mapOf(
                "smoothness" to "excellent",
                "smoothness:date" to "2000-10-10",
                "surface:grade" to "1"
            ),
            SmoothnessValueAnswer(Smoothness.EXCELLENT),
            arrayOf(
                StringMapEntryModify("smoothness", "excellent", "excellent"),
                StringMapEntryDelete("smoothness:date", "2000-10-10"),
                StringMapEntryDelete("surface:grade", "1"),
                StringMapEntryAdd("check_date:smoothness", nowAsCheckDateString())
            ),
        )
    }

    @Test fun `apply is actually steps answer`() {
        verifyAnswer(
            mapOf(
                "smoothness" to "excellent",
                "smoothness:date" to "2000-10-10",
                "surface" to "asphalt",
                "surface:grade" to "3",
                "check_date:smoothness" to "2000-10-10",
            ),
            IsActuallyStepsAnswer,
            arrayOf(
                StringMapEntryAdd("highway", "steps"),
                StringMapEntryDelete("smoothness", "excellent"),
                StringMapEntryDelete("smoothness:date", "2000-10-10"),
                StringMapEntryDelete("surface:grade", "3"),
                StringMapEntryDelete("check_date:smoothness", "2000-10-10")
            )
        )
    }

    @Test fun `apply wrong surface answer`() {
        verifyAnswer(
            mapOf(
                "smoothness" to "excellent",
                "smoothness:date" to "2000-10-10",
                "surface" to "asphalt",
                "surface:grade" to "3",
                "check_date:smoothness" to "2000-10-10",
            ),
            WrongSurfaceAnswer,
            arrayOf(
                StringMapEntryDelete("smoothness", "excellent"),
                StringMapEntryDelete("smoothness:date", "2000-10-10"),
                StringMapEntryDelete("surface", "asphalt"),
                StringMapEntryDelete("surface:grade", "3"),
                StringMapEntryDelete("check_date:smoothness", "2000-10-10")
            )
        )
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: SmoothnessAnswer, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
