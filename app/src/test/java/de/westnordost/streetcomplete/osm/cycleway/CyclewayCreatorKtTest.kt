package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.cycleway.Direction.*
import de.westnordost.streetcomplete.osm.nowAsCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CyclewayCreatorKtTest {

    @Test fun `apply nothing`() {
        assertEquals(
            setOf(),
            LeftAndRightCycleway(null, null).appliedTo(mapOf(
                "cycleway:left" to "track",
                "cycleway" to "no",
                "cycleway:right" to "track"
            ))
        )
    }

    @Test fun `apply cycleway lane answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "lane"),
                StringMapEntryAdd("cycleway:both:lane", "exclusive")
            ),
            cycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE).appliedTo(mapOf())
        )
    }

    @Test fun `apply advisory lane answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "lane"),
                StringMapEntryAdd("cycleway:both:lane", "advisory")
            ),
            cycleway(ADVISORY_LANE, ADVISORY_LANE).appliedTo(mapOf())
        )
    }

    @Test fun `apply cycleway track answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "track")
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf())
        )
    }

    @Test fun `apply unspecified cycle lane answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "lane")
            ),
            cycleway(UNSPECIFIED_LANE, UNSPECIFIED_LANE).appliedTo(mapOf())
        )
    }

    @Test fun `apply unspecified cycle lane answer does not remove previous specific lane answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "lane"),
                StringMapEntryModify("cycleway:both:lane", "exclusive", "exclusive"),
            ),
            cycleway(UNSPECIFIED_LANE, UNSPECIFIED_LANE).appliedTo(mapOf(
                "cycleway:both:lane" to "exclusive"
            ))
        )
    }

    @Test fun `apply bus lane answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "share_busway")
            ),
            cycleway(BUSWAY, BUSWAY).appliedTo(mapOf())
        )
    }

    @Test fun `apply pictogram lane answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "shared_lane"),
                StringMapEntryAdd("cycleway:both:lane", "pictogram")
            ),
            cycleway(PICTOGRAMS, PICTOGRAMS).appliedTo(mapOf())
        )
    }

    @Test fun `apply suggestion lane answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "shared_lane"),
                StringMapEntryAdd("cycleway:both:lane", "advisory")
            ),
            cycleway(SUGGESTION_LANE, SUGGESTION_LANE).appliedTo(mapOf())
        )
    }

    @Test fun `apply no cycleway answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "no")
            ),
            cycleway(NONE, NONE).appliedTo(mapOf())
        )
    }

    @Test fun `apply cycleway on sidewalk answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:both:segregated", "no"),
                StringMapEntryAdd("sidewalk", "both"),
            ),
            cycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT).appliedTo(mapOf())
        )
    }

    @Test fun `apply cycleway on sidewalk answer on one side only`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:right", "track"),
                StringMapEntryAdd("cycleway:right:segregated", "no"),
                StringMapEntryAdd("sidewalk:right", "yes"),
            ),
            cycleway(null, SIDEWALK_EXPLICIT).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:left:segregated", "no"),
                StringMapEntryModify("sidewalk", "right", "both"),
            ),
            cycleway(SIDEWALK_EXPLICIT, null).appliedTo(mapOf("sidewalk" to "right"))
        )
    }

    @Test fun `apply separate cycleway answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "separate")
            ),
            cycleway(SEPARATE, SEPARATE).appliedTo(mapOf())
        )
    }

    @Test fun `apply allowed on sidewalk on one side only`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:right", "no"),
                StringMapEntryAdd("sidewalk:right:bicycle", "yes"),
                StringMapEntryAdd("sidewalk:right:bicycle:signed", "yes")
            ),
            cycleway(null, SIDEWALK_OK).appliedTo(mapOf())
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "no"),
                StringMapEntryAdd("sidewalk:left:bicycle", "yes"),
                StringMapEntryAdd("sidewalk:left:bicycle:signed", "yes")
            ),
            cycleway(SIDEWALK_OK, null).appliedTo(mapOf())
        )
    }

    @Test fun `apply allowed on sidewalk to both sides`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "no"),
                StringMapEntryAdd("sidewalk:both:bicycle", "yes"),
                StringMapEntryAdd("sidewalk:both:bicycle:signed", "yes")
            ),
            cycleway(SIDEWALK_OK, SIDEWALK_OK).appliedTo(mapOf())
        )
    }

    @Test fun `apply none to a side where SIDEWALK_OK is set`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:right", "no", "no"),
                StringMapEntryDelete("sidewalk:right:bicycle", "yes"),
                StringMapEntryDelete("sidewalk:right:bicycle:signed", "yes")
            ),
            cycleway(null, NONE).appliedTo(mapOf(
                "cycleway:right" to "no",
                "sidewalk:right:bicycle" to "yes",
                "sidewalk:right:bicycle:signed" to "yes"
            ))
        )
    }

    @Test fun `apply none to a side where sidewalk bicycle designated it set`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:right", "no", "no"),
                StringMapEntryDelete("sidewalk:right:bicycle", "designated"),
            ),
            cycleway(null, NONE).appliedTo(mapOf(
                "cycleway:right" to "no",
                "sidewalk:right:bicycle" to "designated",
            ))
        )
    }

    @Test fun `apply dual cycle track answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:both:oneway", "no")
            ),
            cycleway(TRACK to BOTH, TRACK to BOTH).appliedTo(mapOf())
        )
    }

    @Test fun `apply dual cycle track answer in oneway`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:both:oneway", "no"),
                StringMapEntryAdd("oneway:bicycle", "no"),
            ),
            cycleway(TRACK to BOTH, TRACK to BOTH).appliedTo(mapOf("oneway" to "yes"))
        )
    }

    @Test fun `apply dual cycle lane answer`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "lane"),
                StringMapEntryAdd("cycleway:both:oneway", "no"),
                StringMapEntryAdd("cycleway:both:lane", "exclusive")
            ),
            cycleway(EXCLUSIVE_LANE to BOTH, EXCLUSIVE_LANE to BOTH).appliedTo(mapOf())
        )
    }

    @Test fun `apply dual cycle lane answer in oneway`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "lane"),
                StringMapEntryAdd("cycleway:both:oneway", "no"),
                StringMapEntryAdd("cycleway:both:lane", "exclusive"),
                StringMapEntryAdd("oneway:bicycle", "no"),
            ),
            cycleway(EXCLUSIVE_LANE to BOTH, EXCLUSIVE_LANE to BOTH).appliedTo(mapOf(
                "oneway" to "yes"
            ))
        )
    }

    @Test fun `apply answer where left and right side are different`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:right", "no")
            ),
            cycleway(TRACK, NONE).appliedTo(mapOf())
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of oneway`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:left:oneway", "-1"),
                StringMapEntryAdd("oneway:bicycle", "no")
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf("oneway" to "yes"))
        )

        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:right:oneway", "-1"),
                StringMapEntryAdd("oneway:bicycle", "no")
            ),
            cycleway(TRACK, TRACK, true).appliedTo(mapOf("oneway" to "yes"), true)
        )
    }

    @Test fun `apply answer where there exists a cycleway in opposite direction of backward oneway`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:right:oneway", "yes"),
                StringMapEntryAdd("oneway:bicycle", "no")
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf("oneway" to "-1"))
        )

        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryAdd("cycleway:left:oneway", "yes"),
                StringMapEntryAdd("oneway:bicycle", "no")
            ),
            cycleway(TRACK, TRACK, true).appliedTo(mapOf("oneway" to "-1"), true)
        )
    }

    @Test fun `apply cycleway track answer updates segregated key`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both:segregated", "no", "yes"),
                StringMapEntryModify("cycleway:both", "no", "track")
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf(
                "cycleway:both:segregated" to "no",
                "cycleway:both" to "no"
            ))
        )
    }

    @Test fun `apply answer for both deletes any previous answers given for left, right, general`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "track"),
                StringMapEntryDelete("cycleway:left", "lane"),
                StringMapEntryDelete("cycleway:left:lane", "advisory"),
                StringMapEntryDelete("cycleway:left:segregated", "maybe"),
                StringMapEntryDelete("cycleway:right", "shared_lane"),
                StringMapEntryDelete("cycleway:right:lane", "pictogram"),
                StringMapEntryDelete("cycleway:right:segregated", "definitely"),
                StringMapEntryDelete("cycleway", "shared_lane"),
                StringMapEntryDelete("cycleway:lane", "pictogram"),
                StringMapEntryDelete("cycleway:segregated", "definitely"),
                StringMapEntryDelete("cycleway:oneway", "yes"),
                StringMapEntryModify("cycleway:left:oneway", "yes", "-1"),
                StringMapEntryModify("cycleway:right:oneway", "yes", "yes"),
                StringMapEntryAdd("cycleway:both:segregated", "yes"),
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf(
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
            ))
        )
    }

    @Test fun `apply answer for left, right deletes any previous answers given for both, general`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:right", "no"),
                StringMapEntryDelete("cycleway:both", "shared_lane"),
                StringMapEntryDelete("cycleway:both:lane", "pictogram"),
                StringMapEntryDelete("cycleway", "shared_lane"),
                StringMapEntryDelete("cycleway:lane", "pictogram"),
                StringMapEntryDelete("cycleway:oneway", "yes"),
                StringMapEntryDelete("cycleway:segregated", "yes"),
                StringMapEntryAdd("cycleway:left:oneway", "-1"),
                StringMapEntryAdd("cycleway:left:segregated", "yes"),
            ),
            cycleway(TRACK, NONE).appliedTo(mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram",
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "cycleway:segregated" to "yes",
                "cycleway:oneway" to "yes",
            ))
        )
    }

    @Test fun `deletes lane subkey when new answer is not a lane`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "lane", "track"),
                StringMapEntryDelete("cycleway:both:lane", "exclusive")
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive"
            ))
        )
    }

    @Test fun `deletes shared lane subkey when new answer is not a lane`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "shared_lane", "track"),
                StringMapEntryDelete("cycleway:both:lane", "pictogram")
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram"
            ))
        )
    }

    @Test fun `modifies oneway tag tag when new answer is not a dual lane`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "lane", "lane"),
                StringMapEntryModify("cycleway:both:lane", "exclusive", "exclusive"),
                StringMapEntryDelete("cycleway:both:oneway", "no"),
                StringMapEntryAdd("cycleway:left:oneway", "-1"),
                StringMapEntryAdd("cycleway:right:oneway", "yes"),
            ),
            cycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE).appliedTo(mapOf(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            ))
        )
    }

    @Test fun `modifies lane subkey when new answer is different lane`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "shared_lane", "shared_lane"),
                StringMapEntryModify("cycleway:both:lane", "pictogram", "advisory")
            ),
            cycleway(SUGGESTION_LANE, SUGGESTION_LANE).appliedTo(mapOf(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram"
            ))
        )
    }

    @Test fun `modifies oneway tag when new answer is not a dual track`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryDelete("cycleway:both:oneway", "no"),
                StringMapEntryAdd("cycleway:left:oneway", "-1"),
                StringMapEntryAdd("cycleway:right:oneway", "yes"),
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf(
                "cycleway:both" to "track",
                "cycleway:both:oneway" to "no"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryDelete("cycleway:both:oneway", "no"),
                StringMapEntryAdd("cycleway:left:oneway", "yes"),
                StringMapEntryAdd("cycleway:right:oneway", "-1"),
            ),
            cycleway(TRACK, TRACK, true).appliedTo(mapOf(
                "cycleway:both" to "track",
                "cycleway:both:oneway" to "no"
            ), true)
        )
    }

    @Test fun `modify segregated tag if new answer is now segregated`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryModify("cycleway:both:segregated", "no", "yes")
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf(
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "no"
            ))
        )
    }

    @Test fun `modify segregated tag if new answer is now not segregated`() {
        assertEquals(
            setOf(
                StringMapEntryModify("sidewalk", "both", "both"),
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryModify("cycleway:both:segregated", "yes", "no")
            ),
            cycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT).appliedTo(mapOf(
                "sidewalk" to "both",
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "yes"
            ))
        )
    }

    @Test fun `delete segregated tag if new answer is not a track or on sidewalk`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "track", "share_busway"),
                StringMapEntryDelete("cycleway:both:segregated", "no")
            ),
            cycleway(BUSWAY, BUSWAY).appliedTo(mapOf(
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "no"
            ))
        )
    }

    @Test fun `sets check date if nothing changed`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryAdd("check_date:cycleway", nowAsCheckDateString())
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf("cycleway:both" to "track"))
        )
    }

    @Test fun `updates check date if nothing changed`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "track", "track"),
                StringMapEntryModify("check_date:cycleway", "2000-11-11", nowAsCheckDateString())
            ),
            cycleway(TRACK, TRACK).appliedTo(mapOf(
                "cycleway:both" to "track",
                "check_date:cycleway" to "2000-11-11"
            ))
        )
    }

    @Test fun `remove oneway bicycle no tag if road is also a oneway for bicycles now`() {
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:both", "no", "no"),
                StringMapEntryDelete("oneway:bicycle", "no")
            ),
            cycleway(NONE, NONE).appliedTo(mapOf(
                "cycleway:both" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            ))
        )
    }

    @Test fun `apply value only for one side`() {
        assertEquals(
            setOf(StringMapEntryAdd("cycleway:left", "track")),
            cycleway(TRACK, null).appliedTo(mapOf())
        )
        assertEquals(
            setOf(StringMapEntryAdd("cycleway:right", "track")),
            cycleway(null, TRACK).appliedTo(mapOf())
        )
    }

    @Test fun `apply for one side does not touch the other side`() {
        assertEquals(
            setOf(StringMapEntryAdd("cycleway:left", "track")),
            cycleway(TRACK, null).appliedTo(mapOf("cycleway:right" to "no"))
        )
        assertEquals(
            setOf(StringMapEntryAdd("cycleway:right", "track")),
            cycleway(null, TRACK).appliedTo(mapOf("cycleway:left" to "no"))
        )
    }

    @Test fun `apply for one side does not touch the other side even if it is invalid`() {
        assertEquals(
            setOf(StringMapEntryAdd("cycleway:left", "track")),
            cycleway(TRACK, null).appliedTo(mapOf("cycleway:right" to "invalid"))
        )
        assertEquals(
            setOf(StringMapEntryAdd("cycleway:right", "track")),
            cycleway(null, TRACK).appliedTo(mapOf("cycleway:left" to "invalid"))
        )
    }

    @Test fun `apply for one side does not change values for the other side even if it was defined for both sides before and invalid`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:right", "invalid"),
                StringMapEntryDelete("cycleway:both", "invalid"),
            ),
            cycleway(TRACK, null).appliedTo(mapOf("cycleway:both" to "invalid"))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "invalid"),
                StringMapEntryAdd("cycleway:right", "track"),
                StringMapEntryDelete("cycleway:both", "invalid"),
            ),
            cycleway(null, TRACK).appliedTo(mapOf("cycleway:both" to "invalid"))
        )
    }

    @Test fun `apply conflates values`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "no"),
                StringMapEntryDelete("cycleway:left", "no"),
                StringMapEntryDelete("cycleway:right", "no"),
            ),
            cycleway(NONE, null).appliedTo(mapOf(
                "cycleway:left" to "no",
                "cycleway:right" to "no"
            ))
        )
    }

    @Test fun `apply updates oneway not for cyclists`() {
        // contra-flow side set to explicitly not oneway -> not oneway for cyclists
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "no"),
                StringMapEntryModify("oneway:bicycle", "no", "no"),
            ),
            cycleway(NONE_NO_ONEWAY, null).appliedTo(mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes"
            ))
        )
        // track on right side is dual-way -> not oneway for cyclists
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:right", "track"),
                StringMapEntryAdd("cycleway:right:oneway", "no"),
                StringMapEntryModify("oneway:bicycle", "no", "no"),
            ),
            cycleway(null, TRACK to BOTH).appliedTo(mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes"
            ))
        )
        // not a oneway at all
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "no"),
                StringMapEntryDelete("oneway:bicycle", "no"),
            ),
            cycleway(NONE, null).appliedTo(mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "no"
            ))
        )
        // contra-flow side is explicitly oneway and flow-side is not dual-way
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:both", "no"),
                StringMapEntryDelete("oneway:bicycle", "no"),
            ),
            cycleway(NONE, NONE).appliedTo(mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes"
            ))
        )
        // no oneway for cyclists is deleted on left side but dual track on right side still there
        // -> still not oneway for cyclists
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "no"),
                StringMapEntryAdd("cycleway:right", "track"),
                StringMapEntryDelete("cycleway:both", "track"),
                StringMapEntryModify("cycleway:right:oneway", "no", "no"),
                StringMapEntryModify("oneway:bicycle", "no", "no"),
            ),
            cycleway(NONE to FORWARD, TRACK to BOTH).appliedTo(mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes",
                "cycleway:both" to "track",
                "cycleway:right:oneway" to "no",
            ))
        )
        // no oneway for cyclists is deleted on left side -> oneway for cyclists now
        assertEquals(
            setOf(
                StringMapEntryModify("cycleway:left", "no", "no"),
                StringMapEntryDelete("oneway:bicycle", "no"),
                StringMapEntryModify("cycleway:right", "track", "track"),
            ),
            cycleway(NONE, TRACK).appliedTo(mapOf(
                "oneway:bicycle" to "no",
                "oneway" to "yes",
                "cycleway:left" to "no",
                "cycleway:right" to "track",
            ))
        )
    }

    @Test fun `apply no cycleway deletes cycleway direction`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "no"),
                StringMapEntryDelete("cycleway:left:oneway", "-1"),
            ),
            cycleway(NONE, null).appliedTo(mapOf(
                "cycleway:left:oneway" to "-1"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "no"),
                StringMapEntryDelete("cycleway:left:oneway", "-1"),
                StringMapEntryAdd("oneway:bicycle", "no"),
            ),
            cycleway(NONE_NO_ONEWAY, null).appliedTo(mapOf(
                "cycleway:left:oneway" to "-1",
                "oneway" to "yes"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "separate"),
                StringMapEntryDelete("cycleway:left:oneway", "-1"),
            ),
            cycleway(SEPARATE, null).appliedTo(mapOf(
                "cycleway:left:oneway" to "-1"
            )),
        )
    }

    @Test fun `apply cycleway with non-standard direction adds cycleway direction`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:left:oneway", "yes"),
            ),
            cycleway(TRACK to FORWARD, null).appliedTo(mapOf())
        )
    }

    @Test fun `apply cycleway in contraflow of oneway adds cycleway direction`() {
        assertEquals(
            setOf(
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:left:oneway", "-1"),
                StringMapEntryAdd("oneway:bicycle", "no"),
            ),
            cycleway(TRACK, null).appliedTo(mapOf("oneway" to "yes"))
        )
    }

    @Test fun `apply answer for one side in oneway when bare tag was set before`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("cycleway", "track"),
                StringMapEntryAdd("cycleway:right", "no")
            ),
            cycleway(null, NONE).appliedTo(mapOf("oneway" to "yes", "cycleway" to "track"))
        )
        assertEquals(
            setOf(
                StringMapEntryDelete("cycleway", "opposite"),
                StringMapEntryAdd("cycleway:right", "no"),
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("oneway:bicycle", "no")
            ),
            cycleway(TRACK, null).appliedTo(mapOf(
                "oneway" to "-1",
                "cycleway" to "opposite"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryDelete("cycleway", "opposite_track"),
                StringMapEntryDelete("cycleway:segregated", "yes"),
                StringMapEntryAdd("cycleway:left", "track"),
                StringMapEntryAdd("cycleway:left:segregated", "yes"),
                StringMapEntryAdd("cycleway:right", "no"),
                StringMapEntryAdd("oneway:bicycle", "no")
            ),
            cycleway(null, NONE).appliedTo(mapOf(
                "oneway" to "yes",
                "cycleway" to "opposite_track",
                "cycleway:segregated" to "yes"
            ))
        )
        assertEquals(
            setOf(
                StringMapEntryDelete("cycleway", "opposite_lane"),
                StringMapEntryDelete("cycleway:lane", "advisory"),
                StringMapEntryAdd("cycleway:left", "lane"),
                StringMapEntryAdd("cycleway:left:lane", "advisory"),
                StringMapEntryAdd("cycleway:right", "no"),
                StringMapEntryAdd("oneway:bicycle", "no")
            ),
            cycleway(null, NONE).appliedTo(mapOf(
                "oneway" to "yes",
                "cycleway" to "opposite_lane",
                "cycleway:lane" to "advisory"
            ))
        )
    }

    @Test fun `expanding bare tags does not overwrite non-bare tags`() {
        assertEquals(
            setOf(
                StringMapEntryDelete("cycleway", "track"),
                StringMapEntryAdd("cycleway:left", "no")
            ),
            cycleway(NONE, null).appliedTo(mapOf(
                "oneway" to "yes",
                "cycleway" to "track",
                "cycleway:right" to "blubber",
            ))
        )
    }

    @Test fun `applying invalid left throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            cycleway(INVALID, null).applyTo(StringMapChangesBuilder(mapOf()), false)
        }
    }

    @Test fun `applying invalid right throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            cycleway(null, INVALID).applyTo(StringMapChangesBuilder(mapOf()), false)
        }
    }
}

private fun LeftAndRightCycleway.appliedTo(tags: Map<String, String>, isLeftHandTraffic: Boolean = false): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyTo(cb, isLeftHandTraffic)
    return cb.create().changes
}

private fun cycleway(left: Pair<Cycleway, Direction>?, right: Pair<Cycleway, Direction>?) =
    LeftAndRightCycleway(
        left?.let { CyclewayAndDirection(it.first, it.second) },
        right?.let { CyclewayAndDirection(it.first, it.second) },
    )

private fun cycleway(left: Cycleway?, right: Cycleway?, isLeftHandTraffic: Boolean = false) =
    LeftAndRightCycleway(
        left?.let { CyclewayAndDirection(it, if (isLeftHandTraffic) FORWARD else BACKWARD) },
        right?.let { CyclewayAndDirection(it, if (isLeftHandTraffic) BACKWARD else FORWARD) },
    )
