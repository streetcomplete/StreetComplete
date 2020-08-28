package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*

import org.junit.Assert.*
import org.junit.Test

class CyclewayParserKtTest {
    /* These are a lot of tests because there are many possible permutations and this test does not
    *  make (much) assumptions that the code is written in a way that if it is solved for one type,
    *  it is solved for all */

    /* ------------------------------------------ cycleway -------------------------------------- */

    @Test fun track() {
        assertEquals(
            CyclewaySides(TRACK, TRACK),
            parse( "cycleway" to "track")
        )
    }

    @Test fun `explicitly on sidewalk`() {
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `dual track`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, LANE_UNSPECIFIED),
            parse( "cycleway" to "lane")
        )
    }

    @Test fun `unspecified dual lane`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive_lane",
                "cycleway:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "mandatory",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "dashed"
            )
        )
    }

    @Test fun pictograms() {
        assertEquals(
            CyclewaySides(PICTOGRAMS, PICTOGRAMS),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram"
            )
        )
    }

    @Test fun sidewalk() {
        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway" to "no",
                "sidewalk:bicycle" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway" to "none",
                "sidewalk:bicycle" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse("sidewalk:bicycle" to "yes")
        )
    }

    @Test fun none() {
        assertEquals(
            CyclewaySides(NONE, NONE),
            parse("cycleway" to "no")
        )
        assertEquals(
            CyclewaySides(NONE, NONE),
            parse( "cycleway" to "none")
        )
    }

    @Test fun busway() {
        assertEquals(
            CyclewaySides(BUSWAY, BUSWAY),
            parse( "cycleway" to "share_busway")
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parseForLeftHandTraffic(
                "cycleway" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, NONE),
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
            CyclewaySides(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway" to "opposite",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parseForLeftHandTraffic(
                "cycleway" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite track`() {
        assertEquals(
            CyclewaySides(TRACK, NONE),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite track (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, TRACK),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite track (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, TRACK),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite track (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(TRACK, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite dual track`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, NONE),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, DUAL_TRACK),
            parse(
                "cycleway" to "opposite_track",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, DUAL_TRACK),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_track",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite busway`() {
        assertEquals(
            CyclewaySides(BUSWAY, NONE),
            parse(
                "cycleway" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite busway (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, BUSWAY),
            parse(
                "cycleway" to "opposite_share_busway",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite busway (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, BUSWAY),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite busway (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(BUSWAY, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_share_busway",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, NONE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, LANE_UNSPECIFIED),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, LANE_UNSPECIFIED),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, NONE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, DUAL_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, DUAL_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, NONE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, ADVISORY_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, ADVISORY_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, NONE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, EXCLUSIVE_LANE),
            parse(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, EXCLUSIVE_LANE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive dual lane`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, NONE),
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
            CyclewaySides(NONE, DUAL_LANE),
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
            CyclewaySides(NONE, DUAL_LANE),
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
            CyclewaySides(DUAL_LANE, NONE),
            parseForLeftHandTraffic(
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    /* -------------------------------------- cycleway:left  ----------------------------------- */

    @Test fun `track left`() {
        assertEquals(
            CyclewaySides(TRACK, null),
            parse( "cycleway:left" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on left side`() {
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:left:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on left side`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:left:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on left side`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, null),
            parse( "cycleway:left" to "lane")
        )
    }

    @Test fun `unspecified dual lane on left side`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:oneway" to "no"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on left side`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms on left side`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane on left side`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:left:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on left side`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:left:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:left:oneway" to "no"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on left side`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms on left side`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane on left side`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms on left side`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "dashed"
            )
        )
    }

    @Test fun `pictograms on left side`() {
        assertEquals(
            CyclewaySides(PICTOGRAMS, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "pictogram"
            )
        )
    }

    @Test fun `sidewalk on left side`() {
        assertEquals(
            CyclewaySides(SIDEWALK_OK, null),
            parse(
                "cycleway:left" to "no",
                "sidewalk:left:bicycle" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(SIDEWALK_OK, null),
            parse(
                "cycleway:left" to "none",
                "sidewalk:left:bicycle" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(SIDEWALK_OK, null),
            parse("sidewalk:left:bicycle" to "yes")
        )
    }

    @Test fun `none on left side`() {
        assertEquals(
            CyclewaySides(NONE, null),
            parse("cycleway:left" to "no")
        )
        assertEquals(
            CyclewaySides(NONE, null),
            parse( "cycleway:left" to "none")
        )
    }

    @Test fun `busway on left side`() {
        assertEquals(
            CyclewaySides(BUSWAY, null),
            parse( "cycleway:left" to "share_busway")
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, null),
            parse(
                "cycleway:left" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, null),
            parse(
                "cycleway:left" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, null),
            parseForLeftHandTraffic(
                "cycleway:left" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, null),
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
            CyclewaySides(TRACK, null),
            parse(
                "cycleway:left" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk on left side opposite`() {
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:left:segregated" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:both:segregated" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track on left side opposite`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_TRACK, null),
            parse(
                "cycleway:left" to "opposite_track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane on left side opposite`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified dual lane on left side opposite`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane on left side opposite`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane synonyms on left side opposite`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "mandatory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane on left side opposite`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
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
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:left:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
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
            CyclewaySides(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane synonyms on left side opposite`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "advisory_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "soft_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(
                "cycleway:left" to "opposite_lane",
                "cycleway:left:lane" to "dashed",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `busway on left side opposite`() {
        assertEquals(
            CyclewaySides(BUSWAY, null),
            parse(
                "cycleway:left" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `don't parse cycleway left opposite`() {
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:left" to "opposite",
                "oneway" to "-1"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:left" to "opposite",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(null, null),
            parseForLeftHandTraffic(
                "cycleway:left" to "opposite",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parseForLeftHandTraffic(
                "cycleway:left" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    /* -------------------------------------- cycleway:right  ----------------------------------- */

    @Test fun `track right`() {
        assertEquals(
            CyclewaySides(null, TRACK),
            parse( "cycleway:right" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on right side`() {
        assertEquals(
            CyclewaySides(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "track",
                "cycleway:right:segregated" to "no"
            )
        )

        assertEquals(
            CyclewaySides(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "track",
                "cycleway:both:segregated" to "no"
            )
        )
        assertEquals(
            CyclewaySides(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on right side`() {
        assertEquals(
            CyclewaySides(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "track",
                "cycleway:right:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "track",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on right side`() {
        assertEquals(
            CyclewaySides(null, LANE_UNSPECIFIED),
            parse( "cycleway:right" to "lane")
        )
    }

    @Test fun `unspecified dual lane on right side`() {
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:oneway" to "no"
            )
        )

        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on right side`() {
        assertEquals(
            CyclewaySides(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms on right side`() {
        assertEquals(
            CyclewaySides(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane on right side`() {
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:right:oneway" to "no"
            )
        )

        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on right side`() {
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive_lane",
                "cycleway:right:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "mandatory",
                "cycleway:right:oneway" to "no"
            )
        )

        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "mandatory",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on right side`() {
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms on right side`() {
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane on right side`() {
        assertEquals(
            CyclewaySides(null, SUGGESTION_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms on right side`() {
        assertEquals(
            CyclewaySides(null, SUGGESTION_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, SUGGESTION_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, SUGGESTION_LANE),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "dashed"
            )
        )
    }

    @Test fun `pictograms on right side`() {
        assertEquals(
            CyclewaySides(null, PICTOGRAMS),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "pictogram"
            )
        )
    }

    @Test fun `sidewalk on right side`() {
        assertEquals(
            CyclewaySides(null, SIDEWALK_OK),
            parse(
                "cycleway:right" to "no",
                "sidewalk:right:bicycle" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, SIDEWALK_OK),
            parse(
                "cycleway:right" to "none",
                "sidewalk:right:bicycle" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(null, SIDEWALK_OK),
            parse("sidewalk:right:bicycle" to "yes")
        )
    }

    @Test fun `none on right side`() {
        assertEquals(
            CyclewaySides(null, NONE),
            parse("cycleway:right" to "no")
        )
        assertEquals(
            CyclewaySides(null, NONE),
            parse( "cycleway:right" to "none")
        )
    }

    @Test fun `busway on right side`() {
        assertEquals(
            CyclewaySides(null, BUSWAY),
            parse( "cycleway:right" to "share_busway")
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            CyclewaySides(null, NONE),
            parse(
                "cycleway:right" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            CyclewaySides(null, NONE_NO_ONEWAY),
            parse(
                "cycleway:right" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (right hand traffic)`() {
        assertEquals(
            CyclewaySides(null, NONE_NO_ONEWAY),
            parseForLeftHandTraffic(
                "cycleway:right" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (reversed + right hand traffic)`() {
        assertEquals(
            CyclewaySides(null, NONE),
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
            CyclewaySides(null, TRACK),
            parse(
                "cycleway:right" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk on right side opposite`() {
        assertEquals(
            CyclewaySides(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:right:segregated" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:both:segregated" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track on right side opposite`() {
        assertEquals(
            CyclewaySides(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_TRACK),
            parse(
                "cycleway:right" to "opposite_track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane on right side opposite`() {
        assertEquals(
            CyclewaySides(null, LANE_UNSPECIFIED),
            parse(
                "cycleway:right" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified dual lane on right side opposite`() {
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane on right side opposite`() {
        assertEquals(
            CyclewaySides(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane synonyms on right side opposite`() {
        assertEquals(
            CyclewaySides(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, EXCLUSIVE_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "mandatory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane on right side opposite`() {
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
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
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive_lane",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "mandatory",
                "cycleway:right:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
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
            CyclewaySides(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane synonyms on right side opposite`() {
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "advisory_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "soft_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(
                "cycleway:right" to "opposite_lane",
                "cycleway:right:lane" to "dashed",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `busway on right side opposite`() {
        assertEquals(
            CyclewaySides(null, BUSWAY),
            parse(
                "cycleway:right" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `don't parse cycleway right opposite`() {
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:right" to "opposite",
                "oneway" to "-1"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:right" to "opposite",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(null, null),
            parseForLeftHandTraffic(
                "cycleway:right" to "opposite",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parseForLeftHandTraffic(
                "cycleway:right" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    /* -------------------------------------- cycleway:both  ----------------------------------- */

    @Test fun `track on both sides`() {
        assertEquals(
            CyclewaySides(TRACK, TRACK),
            parse( "cycleway:both" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on both sides`() {
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "track",
                "cycleway:both:segregated" to "no"
            )
        )
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on both sides`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway:both" to "track",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway:both" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on both sides`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, LANE_UNSPECIFIED),
            parse( "cycleway:both" to "lane")
        )
    }

    @Test fun `unspecified dual lane on both sides`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on both sides`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms on both sides`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane on both sides`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on both sides`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "mandatory",
                "cycleway:both:oneway" to "no"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "exclusive_lane",
                "cycleway:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "mandatory",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on both sides`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms on both sides`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane on both sides`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms on both sides`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "dashed"
            )
        )
    }

    @Test fun `pictograms on both sides`() {
        assertEquals(
            CyclewaySides(PICTOGRAMS, PICTOGRAMS),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "pictogram"
            )
        )
    }

    @Test fun `sidewalk on both sides`() {
        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway:both" to "no",
                "sidewalk:both:bicycle" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway:both" to "none",
                "sidewalk:both:bicycle" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway:both" to "no",
                "sidewalk:bicycle" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse(
                "cycleway:both" to "none",
                "sidewalk:bicycle" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse("sidewalk:both:bicycle" to "yes")
        )
    }

    @Test fun `none on both sides`() {
        assertEquals(
            CyclewaySides(NONE, NONE),
            parse("cycleway:both" to "no")
        )
        assertEquals(
            CyclewaySides(NONE, NONE),
            parse( "cycleway:both" to "none")
        )
    }

    @Test fun `busway on both sides`() {
        assertEquals(
            CyclewaySides(BUSWAY, BUSWAY),
            parse( "cycleway:both" to "share_busway")
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, NONE),
            parse(
                "cycleway:both" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parse(
                "cycleway:both" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (right hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parseForLeftHandTraffic(
                "cycleway:both" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (reversed + right hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, NONE),
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
            CyclewaySides(TRACK, TRACK),
            parse(
                "cycleway:both" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `explicitly on sidewalk on both side opposite`() {
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:both:segregated" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:segregated" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `dual track on both side opposite`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_TRACK, DUAL_TRACK),
            parse(
                "cycleway:both" to "opposite_track",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified lane on both side opposite`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, LANE_UNSPECIFIED),
            parse(
                "cycleway:both" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `unspecified dual lane on both side opposite`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane on both side opposite`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive lane synonyms on both side opposite`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "mandatory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `exclusive dual lane on both side opposite`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
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
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive_lane",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "mandatory",
                "cycleway:both:oneway" to "no",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "exclusive_lane",
                "cycleway:oneway" to "no",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
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
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "advisory",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `advisory lane synonyms on both side opposite`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "advisory_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "soft_lane",
                "oneway" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(
                "cycleway:both" to "opposite_lane",
                "cycleway:both:lane" to "dashed",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `busway on both side opposite`() {
        assertEquals(
            CyclewaySides(BUSWAY, BUSWAY),
            parse(
                "cycleway:both" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `don't parse cycleway both opposite with oneway`() {
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:both" to "opposite",
                "oneway" to "yes"
            )
        )

        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:both" to "opposite",
                "oneway" to "-1"
            )
        )
    }

    /* -------------------------------- parse failures -------------------------------------------*/

    @Test fun `don't parse invalid cycle lane`() {
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway" to "lane",
                "cycleway:lane" to "something"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "something"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "something"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:both" to "lane",
                "cycleway:both:lane" to "something"
            )
        )
    }

    @Test fun `don't parse invalid shared lane`() {
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway" to "shared_lane",
                "cycleway:lane" to "something"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "something"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "something"
            )
        )
        assertEquals(
            CyclewaySides(null, null),
            parse(
                "cycleway:both" to "shared_lane",
                "cycleway:both:lane" to "something"
            )
        )
    }

    @Test fun `don't parse invalid cycleway`() {
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway" to "something")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:left" to "something")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:right" to "something")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:both" to "something")
        )
    }

    @Test fun `don't parse opposite-tagging on non oneways`() {
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway" to "opposite")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:left" to "opposite")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:right" to "opposite")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:both" to "opposite")
        )

        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway" to "opposite_lane")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:left" to "opposite_lane")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:right" to "opposite_lane")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:both" to "opposite_lane")
        )

        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway" to "opposite_track")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:left" to "opposite_track")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:right" to "opposite_track")
        )
        assertEquals(
            CyclewaySides(null, null),
            parse( "cycleway:both" to "opposite_track")
        )
    }
}


private fun parse(vararg pairs: Pair<String, String>) = createCyclewaySides(mapOf(*pairs), false)
private fun parseForLeftHandTraffic(vararg pairs: Pair<String, String>) = createCyclewaySides(mapOf(*pairs), true)
