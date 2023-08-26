package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import org.assertj.core.api.Assertions
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SidewalkCreatorKtTest {
    @Test fun `apply nothing applies nothing`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalk(null, null),
            arrayOf()
        )
    }

    @Test fun `apply simple values`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalk(Sidewalk.YES, Sidewalk.YES),
            arrayOf(StringMapEntryAdd("sidewalk", "both"))
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalk(Sidewalk.NO, Sidewalk.NO),
            arrayOf(StringMapEntryAdd("sidewalk", "no"))
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalk(Sidewalk.YES, Sidewalk.NO),
            arrayOf(StringMapEntryAdd("sidewalk", "left"))
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalk(Sidewalk.NO, Sidewalk.YES),
            arrayOf(StringMapEntryAdd("sidewalk", "right"))
        )
    }

    @Test fun `apply value when each side differs`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalk(Sidewalk.YES, Sidewalk.SEPARATE),
            arrayOf(
                StringMapEntryAdd("sidewalk:left", "yes"),
                StringMapEntryAdd("sidewalk:right", "separate")
            )
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalk(Sidewalk.SEPARATE, Sidewalk.NO),
            arrayOf(
                StringMapEntryAdd("sidewalk:left", "separate"),
                StringMapEntryAdd("sidewalk:right", "no")
            )
        )
    }

    @Test fun `clean up previous tagging`() {
        verifyAnswer(
            mapOf(
                "sidewalk" to "different",
                "sidewalk:left" to "yes",
                "sidewalk:right" to "separate",
                "sidewalk:both" to "yes and separate ;-)"
            ),
            LeftAndRightSidewalk(Sidewalk.SEPARATE, Sidewalk.SEPARATE),
            arrayOf(
                StringMapEntryModify("sidewalk:both", "yes and separate ;-)", "separate"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "separate"),
                StringMapEntryDelete("sidewalk", "different"),
            )
        )
    }

    @Test fun `clean up previous tagging when applying value for each side`() {
        verifyAnswer(
            mapOf(
                "sidewalk" to "both",
                "sidewalk:both" to "yes",
            ),
            LeftAndRightSidewalk(Sidewalk.SEPARATE, Sidewalk.YES),
            arrayOf(
                StringMapEntryAdd("sidewalk:left", "separate"),
                StringMapEntryAdd("sidewalk:right", "yes"),
                StringMapEntryDelete("sidewalk", "both"),
                StringMapEntryDelete("sidewalk:both", "yes"),
            )
        )
    }

    @Test fun `updates check date`() {
        verifyAnswer(
            mapOf("sidewalk" to "both"),
            LeftAndRightSidewalk(Sidewalk.YES, Sidewalk.YES),
            arrayOf(
                StringMapEntryModify("sidewalk", "both", "both"),
                StringMapEntryAdd("check_date:sidewalk", nowAsCheckDateString())
            )
        )
        verifyAnswer(
            mapOf(
                "sidewalk:left" to "separate",
                "sidewalk:right" to "no"
            ),
            LeftAndRightSidewalk(Sidewalk.SEPARATE, Sidewalk.NO),
            arrayOf(
                StringMapEntryModify("sidewalk:left", "separate", "separate"),
                StringMapEntryModify("sidewalk:right", "no", "no"),
                StringMapEntryAdd("check_date:sidewalk", nowAsCheckDateString())
            )
        )
    }

    @Test fun `apply value only for one side`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalk(Sidewalk.YES, null),
            arrayOf(
                StringMapEntryAdd("sidewalk:left", "yes")
            )
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightSidewalk(null, Sidewalk.NO),
            arrayOf(
                StringMapEntryAdd("sidewalk:right", "no")
            )
        )
    }

    @Test fun `apply for one side does not touch the other side`() {
        verifyAnswer(
            mapOf("sidewalk:left" to "separate"),
            LeftAndRightSidewalk(null, Sidewalk.YES),
            arrayOf(
                StringMapEntryAdd("sidewalk:right", "yes")
            )
        )
        verifyAnswer(
            mapOf("sidewalk:right" to "yes"),
            LeftAndRightSidewalk(Sidewalk.NO, null),
            arrayOf(
                StringMapEntryAdd("sidewalk", "right"),
                StringMapEntryDelete("sidewalk:right", "yes"),
            )
        )
    }

    @Test fun `apply for one side does not touch the other side even if it is invalid`() {
        verifyAnswer(
            mapOf("sidewalk:left" to "some invalid value"),
            LeftAndRightSidewalk(null, Sidewalk.YES),
            arrayOf(
                StringMapEntryAdd("sidewalk:right", "yes")
            )
        )
        verifyAnswer(
            mapOf("sidewalk:right" to "another invalid value"),
            LeftAndRightSidewalk(Sidewalk.NO, null),
            arrayOf(
                StringMapEntryAdd("sidewalk:left", "no")
            )
        )
    }

    @Test fun `apply for one side does not change values for the other side even if it was defined for both sides before and invalid`() {
        verifyAnswer(
            mapOf("sidewalk:both" to "some invalid value"),
            LeftAndRightSidewalk(null, Sidewalk.YES),
            arrayOf(
                StringMapEntryAdd("sidewalk:right", "yes"),
                StringMapEntryDelete("sidewalk:both", "some invalid value"),
                StringMapEntryAdd("sidewalk:left", "some invalid value"),
            )
        )
        verifyAnswer(
            mapOf("sidewalk:both" to "some invalid value"),
            LeftAndRightSidewalk(Sidewalk.YES, null),
            arrayOf(
                StringMapEntryAdd("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:both", "some invalid value"),
                StringMapEntryAdd("sidewalk:right", "some invalid value"),
            )
        )
    }

    @Test fun `apply conflates values`() {
        verifyAnswer(
            mapOf(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "yes",
            ),
            LeftAndRightSidewalk(Sidewalk.YES, null),
            arrayOf(
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "yes"),
                StringMapEntryAdd("sidewalk", "both"),
            )
        )
        verifyAnswer(
            mapOf(
                "sidewalk:right" to "no",
            ),
            LeftAndRightSidewalk(Sidewalk.YES, null),
            arrayOf(
                StringMapEntryDelete("sidewalk:right", "no"),
                StringMapEntryAdd("sidewalk", "left"),
            )
        )
        verifyAnswer(
            mapOf(
                "sidewalk:right" to "yes",
            ),
            LeftAndRightSidewalk(Sidewalk.NO, null),
            arrayOf(
                StringMapEntryDelete("sidewalk:right", "yes"),
                StringMapEntryAdd("sidewalk", "right"),
            )
        )
        verifyAnswer(
            mapOf(
                "sidewalk:right" to "no",
            ),
            LeftAndRightSidewalk(Sidewalk.NO, null),
            arrayOf(
                StringMapEntryDelete("sidewalk:right", "no"),
                StringMapEntryAdd("sidewalk", "no"),
            )
        )
    }

    @Test fun `apply does not conflate values non-yes-no-values`() {
        verifyAnswer(
            mapOf(
                "sidewalk:right" to "separate",
            ),
            LeftAndRightSidewalk(Sidewalk.SEPARATE, null),
            arrayOf(
                StringMapEntryDelete("sidewalk:right", "separate"),
                StringMapEntryAdd("sidewalk:both", "separate"),
            )
        )
    }

    @Test
    fun `applying invalid left throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            LeftAndRightSidewalk(Sidewalk.INVALID, null).applyTo(StringMapChangesBuilder(mapOf()))
        }
    }

    @Test
    fun `applying invalid right throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            LeftAndRightSidewalk(null, Sidewalk.INVALID).applyTo(StringMapChangesBuilder(mapOf()))
        }
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: LeftAndRightSidewalk, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
