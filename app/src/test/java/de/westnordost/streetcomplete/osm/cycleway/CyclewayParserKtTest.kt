package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.cycleway.Direction.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CyclewayParserKtTest {
    /* These are a lot of tests because there are many possible permutations and this test does not
     * make (much) assumptions that the code is written in a way that if it is solved for one type,
     * it is solved for all */

    /* -------------------------------------- special cases ------------------------------------- */

    @Test fun `do not interpret non-oneway for bicycles as NONE_NO_ONEWAY if the cycleway on the other side is not a oneway`() {
        assertEquals(
            cycleway(NONE to BACKWARD, TRACK to BOTH),
            parse(
                "cycleway:right" to "track",
                "cycleway:right:oneway" to "no",
                "cycleway:left" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no",
            )
        )
        assertEquals(
            cycleway(TRACK to BOTH, NONE to BACKWARD),
            parseForLeftHandTraffic(
                "cycleway:left" to "track",
                "cycleway:left:oneway" to "no",
                "cycleway:right" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no",
            )
        )
    }

    @Test fun `fall back to bicycle=use_sidepath`() {
        assertEquals(
            cycleway(SEPARATE, SEPARATE),
            parse("bicycle" to "use_sidepath")
        )
    }

    @Test fun `do not fall back to bicycle=use_sidepath if any side is defined normally`() {
        assertEquals(
            cycleway(NONE, null),
            parse("bicycle" to "use_sidepath", "cycleway:left" to "no")
        )
        assertEquals(
            cycleway(null, TRACK),
            parse("bicycle" to "use_sidepath", "cycleway:right" to "track")
        )
    }

    @Test fun `fall back to bicycle=use_sidepath with forward or backward`() {
        assertEquals(
            cycleway(null, SEPARATE),
            parse("bicycle:forward" to "use_sidepath")
        )
        assertEquals(
            cycleway(SEPARATE, null),
            parse("bicycle:backward" to "use_sidepath")
        )
        assertEquals(
            cycleway(SEPARATE, null, true),
            parseForLeftHandTraffic("bicycle:forward" to "use_sidepath")
        )
        assertEquals(
            cycleway(null, SEPARATE, true),
            parseForLeftHandTraffic("bicycle:backward" to "use_sidepath")
        )
    }

    /* ----------------------------------------- direction -------------------------------------- */

    @Test fun `default directions`() {
        assertEquals(
            cycleway(NONE to BACKWARD, NONE to FORWARD),
            parse("cycleway:both" to "no")
        )
        assertEquals(
            cycleway(NONE to FORWARD, NONE to BACKWARD),
            parseForLeftHandTraffic("cycleway:both" to "no")
        )
    }

    @Test fun `fixed directions`() {
        assertEquals(
            cycleway(NONE to BACKWARD, NONE to BACKWARD),
            parse("cycleway:both" to "no", "cycleway:both:oneway" to "-1")
        )
        assertEquals(
            cycleway(NONE to FORWARD, NONE to FORWARD),
            parse("cycleway:both" to "no", "cycleway:both:oneway" to "yes")
        )
        assertEquals(
            cycleway(NONE to BOTH, NONE to BOTH),
            parse("cycleway:both" to "no", "cycleway:both:oneway" to "no")
        )
    }

    /* ------------------------------------------ cycleway -------------------------------------- */

    @Test fun invalid() {
        val invalid = cycleway(INVALID, INVALID)
        assertEquals(invalid, parse("cycleway" to "yes"))
        assertEquals(invalid, parse("cycleway" to "both"))
        assertEquals(invalid, parse("cycleway" to "left"))
        assertEquals(invalid, parse("cycleway" to "right"))
        assertEquals(invalid, parse("cycleway" to "shared"))
        assertEquals(invalid, parse("cycleway" to "none"))

        assertEquals(invalid, parse("cycleway" to "lane", "cycleway:lane" to "yes"))
        assertEquals(invalid, parse("cycleway" to "lane", "cycleway:lane" to "right"))
        assertEquals(invalid, parse("cycleway" to "lane", "cycleway:lane" to "left"))
        assertEquals(invalid, parse("cycleway" to "lane", "cycleway:lane" to "both"))
        assertEquals(invalid, parse("cycleway" to "lane", "cycleway:lane" to "shoulder"))
        assertEquals(invalid, parse("cycleway" to "lane", "cycleway:lane" to "soft_lane"))
        assertEquals(invalid, parse("cycleway" to "lane", "cycleway:lane" to "advisory_lane"))
        assertEquals(invalid, parse("cycleway" to "lane", "cycleway:lane" to "exclusive_lane"))
        assertEquals(invalid, parse("cycleway" to "lane", "cycleway:lane" to "mandatory"))
    }

    @Test fun unknown() {
        assertEquals(
            cycleway(UNKNOWN, UNKNOWN),
            parse("cycleway" to "something")
        )
    }

    @Test fun `unknown in oneway`() {
        assertEquals(
            cycleway(null, UNKNOWN),
            parse(
                "cycleway" to "something",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(null, UNKNOWN),
            parse(
                "cycleway" to "something",
                "junction" to "roundabout"
            )
        )
    }

    @Test fun `unknown in oneway (reversed)`() {
        assertEquals(
            cycleway(UNKNOWN, null),
            parse(
                "cycleway" to "something",
                "oneway" to "-1"
            )
        )
        assertEquals(
            cycleway(UNKNOWN, null),
            parse(
                "cycleway" to "something",
                "oneway" to "-1",
                "junction" to "roundabout"
            )
        )
    }

    @Test fun `unknown in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(UNKNOWN, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "something",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(UNKNOWN, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "something",
                "junction" to "roundabout"
            )
        )
    }

    @Test fun `unknown in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, UNKNOWN, true),
            parseForLeftHandTraffic(
                "cycleway" to "something",
                "oneway" to "-1"
            )
        )
        assertEquals(
            cycleway(null, UNKNOWN, true),
            parseForLeftHandTraffic(
                "cycleway" to "something",
                "oneway" to "-1",
                "junction" to "roundabout"
            )
        )
    }

    @Test fun `unknown cycle lane`() {
        assertEquals(
            cycleway(UNKNOWN_LANE, UNKNOWN_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "something"
            )
        )
    }

    @Test fun `unknown cycle lane in oneway`() {
        assertEquals(
            cycleway(null, UNKNOWN_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unknown cycle lane in oneway (reversed)`() {
        assertEquals(
            cycleway(UNKNOWN_LANE, null),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unknown cycle lane in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(UNKNOWN_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unknown cycle lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, UNKNOWN_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unknown shared lane`() {
        assertEquals(
            cycleway(UNKNOWN_SHARED_LANE, UNKNOWN_SHARED_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something"
            )
        )
    }

    @Test fun `unknown shared lane in oneway`() {
        assertEquals(
            cycleway(null, UNKNOWN_SHARED_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unknown shared lane in oneway (reversed)`() {
        assertEquals(
            cycleway(UNKNOWN_SHARED_LANE, null),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unknown shared lane in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(UNKNOWN_SHARED_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unknown shared lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, UNKNOWN_SHARED_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified shared lane`() {
        assertEquals(
            cycleway(UNSPECIFIED_SHARED_LANE, UNSPECIFIED_SHARED_LANE),
            parse("cycleway" to "shared_lane")
        )
    }

    @Test fun `unspecified shared lane in oneway`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_SHARED_LANE),
            parse(
                "cycleway" to "shared_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified shared lane in oneway (reversed)`() {
        assertEquals(
            cycleway(UNSPECIFIED_SHARED_LANE, null),
            parse(
                "cycleway" to "shared_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified shared lane in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(UNSPECIFIED_SHARED_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified shared lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_SHARED_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun track() {
        assertEquals(
            cycleway(TRACK, TRACK),
            parse("cycleway" to "track")
        )
    }

    @Test fun `track in oneway`() {
        assertEquals(
            cycleway(null, TRACK),
            parse(
                "cycleway" to "track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `track in oneway (reversed)`() {
        assertEquals(
            cycleway(TRACK, null),
            parse(
                "cycleway" to "track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `track in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(TRACK, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `track in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, TRACK, true),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `explicitly on sidewalk`() {
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `explicitly on sidewalk in oneway`() {
        assertEquals(
            cycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway" to "track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk in oneway (reversed)`() {
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway" to "track",
                "cycleway:segregated" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `explicitly on sidewalk in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, SIDEWALK_EXPLICIT, true),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "cycleway:segregated" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `dual track`() {
        assertEquals(
            cycleway(TRACK to BOTH, TRACK to BOTH),
            parse(
                "cycleway" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `dual track in oneway`() {
        assertEquals(
            cycleway(null, TRACK to BOTH),
            parse(
                "cycleway" to "track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track in oneway (reversed)`() {
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parse(
                "cycleway" to "track",
                "cycleway:oneway" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `dual track in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, TRACK to BOTH),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "cycleway:oneway" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified lane`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE, UNSPECIFIED_LANE),
            parse("cycleway" to "lane")
        )
    }

    @Test fun `unspecified lane in oneway`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE),
            parse(
                "cycleway" to "lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane in oneway (reversed)`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE, null),
            parse(
                "cycleway" to "lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified lane in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified dual lane`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane in oneway`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane in oneway (reversed)`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `exclusive lane in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `exclusive dual lane`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane in oneway`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane in oneway (reversed)`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `exclusive dual lane in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `advisory lane`() {
        assertEquals(
            cycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane in oneway`() {
        assertEquals(
            cycleway(null, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane in oneway (reversed)`() {
        assertEquals(
            cycleway(ADVISORY_LANE, null),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `advisory lane in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(ADVISORY_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, ADVISORY_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `suggestion lane`() {
        assertEquals(
            cycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane in oneway`() {
        assertEquals(
            cycleway(null, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `suggestion lane in oneway (reversed)`() {
        assertEquals(
            cycleway(SUGGESTION_LANE, null),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `suggestion lane in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(SUGGESTION_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `suggestion lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, SUGGESTION_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory",
                "oneway" to "-1"
            )
        )
    }

    @Test fun pictograms() {
        assertEquals(
            cycleway(PICTOGRAMS, PICTOGRAMS),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram"
            )
        )
    }

    @Test fun `pictograms in oneway`() {
        assertEquals(
            cycleway(null, PICTOGRAMS),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `pictograms in oneway (reversed)`() {
        assertEquals(
            cycleway(PICTOGRAMS, null),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `pictograms in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(PICTOGRAMS, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `pictograms in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, PICTOGRAMS, true),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "oneway" to "-1"
            )
        )
    }

    @Test fun none() {
        assertEquals(
            cycleway(NONE, NONE),
            parse("cycleway" to "no")
        )
    }

    @Test fun separate() {
        assertEquals(
            cycleway(SEPARATE, SEPARATE),
            parse("cycleway" to "separate")
        )
    }

    @Test fun busway() {
        assertEquals(
            cycleway(BUSWAY, BUSWAY),
            parse("cycleway" to "share_busway")
        )
    }

    @Test fun `busway in oneway`() {
        assertEquals(
            cycleway(null, BUSWAY),
            parse("cycleway" to "share_busway", "oneway" to "yes")
        )
    }

    @Test fun `busway in oneway (reversed)`() {
        assertEquals(
            cycleway(BUSWAY, null),
            parse("cycleway" to "share_busway", "oneway" to "-1")
        )
    }

    @Test fun `busway in oneway (left hand traffic)`() {
        assertEquals(
            cycleway(BUSWAY, null, true),
            parseForLeftHandTraffic("cycleway" to "share_busway", "oneway" to "yes")
        )
    }

    @Test fun `busway in oneway (reversed, left hand traffic)`() {
        assertEquals(
            cycleway(null, BUSWAY, true),
            parseForLeftHandTraffic("cycleway" to "share_busway", "oneway" to "-1")
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            cycleway(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            cycleway(NONE, NONE_NO_ONEWAY, true),
            parseForLeftHandTraffic(
                "cycleway" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, NONE, true),
            parseForLeftHandTraffic(
                "cycleway" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun shoulder() {
        assertEquals(
            cycleway(SHOULDER, SHOULDER),
            parse("cycleway" to "shoulder")
        )
    }

    @Test fun `shoulder in oneway`() {
        assertEquals(
            cycleway(null, SHOULDER),
            parse("cycleway" to "shoulder", "oneway" to "yes")
        )
    }

    /* ------------------------------ cycleway opposite taggings -------------------------------- */

    @Test fun `cycleway opposite`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite (reversed)`() {
        assertEquals(
            cycleway(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway" to "opposite",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite (left hand traffic)`() {
        assertEquals(
            cycleway(NONE, NONE_NO_ONEWAY, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, NONE, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite track`() {
        assertEquals(
            cycleway(TRACK, null),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite track (reversed)`() {
        assertEquals(
            cycleway(null, TRACK),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite track (left hand traffic)`() {
        assertEquals(
            cycleway(null, TRACK, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite track (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(TRACK, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite dual track`() {
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (reversed)`() {
        assertEquals(
            cycleway(null, TRACK to BOTH),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (left hand traffic)`() {
        assertEquals(
            cycleway(null, TRACK to BOTH),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite busway`() {
        assertEquals(
            cycleway(BUSWAY, null),
            parse(
                "cycleway" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite busway (reversed)`() {
        assertEquals(
            cycleway(null, BUSWAY),
            parse(
                "cycleway" to "opposite_share_busway",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite busway (left hand traffic)`() {
        assertEquals(
            cycleway(null, BUSWAY, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite busway (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(BUSWAY, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_share_busway",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE, null),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (reversed)`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (left hand traffic)`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, null),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (reversed)`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (left hand traffic)`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE to BOTH),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane`() {
        assertEquals(
            cycleway(ADVISORY_LANE, null),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (reversed)`() {
        assertEquals(
            cycleway(null, ADVISORY_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (left hand traffic)`() {
        assertEquals(
            cycleway(null, ADVISORY_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(ADVISORY_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (reversed)`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (left hand traffic)`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE, null, true),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive dual lane`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite exclusive dual lane (reversed)`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite exclusive dual lane (left hand traffic)`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite exclusive dual lane (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    /* -------------------------------------- cycleway:left  ----------------------------------- */

    @Test fun `unknown on left side`() {
        assertEquals(
            cycleway(UNKNOWN, null),
            parse("cycleway:left" to "something")
        )
    }

    @Test fun `unknown cycle lane on left side`() {
        assertEquals(
            cycleway(UNKNOWN_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "something"
            )
        )
    }

    @Test fun `unknown shared lane on left side`() {
        assertEquals(
            cycleway(UNKNOWN_SHARED_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "something"
            )
        )
    }

    @Test fun `unspecified shared lane on left side`() {
        assertEquals(
            cycleway(UNSPECIFIED_SHARED_LANE, null),
            parse("cycleway:left" to "shared_lane")
        )
    }

    @Test fun `track left`() {
        assertEquals(
            cycleway(TRACK, null),
            parse("cycleway:left" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on left side`() {
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:left:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on left side`() {
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:left:oneway" to "no"
            )
        )
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on left side`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE, null),
            parse("cycleway:left" to "lane")
        )
    }

    @Test fun `unspecified dual lane on left side`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:oneway" to "no"
            )
        )

        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on left side`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive dual lane on left side`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:left:oneway" to "no"
            )
        )
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on left side`() {
        assertEquals(
            cycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane on left side`() {
        assertEquals(
            cycleway(SUGGESTION_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "advisory"
            )
        )
    }

    @Test fun `pictograms on left side`() {
        assertEquals(
            cycleway(PICTOGRAMS, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "pictogram"
            )
        )
    }

    @Test fun `none on left side`() {
        assertEquals(
            cycleway(NONE, null),
            parse("cycleway:left" to "no")
        )
    }

    @Test fun `separate on left side`() {
        assertEquals(
            cycleway(SEPARATE, null),
            parse("cycleway:left" to "separate")
        )
    }

    @Test fun `busway on left side`() {
        assertEquals(
            cycleway(BUSWAY, null),
            parse("cycleway:left" to "share_busway")
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, null),
            parse(
                "cycleway:left" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            cycleway(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway:left" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            cycleway(NONE, NONE_NO_ONEWAY, true),
            parseForLeftHandTraffic(
                "cycleway:left" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, null, true),
            parseForLeftHandTraffic(
                "cycleway:left" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `shoulder on left side`() {
        assertEquals(
            cycleway(SHOULDER, null),
            parse("cycleway:left" to "shoulder")
        )
    }

    /* ------------------------------ cycleway:left opposite tagging --------------------------- */

    @Test fun `left opposite`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, null),
            parse(
                "cycleway:left" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `left opposite (left hand traffic)`() {
        assertEquals(
            cycleway(NONE, null, true),
            parseForLeftHandTraffic(
                "cycleway:left" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `track left opposite`() {
        assertEquals(
            cycleway(TRACK, null),
            parse(
                "cycleway:left" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk on left side opposite`() {
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:left:segregated" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:both:segregated" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track on left side opposite`() {
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(TRACK to BOTH, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane on left side opposite`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified dual lane on left side opposite`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane on left side opposite`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane on left side opposite`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane on left side opposite`() {
        assertEquals(
            cycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `busway on left side opposite`() {
        assertEquals(
            cycleway(BUSWAY, null),
            parse(
                "cycleway:left" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    /* -------------------------------------- cycleway:right  ----------------------------------- */

    @Test fun `unknown on right side`() {
        assertEquals(
            cycleway(null, UNKNOWN),
            parse("cycleway:right" to "something")
        )
    }

    @Test fun `unknown cycle lane on right side`() {
        assertEquals(
            cycleway(null, UNKNOWN_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "something"
            )
        )
    }

    @Test fun `unknown shared lane on right side`() {
        assertEquals(
            cycleway(null, UNKNOWN_SHARED_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "something"
            )
        )
    }

    @Test fun `unspecified shared lane on right side`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_SHARED_LANE),
            parse("cycleway:right" to "shared_lane")
        )
    }

    @Test fun `track right`() {
        assertEquals(
            cycleway(null, TRACK),
            parse("cycleway:right" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on right side`() {
        assertEquals(
            cycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "track",
                "cycleway:right:segregated" to "no"
            )
        )

        assertEquals(
            cycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "track",
                "cycleway:both:segregated" to "no"
            )
        )
        assertEquals(
            cycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on right side`() {
        assertEquals(
            cycleway(null, TRACK to BOTH),
            parse(
                "cycleway:right" to "track",
                "cycleway:right:oneway" to "no"
            )
        )
        assertEquals(
            cycleway(null, TRACK to BOTH),
            parse(
                "cycleway:right" to "track",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            cycleway(null, TRACK to BOTH),
            parse(
                "cycleway:right" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on right side`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE),
            parse("cycleway:right" to "lane")
        )
    }

    @Test fun `unspecified dual lane on right side`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:oneway" to "no"
            )
        )

        assertEquals(
            cycleway(null, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:right" to "lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:right" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on right side`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive dual lane on right side`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:right:oneway" to "no"
            )
        )

        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on right side`() {
        assertEquals(
            cycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane on right side`() {
        assertEquals(
            cycleway(null, SUGGESTION_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "advisory"
            )
        )
    }

    @Test fun `pictograms on right side`() {
        assertEquals(
            cycleway(null, PICTOGRAMS),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "pictogram"
            )
        )
    }

    @Test fun `none on right side`() {
        assertEquals(
            cycleway(null, NONE),
            parse("cycleway:right" to "no")
        )
    }

    @Test fun `separate on right side`() {
        assertEquals(
            cycleway(null, SEPARATE),
            parse("cycleway:right" to "separate")
        )
    }

    @Test fun `busway on right side`() {
        assertEquals(
            cycleway(null, BUSWAY),
            parse("cycleway:right" to "share_busway")
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway:right" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            cycleway(null, NONE_NO_ONEWAY),
            parse(
                "cycleway:right" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            cycleway(null, NONE_NO_ONEWAY, true),
            parseForLeftHandTraffic(
                "cycleway:right" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, NONE, true),
            parseForLeftHandTraffic(
                "cycleway:right" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `track on left side that is not in contraflow direction`() {
        assertEquals(
            cycleway(TRACK to FORWARD, NONE to FORWARD),
            parse(
                "cycleway:right" to "no",
                "cycleway:left" to "track",
                "cycleway:left:oneway" to "yes"
            )
        )
    }

    @Test fun `track on left side for left-hand-traffic that is not in flow direction`() {
        assertEquals(
            cycleway(TRACK to BACKWARD, NONE to BACKWARD),
            parseForLeftHandTraffic(
                "cycleway:right" to "no",
                "cycleway:left" to "track",
                "cycleway:left:oneway" to "-1"
            )
        )
    }

    @Test fun `track on right side that is not in flow direction`() {
        assertEquals(
            cycleway(NONE to BACKWARD, TRACK to BACKWARD),
            parse(
                "cycleway:left" to "no",
                "cycleway:right" to "track",
                "cycleway:right:oneway" to "-1"
            )
        )
    }

    @Test fun `track on right side for left-hand-traffic that is not in contraflow direction`() {
        assertEquals(
            cycleway(NONE to FORWARD, TRACK to FORWARD),
            parseForLeftHandTraffic(
                "cycleway:left" to "no",
                "cycleway:right" to "track",
                "cycleway:right:oneway" to "yes"
            )
        )
    }

    @Test fun `shoulder on right side`() {
        assertEquals(
            cycleway(null, SHOULDER),
            parse("cycleway:right" to "shoulder")
        )
    }

    /* ------------------------------ cycleway:right opposite tagging --------------------------- */

    @Test fun `right opposite`() {
        assertEquals(
            cycleway(null, NONE),
            parse(
                "cycleway:right" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `right opposite (left hand traffic)`() {
        assertEquals(
            cycleway(null, NONE_NO_ONEWAY, true),
            parseForLeftHandTraffic(
                "cycleway:right" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `track right opposite`() {
        assertEquals(
            cycleway(null, TRACK),
            parse(
                "cycleway:right" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk on right side opposite`() {
        assertEquals(
            cycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:right:segregated" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            cycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:both:segregated" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track on right side opposite`() {
        assertEquals(
            cycleway(null, TRACK to BOTH),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            cycleway(null, TRACK to BOTH),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(null, TRACK to BOTH),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane on right side opposite`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified dual lane on right side opposite`() {
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            cycleway(null, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(null, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane on right side opposite`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane on right side opposite`() {
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(null, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane on right side opposite`() {
        assertEquals(
            cycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `busway on right side opposite`() {
        assertEquals(
            cycleway(null, BUSWAY),
            parse(
                "cycleway:right" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    /* -------------------------------------- cycleway:both  ----------------------------------- */

    @Test fun `unknown on both sides`() {
        assertEquals(
            cycleway(UNKNOWN, UNKNOWN),
            parse("cycleway:both" to "something")
        )
    }

    @Test fun `unknown cycle lane on both sides`() {
        assertEquals(
            cycleway(UNKNOWN_LANE, UNKNOWN_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "something"
            )
        )
    }

    @Test fun `unknown shared lane on both sides`() {
        assertEquals(
            cycleway(UNKNOWN_SHARED_LANE, UNKNOWN_SHARED_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "something"
            )
        )
    }

    @Test fun `unspecified shared lane on both sides`() {
        assertEquals(
            cycleway(UNSPECIFIED_SHARED_LANE, UNSPECIFIED_SHARED_LANE),
            parse("cycleway:both" to "shared_lane")
        )
    }

    @Test fun `track on both sides`() {
        assertEquals(
            cycleway(TRACK, TRACK),
            parse("cycleway:both" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on both sides`() {
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "no"
            )
        )
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on both sides`() {
        assertEquals(
            cycleway(TRACK to BOTH, TRACK to BOTH),
            parse(
                "cycleway:both" to "track",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            cycleway(TRACK to BOTH, TRACK to BOTH),
            parse(
                "cycleway:both" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on both sides`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE, UNSPECIFIED_LANE),
            parse("cycleway:both" to "lane")
        )
    }

    @Test fun `unspecified dual lane on both sides`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:both" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on both sides`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive dual lane on both sides`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on both sides`() {
        assertEquals(
            cycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane on both sides`() {
        assertEquals(
            cycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "advisory"
            )
        )
    }

    @Test fun `pictograms on both sides`() {
        assertEquals(
            cycleway(PICTOGRAMS, PICTOGRAMS),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram"
            )
        )
    }

    @Test fun `none on both sides`() {
        assertEquals(
            cycleway(NONE, NONE),
            parse("cycleway:both" to "no")
        )
    }

    @Test fun `separate on both sides`() {
        assertEquals(
            cycleway(SEPARATE, SEPARATE),
            parse("cycleway:both" to "separate")
        )
    }

    @Test fun `busway on both sides`() {
        assertEquals(
            cycleway(BUSWAY, BUSWAY),
            parse("cycleway:both" to "share_busway")
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway:both" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            cycleway(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway:both" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            cycleway(NONE, NONE_NO_ONEWAY, true),
            parseForLeftHandTraffic(
                "cycleway:both" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            cycleway(NONE_NO_ONEWAY, NONE, true),
            parseForLeftHandTraffic(
                "cycleway:both" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `shoulder on both sides`() {
        assertEquals(
            cycleway(SHOULDER, SHOULDER),
            parse("cycleway:both" to "shoulder")
        )
    }

    /* ------------------------------ cycleway:both opposite tagging --------------------------- */

    @Test fun `track both opposite`() {
        assertEquals(
            cycleway(TRACK, TRACK),
            parse(
                "cycleway:both" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk on both side opposite`() {
        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:both:segregated" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            cycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track on both side opposite`() {
        assertEquals(
            cycleway(TRACK to BOTH, TRACK to BOTH),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            cycleway(TRACK to BOTH, TRACK to BOTH),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane on both side opposite`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE, UNSPECIFIED_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified dual lane on both side opposite`() {
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(UNSPECIFIED_LANE to BOTH, UNSPECIFIED_LANE to BOTH),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane on both side opposite`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane on both side opposite`() {
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            cycleway(EXCLUSIVE_LANE to BOTH, EXCLUSIVE_LANE to BOTH),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane on both side opposite`() {
        assertEquals(
            cycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `busway on both side opposite`() {
        assertEquals(
            cycleway(BUSWAY, BUSWAY),
            parse(
                "cycleway:both" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    /* -------------------------------- parse failures -------------------------------------------*/

    @Test fun `don't parse opposite-tagging on non oneways`() {
        assertNull(parse("cycleway" to "opposite"))
        assertNull(parse("cycleway:left" to "opposite"))
        assertNull(parse("cycleway:right" to "opposite"))
        assertNull(parse("cycleway:both" to "opposite"))

        assertNull(parse("cycleway" to "opposite_lane"))
        assertNull(parse("cycleway:left" to "opposite_lane"))
        assertNull(parse("cycleway:right" to "opposite_lane"))
        assertNull(parse("cycleway:both" to "opposite_lane"))

        assertNull(parse("cycleway" to "opposite_track"))
        assertNull(parse("cycleway:left" to "opposite_track"))
        assertNull(parse("cycleway:right" to "opposite_track"))
        assertNull(parse("cycleway:both" to "opposite_track"))
    }
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

private fun parse(vararg pairs: Pair<String, String>) =
    createCyclewaySides(mapOf(*pairs), false)

private fun parseForLeftHandTraffic(vararg pairs: Pair<String, String>) =
    createCyclewaySides(mapOf(*pairs), true)
