package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import de.westnordost.streetcomplete.quests.cycleway.CyclewayAnswer
import de.westnordost.streetcomplete.quests.cycleway.CyclewaySide
import org.assertj.core.api.Assertions
import org.junit.Test

class CyclewayCreatorKtTest {

    @Test fun `apply nothing`() {
        verifyAnswer(
            mapOf(
                "cycleway:left" to "track",
                "cycleway" to "no",
                "cycleway:right" to "track"
            ),
            CyclewayAnswer(null, null),
            arrayOf()
        )
    }

    @Test fun `apply cycleway lane answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.EXCLUSIVE_LANE),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "lane"),
                StringMapEntryAdd("cycleway:both:lane", "exclusive")
            )
        )
    }

    @Test fun `apply advisory lane answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.ADVISORY_LANE),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "lane"),
                StringMapEntryAdd("cycleway:both:lane", "advisory")
            )
        )
    }

    @Test fun `apply cycleway track answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "track")
            )
        )
    }

    @Test fun `apply unspecified cycle lane answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.UNSPECIFIED_LANE),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "lane")
            )
        )
    }

    @Test fun `apply bus lane answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.BUSWAY),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "share_busway")
            )
        )
    }

    @Test fun `apply pictogram lane answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.PICTOGRAMS),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "shared_lane"),
                StringMapEntryAdd("cycleway:both:lane", "pictogram")
            )
        )
    }

    @Test fun `apply suggestion lane answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.PICTOGRAMS),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "shared_lane"),
                StringMapEntryAdd("cycleway:both:lane", "pictogram")
            )
        )
    }

    @Test fun `apply no cycleway answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.NONE),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "no")
            )
        )
    }

    @Test fun `apply cycleway on sidewalk answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.SIDEWALK_EXPLICIT),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:both:segregated", "no")
            )
        )
    }

    @Test fun `apply separate cycleway answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.SEPARATE),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "separate")
            )
        )
    }

    @Test fun `apply dual cycle track answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.DUAL_TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:both:oneway", "no")
            )
        )
    }

    @Test fun `apply dual cycle lane answer`() {
        verifyAnswer(
            mapOf(),
            bothSidesAnswer(Cycleway.DUAL_LANE),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "lane"),
                StringMapEntryAdd("cycleway:both:oneway", "no"),
                StringMapEntryAdd("cycleway:both:lane", "exclusive")
            )
        )
    }

    @Test fun `apply answer where left and right side are different`() {
        verifyAnswer(
            mapOf(),
            CyclewayAnswer(CyclewaySide(Cycleway.TRACK), CyclewaySide(Cycleway.NONE)),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:right", "no")
            )
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of oneway`() {
        // this would be a street that has tracks on both sides but is oneway=yes (in countries with
        // right hand traffic)
        verifyAnswer(
            mapOf(),
            CyclewayAnswer(CyclewaySide(Cycleway.TRACK, -1), CyclewaySide(Cycleway.TRACK), true),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:left:oneway", "-1"),
                StringMapEntryAdd("cycleway:right", "track"),
                StringMapEntryAdd("oneway:bicycle", "no")
            )
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of backward oneway`() {
        // this would be a street that has lanes on both sides but is oneway=-1 (in countries with
        // right hand traffic)
        verifyAnswer(
            mapOf(),
            CyclewayAnswer(CyclewaySide(Cycleway.TRACK, +1), CyclewaySide(Cycleway.TRACK), true),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:left:oneway", "yes"),
                StringMapEntryAdd("cycleway:right", "track"),
                StringMapEntryAdd("oneway:bicycle", "no")
            )
        )
    }

    @Test fun `apply cycleway track answer updates segregated key`() {
        verifyAnswer(
            mapOf(
                "cycleway:both:segregated" to "no",
                "cycleway:both" to "no"
            ),
            bothSidesAnswer(Cycleway.TRACK),
            arrayOf(
                StringMapEntryModify("cycleway:both:segregated", "no", "yes"),
                StringMapEntryModify("cycleway:both", "no", "track")
            )
        )
    }

    @Test fun `apply answer for both deletes any previous answers given for left, right, general`() {
        verifyAnswer(
            mapOf(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "advisory",
                "cycleway:left:segregated" to "maybe",
                "cycleway:left:oneway" to "yes",
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "pictogram",
                "cycleway:right:segregated" to "definitely",
                "cycleway:right:oneway" to "yes",
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "cycleway:segregated" to "definitely",
                "cycleway:oneway" to "yes"
            ),
            bothSidesAnswer(Cycleway.TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryDelete("cycleway:left", "lane"),
                StringMapEntryDelete("cycleway:left:lane", "advisory"),
                StringMapEntryDelete("cycleway:left:segregated", "maybe"),
                StringMapEntryDelete("cycleway:left:oneway", "yes"),
                StringMapEntryDelete("cycleway:right", "shared_lane"),
                StringMapEntryDelete("cycleway:right:lane", "pictogram"),
                StringMapEntryDelete("cycleway:right:segregated", "definitely"),
                StringMapEntryDelete("cycleway:right:oneway", "yes"),
                StringMapEntryDelete("cycleway", "shared_lane"),
                StringMapEntryDelete("cycleway:lane", "pictogram"),
                StringMapEntryDelete("cycleway:segregated", "definitely"),
                StringMapEntryDelete("cycleway:oneway", "yes")
            )
        )
    }

    @Test fun `apply answer for left, right deletes any previous answers given for both, general`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram",
                "cycleway:both:segregated" to "definitely",
                "cycleway:both:oneway" to "yes",
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "cycleway:segregated" to "definitely",
                "cycleway:oneway" to "yes",
            ),
            CyclewayAnswer(CyclewaySide(Cycleway.TRACK), CyclewaySide(Cycleway.NONE), false),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:right", "no"),
                StringMapEntryDelete("cycleway:both", "shared_lane"),
                StringMapEntryDelete("cycleway:both:lane", "pictogram"),
                StringMapEntryDelete("cycleway:both:segregated", "definitely"),
                StringMapEntryDelete("cycleway:both:oneway", "yes"),
                StringMapEntryDelete("cycleway", "shared_lane"),
                StringMapEntryDelete("cycleway:lane", "pictogram"),
                StringMapEntryDelete("cycleway:segregated", "definitely"),
                StringMapEntryDelete("cycleway:oneway", "yes"),
            )
        )
    }

    @Test fun `deletes lane subkey when new answer is not a lane`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive"
            ),
            bothSidesAnswer(Cycleway.TRACK),
            arrayOf(
                StringMapEntryModify("cycleway:both", "lane", "track"),
                StringMapEntryDelete("cycleway:both:lane", "exclusive")
            )
        )
    }

    @Test fun `deletes shared lane subkey when new answer is not a lane`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram"
            ),
            bothSidesAnswer(Cycleway.TRACK),
            arrayOf(
                StringMapEntryModify("cycleway:both", "shared_lane", "track"),
                StringMapEntryDelete("cycleway:both:lane", "pictogram")
            )
        )
    }

    @Test fun `deletes dual lane tag when new answer is not a dual lane`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            ),
            bothSidesAnswer(Cycleway.EXCLUSIVE_LANE),
            arrayOf(
                StringMapEntryModify("cycleway:both", "lane", "lane"),
                StringMapEntryModify("cycleway:both:lane", "exclusive", "exclusive"),
                StringMapEntryDelete("cycleway:both:oneway", "no")
            )
        )
    }

    @Test fun `modifies lane subkey when new answer is different lane`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram"
            ),
            bothSidesAnswer(Cycleway.SUGGESTION_LANE),
            arrayOf(
                StringMapEntryModify("cycleway:both", "shared_lane", "shared_lane"),
                StringMapEntryModify("cycleway:both:lane", "pictogram", "advisory")
            )
        )
    }

    @Test fun `deletes dual track tag when new answer is not a dual track`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "track",
                "cycleway:both:oneway" to "no"
            ),
            bothSidesAnswer(Cycleway.TRACK),
            arrayOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryDelete("cycleway:both:oneway", "no")
            )
        )
    }

    @Test fun `modify segregated tag if new answer is now segregated`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "no"
            ),
            bothSidesAnswer(Cycleway.TRACK),
            arrayOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryModify("cycleway:both:segregated", "no", "yes")
            )
        )
    }

    @Test fun `modify segregated tag if new answer is now not segregated`() {
        verifyAnswer(
            mapOf(
                "sidewalk" to "both",
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "yes"
            ),
            bothSidesAnswer(Cycleway.SIDEWALK_EXPLICIT),
            arrayOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryModify("cycleway:both:segregated", "yes", "no")
            )
        )
    }

    @Test fun `delete segregated tag if new answer is not a track or on sidewalk`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "no"
            ),
            bothSidesAnswer(Cycleway.BUSWAY),
            arrayOf(
                StringMapEntryModify("cycleway:both", "track", "share_busway"),
                StringMapEntryDelete("cycleway:both:segregated", "no")
            )
        )
    }

    @Test fun `sets check date if nothing changed`() {
        verifyAnswer(
            mapOf("cycleway:both" to "track"),
            bothSidesAnswer(Cycleway.TRACK),
            arrayOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryAdd("check_date:cycleway", nowAsCheckDateString())
            )
        )
    }

    @Test fun `updates check date if nothing changed`() {
        verifyAnswer(
            mapOf("cycleway:both" to "track", "check_date:cycleway" to "2000-11-11"),
            bothSidesAnswer(Cycleway.TRACK),
            arrayOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryModify("check_date:cycleway", "2000-11-11", nowAsCheckDateString())
            )
        )
    }

    @Test fun `remove oneway bicycle no tag if road is also a oneway for bicycles now`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            ),
            bothSidesAnswer(Cycleway.NONE),
            arrayOf(
                StringMapEntryModify("cycleway:both", "no", "no"),
                StringMapEntryDelete("oneway:bicycle", "no")
            )
        )
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: CyclewayAnswer, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}


private fun bothSidesAnswer(bothSides: Cycleway): CyclewayAnswer {
    val side = CyclewaySide(bothSides)
    return CyclewayAnswer(side, side)
}
