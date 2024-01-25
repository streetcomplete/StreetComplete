package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals

class SmoothnessAnswerKtTest {

    @Test fun `apply smoothness answer`() {
        assertEquals(
            setOf(StringMapEntryAdd("smoothness", "excellent")),
            SmoothnessValueAnswer(Smoothness.EXCELLENT).appliedTo(mapOf())
        )
    }

    @Test fun `deletes possibly out of date info`() {
        assertEquals(
            setOf(
                StringMapEntryModify("smoothness", "excellent", "excellent"),
                StringMapEntryDelete("smoothness:date", "2000-10-10"),
                StringMapEntryDelete("surface:grade", "1"),
                StringMapEntryAdd("check_date:smoothness", nowAsCheckDateString())
            ),
            SmoothnessValueAnswer(Smoothness.EXCELLENT).appliedTo(mapOf(
                "smoothness" to "excellent",
                "smoothness:date" to "2000-10-10",
                "surface:grade" to "1"
            ))
        )
    }

    @Test fun `apply is actually steps answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("highway", "steps"),
                StringMapEntryDelete("smoothness", "excellent"),
                StringMapEntryDelete("smoothness:date", "2000-10-10"),
                StringMapEntryDelete("surface:grade", "3"),
                StringMapEntryDelete("check_date:smoothness", "2000-10-10")
            ),
            IsActuallyStepsAnswer.appliedTo(mapOf(
                "smoothness" to "excellent",
                "smoothness:date" to "2000-10-10",
                "surface" to "asphalt",
                "surface:grade" to "3",
                "check_date:smoothness" to "2000-10-10",
            ))
        )
    }

    @Test fun `apply wrong surface answer`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("smoothness", "excellent"),
                StringMapEntryDelete("smoothness:date", "2000-10-10"),
                StringMapEntryDelete("surface", "asphalt"),
                StringMapEntryDelete("surface:grade", "3"),
                StringMapEntryDelete("check_date:smoothness", "2000-10-10")
            ),
            WrongSurfaceAnswer.appliedTo(mapOf(
                "smoothness" to "excellent",
                "smoothness:date" to "2000-10-10",
                "surface" to "asphalt",
                "surface:grade" to "3",
                "check_date:smoothness" to "2000-10-10",
            ))
        )
    }
}

private fun SmoothnessAnswer.appliedTo(tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb)
    return cb.create().changes
}
