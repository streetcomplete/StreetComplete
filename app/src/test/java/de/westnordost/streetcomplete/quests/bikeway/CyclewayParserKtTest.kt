package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*

import org.junit.Assert.*
import org.junit.Test

class CyclewayParserKtTest {
    /* These are a lot of tests because there are many possible permutations and this test does not
    *  make (much) assumptions that the code is written in a way that if it is solved for one type,
    *  it is solved for all */

    /* ------------------------------------------ cycleway -------------------------------------- */

    @Test fun invalid() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN, UNKNOWN),
            parse("cycleway" to "something")
        )
    }

    @Test fun `invalid in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, UNKNOWN),
            parse(
                "cycleway" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `invalid in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN, null),
            parse(
                "cycleway" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `invalid in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN, null),
            parseForLeftHandTraffic(
                "cycleway" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `invalid in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, UNKNOWN),
            parseForLeftHandTraffic(
                "cycleway" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `invalid cycle lane`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN_LANE, UNKNOWN_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "something"
            )
        )
    }

    @Test fun `invalid cycle lane in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, UNKNOWN_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `invalid cycle lane in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN_LANE, null),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `invalid cycle lane in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `invalid cycle lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, UNKNOWN_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `invalid shared lane`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN_SHARED_LANE, UNKNOWN_SHARED_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something"
            )
        )
    }

    @Test fun `invalid shared lane in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, UNKNOWN_SHARED_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `invalid shared lane in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN_SHARED_LANE, null),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `invalid shared lane in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN_SHARED_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `invalid shared lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, UNKNOWN_SHARED_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified shared lane`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_SHARED_LANE, UNSPECIFIED_SHARED_LANE),
            parse("cycleway" to "shared_lane")
        )
    }

    @Test fun `unspecified shared lane in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, UNSPECIFIED_SHARED_LANE),
            parse(
                "cycleway" to "shared_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified shared lane in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_SHARED_LANE, null),
            parse(
                "cycleway" to "shared_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified shared lane in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_SHARED_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified shared lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, UNSPECIFIED_SHARED_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun track() {
        assertEquals(
            LeftAndRightCycleway(TRACK, TRACK),
            parse( "cycleway" to "track")
        )
    }

    @Test fun `track in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, TRACK),
            parse(
                "cycleway" to "track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `track in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(TRACK, null),
            parse(
                "cycleway" to "track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `track in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(TRACK, null),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `track in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, TRACK),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `explicitly on sidewalk`() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `explicitly on sidewalk in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway" to "track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway" to "track",
                "cycleway:segregated" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `explicitly on sidewalk in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, null),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_EXPLICIT),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "cycleway:segregated" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `dual track`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `dual track in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parse(
                "cycleway" to "track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parse(
                "cycleway" to "track",
                "cycleway:oneway" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `dual track in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parseForLeftHandTraffic(
                "cycleway" to "track",
                "cycleway:oneway" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified lane`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_LANE, UNSPECIFIED_LANE),
            parse("cycleway" to "lane")
        )
    }

    @Test fun `unspecified lane in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, UNSPECIFIED_LANE),
            parse(
                "cycleway" to "lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_LANE, null),
            parse(
                "cycleway" to "lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified lane in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, UNSPECIFIED_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `unspecified dual lane`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `exclusive lane in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `exclusive lane synonyms`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
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
            LeftAndRightCycleway(DUAL_LANE, null),
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
            LeftAndRightCycleway(DUAL_LANE, null),
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
            LeftAndRightCycleway(null, DUAL_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive_lane",
                "cycleway:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "mandatory",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `advisory lane in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `advisory lane synonyms`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "soft_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane`() {
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `suggestion lane in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, null),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `suggestion lane in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `suggestion lane in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, SUGGESTION_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `suggestion lane synonyms`() {
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "soft_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "dashed"
            )
        )
    }

    @Test fun pictograms() {
        assertEquals(
            LeftAndRightCycleway(PICTOGRAMS, PICTOGRAMS),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram"
            )
        )
    }

    @Test fun `pictograms in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, PICTOGRAMS),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `pictograms in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(PICTOGRAMS, null),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `pictograms in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(PICTOGRAMS, null),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `pictograms in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, PICTOGRAMS),
            parseForLeftHandTraffic(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram",
                "oneway" to "-1"
            )
        )
    }


    @Test fun sidewalk() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway" to "no",
                "sidewalk:bicycle" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway" to "none",
                "sidewalk:bicycle" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, SIDEWALK_OK),
            parse("sidewalk:bicycle" to "yes")
        )
    }

    @Test fun none() {
        assertEquals(
            LeftAndRightCycleway(NONE, NONE),
            parse("cycleway" to "no")
        )
        assertEquals(
            LeftAndRightCycleway(NONE, NONE),
            parse( "cycleway" to "none")
        )
    }

    @Test fun busway() {
        assertEquals(
            LeftAndRightCycleway(BUSWAY, BUSWAY),
            parse( "cycleway" to "share_busway")
        )
    }

    @Test fun `busway in oneway`() {
        assertEquals(
            LeftAndRightCycleway(null, BUSWAY),
            parse( "cycleway" to "share_busway", "oneway" to "yes")
        )
    }

    @Test fun `busway in oneway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(BUSWAY, null),
            parse( "cycleway" to "share_busway", "oneway" to "-1")
        )
    }

    @Test fun `busway in oneway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(BUSWAY, null),
            parseForLeftHandTraffic( "cycleway" to "share_busway", "oneway" to "yes")
        )
    }

    @Test fun `busway in oneway (reversed, left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, BUSWAY),
            parseForLeftHandTraffic( "cycleway" to "share_busway", "oneway" to "-1")
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            LeftAndRightCycleway(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(NONE, NONE_NO_ONEWAY),
            parseForLeftHandTraffic(
                "cycleway" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(NONE_NO_ONEWAY, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    /* ------------------------------ cycleway opposite taggings -------------------------------- */

    @Test fun `cycleway opposite`() {
        assertEquals(
            LeftAndRightCycleway(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway" to "opposite",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(NONE, NONE_NO_ONEWAY),
            parseForLeftHandTraffic(
                "cycleway" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(NONE_NO_ONEWAY, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite track`() {
        assertEquals(
            LeftAndRightCycleway(TRACK, null),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite track (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(null, TRACK),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite track (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, TRACK),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite track (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(TRACK, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite dual track`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite busway`() {
        assertEquals(
            LeftAndRightCycleway(BUSWAY, null),
            parse(
                "cycleway" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite busway (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(null, BUSWAY),
            parse(
                "cycleway" to "opposite_share_busway",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite busway (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, BUSWAY),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite busway (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(BUSWAY, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_share_busway",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_LANE, null),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(null, UNSPECIFIED_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, UNSPECIFIED_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive dual lane`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
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
            LeftAndRightCycleway(null, DUAL_LANE),
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
            LeftAndRightCycleway(null, DUAL_LANE),
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
            LeftAndRightCycleway(DUAL_LANE, null),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    /* -------------------------------------- cycleway:left  ----------------------------------- */

    @Test fun `invalid on left side`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN, null),
            parse("cycleway:left" to "something")
        )
    }

    @Test fun `invalid cycle lane on left side`() {
        assertEquals(LeftAndRightCycleway(UNKNOWN_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "something"
            )
        )
    }

    @Test fun `invalid shared lane on left side`() {
        assertEquals(LeftAndRightCycleway(UNKNOWN_SHARED_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "something"
            )
        )
    }

    @Test fun `unspecified shared lane on left side`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_SHARED_LANE, null),
            parse("cycleway:left" to "shared_lane")
        )
    }

    @Test fun `track left`() {
        assertEquals(
            LeftAndRightCycleway(TRACK, null),
            parse( "cycleway:left" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on left side`() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:left:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on left side`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:left:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on left side`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_LANE, null),
            parse( "cycleway:left" to "lane")
        )
    }

    @Test fun `unspecified dual lane on left side`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:oneway" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on left side`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms on left side`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane on left side`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:left:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on left side`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:left:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:left:oneway" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on left side`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms on left side`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "advisory_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "soft_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane on left side`() {
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms on left side`() {
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "advisory_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "soft_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "dashed"
            )
        )
    }

    @Test fun `pictograms on left side`() {
        assertEquals(
            LeftAndRightCycleway(PICTOGRAMS, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "pictogram"
            )
        )
    }

    @Test fun `sidewalk on left side`() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, null),
            parse(
                "cycleway:left" to "no",
                "sidewalk:left:bicycle" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, null),
            parse(
                "cycleway:left" to "none",
                "sidewalk:left:bicycle" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, null),
            parse("sidewalk:left:bicycle" to "yes")
        )
    }

    @Test fun `none on left side`() {
        assertEquals(
            LeftAndRightCycleway(NONE, null),
            parse("cycleway:left" to "no")
        )
        assertEquals(
            LeftAndRightCycleway(NONE, null),
            parse( "cycleway:left" to "none")
        )
    }

    @Test fun `busway on left side`() {
        assertEquals(
            LeftAndRightCycleway(BUSWAY, null),
            parse( "cycleway:left" to "share_busway")
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            LeftAndRightCycleway(NONE_NO_ONEWAY, null),
            parse(
                "cycleway:left" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(NONE, null),
            parse(
                "cycleway:left" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(NONE, null),
            parseForLeftHandTraffic(
                "cycleway:left" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(NONE_NO_ONEWAY, null),
            parseForLeftHandTraffic(
                "cycleway:left" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    /* ------------------------------ cycleway:left opposite tagging --------------------------- */

    @Test fun `track left opposite`() {
        assertEquals(
            LeftAndRightCycleway(TRACK, null),
            parse(
                "cycleway:left" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:left:segregated" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:both:segregated" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified dual lane on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane synonyms on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "mandatory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane synonyms on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "advisory_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "soft_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "dashed",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `busway on left side opposite`() {
        assertEquals(
            LeftAndRightCycleway(BUSWAY, null),
            parse(
                "cycleway:left" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    /* -------------------------------------- cycleway:right  ----------------------------------- */

    @Test fun `invalid on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, UNKNOWN),
            parse("cycleway:right" to "something")
        )
    }

    @Test fun `invalid cycle lane on right side`() {
        assertEquals(LeftAndRightCycleway(null, UNKNOWN_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "something"
            )
        )
    }

    @Test fun `invalid shared lane on right side`() {
        assertEquals(LeftAndRightCycleway(null, UNKNOWN_SHARED_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "something"
            )
        )
    }

    @Test fun `unspecified shared lane on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, UNSPECIFIED_SHARED_LANE),
            parse("cycleway:right" to "shared_lane")
        )
    }

    @Test fun `track right`() {
        assertEquals(
            LeftAndRightCycleway(null, TRACK),
            parse( "cycleway:right" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "track",
                "cycleway:right:segregated" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "track",
                "cycleway:both:segregated" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "track",
                "cycleway:right:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "track",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, UNSPECIFIED_LANE),
            parse( "cycleway:right" to "lane")
        )
    }

    @Test fun `unspecified dual lane on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:oneway" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:right:oneway" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive_lane",
                "cycleway:right:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "mandatory",
                "cycleway:right:oneway" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "mandatory",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "advisory_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "soft_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, SUGGESTION_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, SUGGESTION_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "advisory_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, SUGGESTION_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "soft_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, SUGGESTION_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "dashed"
            )
        )
    }

    @Test fun `pictograms on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, PICTOGRAMS),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "pictogram"
            )
        )
    }

    @Test fun `sidewalk on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_OK),
            parse(
                "cycleway:right" to "no",
                "sidewalk:right:bicycle" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_OK),
            parse(
                "cycleway:right" to "none",
                "sidewalk:right:bicycle" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_OK),
            parse("sidewalk:right:bicycle" to "yes")
        )
    }

    @Test fun `none on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, NONE),
            parse("cycleway:right" to "no")
        )
        assertEquals(
            LeftAndRightCycleway(null, NONE),
            parse( "cycleway:right" to "none")
        )
    }

    @Test fun `busway on right side`() {
        assertEquals(
            LeftAndRightCycleway(null, BUSWAY),
            parse( "cycleway:right" to "share_busway")
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            LeftAndRightCycleway(null, NONE),
            parse(
                "cycleway:right" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(null, NONE_NO_ONEWAY),
            parse(
                "cycleway:right" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (right hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, NONE_NO_ONEWAY),
            parseForLeftHandTraffic(
                "cycleway:right" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (reversed + right hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(null, NONE),
            parseForLeftHandTraffic(
                "cycleway:right" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    /* ------------------------------ cycleway:right opposite tagging --------------------------- */

    @Test fun `track right opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, TRACK),
            parse(
                "cycleway:right" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:right:segregated" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:both:segregated" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, UNSPECIFIED_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified dual lane on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane synonyms on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "mandatory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive_lane",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "mandatory",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "mandatory",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane synonyms on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "advisory_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "soft_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "dashed",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `busway on right side opposite`() {
        assertEquals(
            LeftAndRightCycleway(null, BUSWAY),
            parse(
                "cycleway:right" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    /* -------------------------------------- cycleway:both  ----------------------------------- */

    @Test fun `invalid on both sides`() {
        assertEquals(
            LeftAndRightCycleway(UNKNOWN, UNKNOWN),
            parse("cycleway:both" to "something")
        )
    }

    @Test fun `invalid cycle lane on both sides`() {
        assertEquals(LeftAndRightCycleway(UNKNOWN_LANE, UNKNOWN_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "something"
            )
        )
    }

    @Test fun `invalid shared lane on both sides`() {
        assertEquals(LeftAndRightCycleway(UNKNOWN_SHARED_LANE, UNKNOWN_SHARED_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "something"
            )
        )
    }

    @Test fun `unspecified shared lane on both sides`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_SHARED_LANE, UNSPECIFIED_SHARED_LANE),
            parse("cycleway:both" to "shared_lane")
        )
    }

    @Test fun `track on both sides`() {
        assertEquals(
            LeftAndRightCycleway(TRACK, TRACK),
            parse( "cycleway:both" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on both sides`() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on both sides`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway:both" to "track",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway:both" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on both sides`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_LANE, UNSPECIFIED_LANE),
            parse( "cycleway:both" to "lane")
        )
    }

    @Test fun `unspecified dual lane on both sides`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on both sides`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms on both sides`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane on both sides`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on both sides`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "mandatory",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive_lane",
                "cycleway:oneway" to "no"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "mandatory",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on both sides`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms on both sides`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "advisory_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "soft_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane on both sides`() {
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms on both sides`() {
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "advisory_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "soft_lane"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "dashed"
            )
        )
    }

    @Test fun `pictograms on both sides`() {
        assertEquals(
            LeftAndRightCycleway(PICTOGRAMS, PICTOGRAMS),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram"
            )
        )
    }

    @Test fun `sidewalk on both sides`() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway:both" to "no",
                "sidewalk:both:bicycle" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway:both" to "none",
                "sidewalk:both:bicycle" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway:both" to "no",
                "sidewalk:bicycle" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway:both" to "none",
                "sidewalk:bicycle" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(SIDEWALK_OK, SIDEWALK_OK),
            parse("sidewalk:both:bicycle" to "yes")
        )
    }

    @Test fun `none on both sides`() {
        assertEquals(
            LeftAndRightCycleway(NONE, NONE),
            parse("cycleway:both" to "no")
        )
        assertEquals(
            LeftAndRightCycleway(NONE, NONE),
            parse( "cycleway:both" to "none")
        )
    }

    @Test fun `busway on both sides`() {
        assertEquals(
            LeftAndRightCycleway(BUSWAY, BUSWAY),
            parse( "cycleway:both" to "share_busway")
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            LeftAndRightCycleway(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway:both" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            LeftAndRightCycleway(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway:both" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (right hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(NONE, NONE_NO_ONEWAY),
            parseForLeftHandTraffic(
                "cycleway:both" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (reversed + right hand traffic)`() {
        assertEquals(
            LeftAndRightCycleway(NONE_NO_ONEWAY, NONE),
            parseForLeftHandTraffic(
                "cycleway:both" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    /* ------------------------------ cycleway:both opposite tagging --------------------------- */

    @Test fun `track both opposite`() {
        assertEquals(
            LeftAndRightCycleway(TRACK, TRACK),
            parse(
                "cycleway:both" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:both:segregated" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(UNSPECIFIED_LANE, UNSPECIFIED_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified dual lane on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane synonyms on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "mandatory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "mandatory",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "mandatory",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane synonyms on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "advisory_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "soft_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            LeftAndRightCycleway(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "dashed",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `busway on both side opposite`() {
        assertEquals(
            LeftAndRightCycleway(BUSWAY, BUSWAY),
            parse(
                "cycleway:both" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    /* -------------------------------- parse failures -------------------------------------------*/

    @Test fun `don't parse opposite-tagging on non oneways`() {
        assertNull(parse( "cycleway" to "opposite"))
        assertNull(parse( "cycleway:left" to "opposite"))
        assertNull(parse( "cycleway:right" to "opposite"))
        assertNull(parse( "cycleway:both" to "opposite"))

        assertNull(parse( "cycleway" to "opposite_lane"))
        assertNull(parse( "cycleway:left" to "opposite_lane"))
        assertNull(parse( "cycleway:right" to "opposite_lane"))
        assertNull(parse( "cycleway:both" to "opposite_lane"))

        assertNull(parse( "cycleway" to "opposite_track"))
        assertNull(parse( "cycleway:left" to "opposite_track"))
        assertNull(parse( "cycleway:right" to "opposite_track"))
        assertNull(parse( "cycleway:both" to "opposite_track"))
    }
}


private fun parse(vararg pairs: Pair<String, String>) =
    createCyclewaySides(mapOf(*pairs), false)

private fun parseForLeftHandTraffic(vararg pairs: Pair<String, String>) =
    createCyclewaySides(mapOf(*pairs), true)
