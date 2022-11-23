package de.westnordost.streetcomplete.osm.cycleway


import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
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
            LeftAndRightCycleway(null, null),
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

    @Test fun `apply unspecified cycle lane answer does not remove previous specific lane answer`() {
        verifyAnswer(
            mapOf("cycleway:both:lane" to "exclusive"),
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
            bothSidesAnswer(Cycleway.SUGGESTION_LANE),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "shared_lane"),
                StringMapEntryAdd("cycleway:both:lane", "advisory")
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
                StringMapEntryAdd("cycleway:both:segregated", "no"),
                StringMapEntryAdd("sidewalk", "both"),
            )
        )
    }

    @Test fun `apply cycleway on sidewalk answer on one side only`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightCycleway(null, Cycleway.SIDEWALK_EXPLICIT),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:both:segregated", "no"),
                StringMapEntryAdd("sidewalk:right", "yes"),
            )
        )
        verifyAnswer(
            mapOf("sidewalk" to "right"),
            LeftAndRightCycleway(Cycleway.SIDEWALK_EXPLICIT, null),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:both:segregated", "no"),
                StringMapEntryModify("sidewalk", "right", "both"),
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

    @Test fun `apply dual cycle track answer in oneway`() {
        verifyAnswer(
            mapOf("oneway" to "yes"),
            bothSidesAnswer(Cycleway.DUAL_TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:both:oneway", "no"),
                StringMapEntryAdd("oneway:bicycle", "no"),
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

    @Test fun `apply dual cycle lane answer in oneway`() {
        verifyAnswer(
            mapOf("oneway" to "yes"),
            bothSidesAnswer(Cycleway.DUAL_LANE),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "lane"),
                StringMapEntryAdd("cycleway:both:oneway", "no"),
                StringMapEntryAdd("cycleway:both:lane", "exclusive"),
                StringMapEntryAdd("oneway:bicycle", "no"),
            )
        )
    }

    @Test fun `apply answer where left and right side are different`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightCycleway(Cycleway.TRACK, Cycleway.NONE),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:right", "no")
            )
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of oneway`() {
        verifyAnswer(
            mapOf("oneway" to "yes"),
            LeftAndRightCycleway(Cycleway.TRACK, Cycleway.TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:left:oneway", "-1"),
                StringMapEntryAdd("oneway:bicycle", "no")
            )
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of backward oneway`() {
        verifyAnswer(
            mapOf("oneway" to "-1"),
            LeftAndRightCycleway(Cycleway.TRACK, Cycleway.TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:right:oneway", "yes"),
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
                StringMapEntryDelete("cycleway:oneway", "yes"),
                StringMapEntryAdd("cycleway:both:oneway", "yes"),
                StringMapEntryAdd("cycleway:both:segregated", "yes"),
            )
        )
    }

    @Test fun `apply answer for left, right deletes any previous answers given for both, general`() {
        verifyAnswer(
            mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram",
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "cycleway:segregated" to "yes",
                "cycleway:oneway" to "yes",
            ),
            LeftAndRightCycleway(Cycleway.TRACK, Cycleway.NONE),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:right", "no"),
                StringMapEntryDelete("cycleway:both", "shared_lane"),
                StringMapEntryDelete("cycleway:both:lane", "pictogram"),
                StringMapEntryDelete("cycleway", "shared_lane"),
                StringMapEntryDelete("cycleway:lane", "pictogram"),
                StringMapEntryDelete("cycleway:oneway", "yes"),
                StringMapEntryDelete("cycleway:segregated", "yes"),
                StringMapEntryAdd("cycleway:both:oneway", "yes"),
                StringMapEntryAdd("cycleway:left:segregated", "yes"),
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

    @Test fun `apply value only for one side`() {
        verifyAnswer(
            mapOf(),
            LeftAndRightCycleway(Cycleway.TRACK, null),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track")
            )
        )
        verifyAnswer(
            mapOf(),
            LeftAndRightCycleway(null, Cycleway.TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:right", "track")
            )
        )
    }

    @Test fun `apply for one side does not touch the other side`() {
        verifyAnswer(
            mapOf("cycleway:right" to "no"),
            LeftAndRightCycleway(Cycleway.TRACK, null),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track")
            )
        )
        verifyAnswer(
            mapOf("cycleway:left" to "no"),
            LeftAndRightCycleway(null, Cycleway.TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:right", "track")
            )
        )
    }

    @Test fun `apply for one side does not touch the other side even if it is invalid`() {
        verifyAnswer(
            mapOf("cycleway:right" to "invalid"),
            LeftAndRightCycleway(Cycleway.TRACK, null),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track")
            )
        )
        verifyAnswer(
            mapOf("cycleway:left" to "invalid"),
            LeftAndRightCycleway(null, Cycleway.TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:right", "track")
            )
        )
    }

    @Test fun `apply for one side does not change values for the other side even if it was defined for both sides before and invalid`() {
        verifyAnswer(
            mapOf("cycleway:both" to "invalid"),
            LeftAndRightCycleway(Cycleway.TRACK, null),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:right", "invalid"),
                StringMapEntryDelete("cycleway:both", "invalid"),
            )
        )
        verifyAnswer(
            mapOf("cycleway:both" to "invalid"),
            LeftAndRightCycleway(null, Cycleway.TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "invalid"),
                StringMapEntryAdd("cycleway:right", "track"),
                StringMapEntryDelete("cycleway:both", "invalid"),
            )
        )
    }

    @Test fun `apply conflates values`() {
        verifyAnswer(
            mapOf("cycleway:left" to "no", "cycleway:right" to "no"),
            LeftAndRightCycleway(Cycleway.NONE, null),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "no"),
                StringMapEntryDelete("cycleway:left", "no"),
                StringMapEntryDelete("cycleway:right", "no"),
            )
        )
    }

    @Test fun `apply updates oneway not for cyclists`() {
        // contra-flow side set to explicitly not oneway -> not oneway for cyclists
        verifyAnswer(
            mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes"
            ),
            LeftAndRightCycleway(Cycleway.NONE_NO_ONEWAY, null),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "no"),
                StringMapEntryModify("oneway:bicycle", "no", "no"),
            )
        )
        // track on right side is dual-way -> not oneway for cyclists
        verifyAnswer(
            mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes"
            ),
            LeftAndRightCycleway(null, Cycleway.DUAL_TRACK),
            arrayOf(
                StringMapEntryAdd("cycleway:right", "track"),
                StringMapEntryAdd("cycleway:right:oneway", "no"),
                StringMapEntryModify("oneway:bicycle", "no", "no"),
            )
        )
        // not a oneway at all
        verifyAnswer(
            mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "no"
            ),
            LeftAndRightCycleway(Cycleway.NONE, null),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "no"),
                StringMapEntryDelete("oneway:bicycle", "no"),
            )
        )
        // contra-flow side is explicitly oneway and flow-side is not dual-way
        verifyAnswer(
            mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes"
            ),
            LeftAndRightCycleway(Cycleway.NONE, Cycleway.NONE),
            arrayOf(
                StringMapEntryAdd("cycleway:both", "no"),
                StringMapEntryDelete("oneway:bicycle", "no"),
            )
        )
        // no oneway for cyclists is deleted on left side but dual track on right side still there
        // -> still not oneway for cyclists
        verifyAnswer(
            mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes",
                "cycleway:both" to "track",
                "cycleway:right:oneway" to "no",
            ),
            LeftAndRightCycleway(Cycleway.NONE, null),
            arrayOf(
                StringMapEntryAdd("cycleway:left", "no"),
                StringMapEntryAdd("cycleway:right", "track"),
                StringMapEntryDelete("cycleway:both", "track"),
                StringMapEntryModify("oneway:bicycle", "no", "no"),
            )
        )
        // no oneway for cyclists is deleted on left side -> oneway for cyclists now
        verifyAnswer(
            mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes",
                "cycleway:left" to "no",
                "cycleway:right" to "track",
            ),
            LeftAndRightCycleway(Cycleway.NONE, null),
            arrayOf(
                StringMapEntryModify("cycleway:left", "no", "no"),
                StringMapEntryDelete("oneway:bicycle", "no"),
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `applying invalid left throws exception`() {
        LeftAndRightCycleway(Cycleway.INVALID, null).applyTo(StringMapChangesBuilder(mapOf()), false)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `applying invalid right throws exception`() {
        LeftAndRightCycleway(null, Cycleway.INVALID).applyTo(StringMapChangesBuilder(mapOf()), false)
    }
}

private fun verifyAnswer(tags: Map<String, String>, answer: LeftAndRightCycleway, expectedChanges: Array<StringMapEntryChange>) {
    val cb = StringMapChangesBuilder(tags)
    answer.applyTo(cb, false)
    val changes = cb.create().changes
    Assertions.assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}


private fun bothSidesAnswer(bothSides: Cycleway): LeftAndRightCycleway =
    LeftAndRightCycleway(bothSides, bothSides)
