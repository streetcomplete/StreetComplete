package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.toCheckDateString
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDate

class SidewalkKtTest {
    @Test fun `apply simple values`() {
        verifyAnswer(
            mapOf(),
            SidewalkSides(Sidewalk.YES, Sidewalk.YES),
            arrayOf(StringMapEntryAdd("sidewalk", "both"))
        )
        verifyAnswer(
            mapOf(),
            SidewalkSides(Sidewalk.NO, Sidewalk.NO),
            arrayOf(StringMapEntryAdd("sidewalk", "no"))
        )
        verifyAnswer(
            mapOf(),
            SidewalkSides(Sidewalk.YES, Sidewalk.NO),
            arrayOf(StringMapEntryAdd("sidewalk", "left"))
        )
        verifyAnswer(
            mapOf(),
            SidewalkSides(Sidewalk.NO, Sidewalk.YES),
            arrayOf(StringMapEntryAdd("sidewalk", "right"))
        )
        verifyAnswer(
            mapOf(),
            SidewalkSides(Sidewalk.SEPARATE, Sidewalk.SEPARATE),
            arrayOf(StringMapEntryAdd("sidewalk", "separate"))
        )
    }

    @Test fun `apply value when each side differs`() {
        verifyAnswer(
            mapOf(),
            SidewalkSides(Sidewalk.YES, Sidewalk.SEPARATE),
            arrayOf(
                StringMapEntryAdd("sidewalk:left", "yes"),
                StringMapEntryAdd("sidewalk:right", "separate")
            )
        )
        verifyAnswer(
            mapOf(),
            SidewalkSides(Sidewalk.SEPARATE, Sidewalk.NO),
            arrayOf(
                StringMapEntryAdd("sidewalk:left", "separate"),
                StringMapEntryAdd("sidewalk:right", "no")
            )
        )
    }

    @Test fun `clean up previous tagging when applying simple values`() {
        verifyAnswer(
            mapOf(
                "sidewalk:left" to "yes",
                "sidewalk:right" to "separate",
                "sidewalk:both" to "yes and separate ;-)",
                "sidewalk:left:surface" to "jello",
                "sidewalk:both:oneway" to "yes"
            ),
            SidewalkSides(Sidewalk.SEPARATE, Sidewalk.SEPARATE),
            arrayOf(
                StringMapEntryAdd("sidewalk", "separate"),
                StringMapEntryDelete("sidewalk:left", "yes"),
                StringMapEntryDelete("sidewalk:right", "separate"),
                StringMapEntryDelete("sidewalk:both", "yes and separate ;-)"),
                StringMapEntryDelete("sidewalk:left:surface", "jello"),
                StringMapEntryDelete("sidewalk:both:oneway", "yes"),
            )
        )
    }

    @Test fun `clean up previous tagging when applying value for each side`() {
        verifyAnswer(
            mapOf(
                "sidewalk" to "both",
                "sidewalk:both" to "yes",
                "sidewalk:left:surface" to "jello",
                "sidewalk:both:oneway" to "yes"
            ),
            SidewalkSides(Sidewalk.SEPARATE, Sidewalk.YES),
            arrayOf(
                StringMapEntryAdd("sidewalk:left", "separate"),
                StringMapEntryAdd("sidewalk:right", "yes"),
                StringMapEntryDelete("sidewalk", "both"),
                StringMapEntryDelete("sidewalk:both", "yes"),
                StringMapEntryDelete("sidewalk:left:surface", "jello"),
                StringMapEntryDelete("sidewalk:both:oneway", "yes"),
            )
        )
    }

    @Test fun `updates check date`() {
        verifyAnswer(
            mapOf("sidewalk" to "both"),
            SidewalkSides(Sidewalk.YES, Sidewalk.YES),
            arrayOf(
                StringMapEntryModify("sidewalk", "both", "both"),
                StringMapEntryAdd("check_date:sidewalk", LocalDate.now().toCheckDateString())
            )
        )
        verifyAnswer(
            mapOf("sidewalk:left" to "separate", "sidewalk:right" to "no"),
            SidewalkSides(Sidewalk.SEPARATE, Sidewalk.NO),
            arrayOf(
                StringMapEntryModify("sidewalk:left", "separate", "separate"),
                StringMapEntryModify("sidewalk:right", "no", "no"),
                StringMapEntryAdd("check_date:sidewalk", LocalDate.now().toCheckDateString())
            )
        )
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: SidewalkSides, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}
