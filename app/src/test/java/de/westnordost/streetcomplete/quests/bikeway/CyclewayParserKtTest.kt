package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*

import org.junit.Assert.*
import org.junit.Test

class CyclewayParserKtTest {
    /* These are a lot of tests because there are many possible permutations and this test does not
    *  make (much) assumptions that the code is written in a way that if it is solved for one type,
    *  it is solved for all */

    /* ------------------------------------------ cycleway -------------------------------------- */

    @Test fun `track on both sides`() {
        assertEquals(
            CyclewaySides(TRACK, TRACK),
            parse(false, "cycleway" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on both sides`() {
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, SIDEWALK_EXPLICIT),
            parse(false,
                "cycleway" to "track",
                "cycleway:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on both sides`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, DUAL_TRACK),
            parse(false,
                "cycleway" to "track",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on both sides`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, LANE_UNSPECIFIED),
            parse(false, "cycleway" to "lane")
        )
    }

    @Test fun `unspecified dual lane on both sides`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on both sides`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms on both sides`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, EXCLUSIVE_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane on both sides`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on both sides`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "exclusive_lane",
                "cycleway:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, DUAL_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "mandatory",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on both sides`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms on both sides`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, ADVISORY_LANE),
            parse(false,
                "cycleway" to "lane",
                "cycleway:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane on both sides`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(false,
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms on both sides`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(false,
                "cycleway" to "shared_lane",
                "cycleway:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(false,
                "cycleway" to "shared_lane",
                "cycleway:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, SUGGESTION_LANE),
            parse(false,
                "cycleway" to "shared_lane",
                "cycleway:lane" to "dashed"
            )
        )
    }

    @Test fun `pictograms on both sides`() {
        assertEquals(
            CyclewaySides(PICTOGRAMS, PICTOGRAMS),
            parse(false,
                "cycleway" to "shared_lane",
                "cycleway:lane" to "pictogram"
            )
        )
    }

    @Test fun `sidewalk on both sides`() {
        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse(false,
                "cycleway" to "no",
                "sidewalk:bicycle" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(SIDEWALK_OK, SIDEWALK_OK),
            parse(false,
                "cycleway" to "none",
                "sidewalk:bicycle" to "yes"
            )
        )
    }

    @Test fun `none on both sides`() {
        assertEquals(
            CyclewaySides(NONE, NONE),
            parse(false,"cycleway" to "no")
        )
        assertEquals(
            CyclewaySides(NONE, NONE),
            parse(false, "cycleway" to "none")
        )
    }

    @Test fun `busway on both sides`() {
        assertEquals(
            CyclewaySides(BUSWAY, BUSWAY),
            parse(false, "cycleway" to "share_busway")
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, NONE),
            parse(false,
                "cycleway" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parse(false,
                "cycleway" to "no",
                "oneway" to "-1",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parse(true,
                "cycleway" to "no",
                "oneway" to "yes",
                "oneway:bicycle" to "no"
            )
        )
    }

    @Test fun `none on both sides but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, NONE),
            parse(true,
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
            parse(false,
                "cycleway" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parse(false,
                "cycleway" to "opposite",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, NONE_NO_ONEWAY),
            parse(true,
                "cycleway" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, NONE),
            parse(true,
                "cycleway" to "opposite",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite track`() {
        assertEquals(
            CyclewaySides(TRACK, NONE),
            parse(false,
                "cycleway" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite track (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, TRACK),
            parse(false,
                "cycleway" to "opposite_track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite track (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, TRACK),
            parse(true,
                "cycleway" to "opposite_track",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite track (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(TRACK, NONE),
            parse(true,
                "cycleway" to "opposite_track",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite dual track`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, NONE),
            parse(false,
                "cycleway" to "opposite_track",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, DUAL_TRACK),
            parse(false,
                "cycleway" to "opposite_track",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, DUAL_TRACK),
            parse(true,
                "cycleway" to "opposite_track",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite dual track (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, NONE),
            parse(true,
                "cycleway" to "opposite_track",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite busway`() {
        assertEquals(
            CyclewaySides(BUSWAY, NONE),
            parse(false,
                "cycleway" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite busway (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, BUSWAY),
            parse(false,
                "cycleway" to "opposite_share_busway",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite busway (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, BUSWAY),
            parse(true,
                "cycleway" to "opposite_share_busway",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite busway (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(BUSWAY, NONE),
            parse(true,
                "cycleway" to "opposite_share_busway",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, NONE),
            parse(false,
                "cycleway" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, LANE_UNSPECIFIED),
            parse(false,
                "cycleway" to "opposite_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, LANE_UNSPECIFIED),
            parse(true,
                "cycleway" to "opposite_lane",
                "oneway" to "yes"
            )
        )
    }

    @Test fun `cycleway opposite unspecified lane (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, NONE),
            parse(true,
                "cycleway" to "opposite_lane",
                "oneway" to "-1"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, NONE),
            parse(false,
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, DUAL_LANE),
            parse(false,
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, DUAL_LANE),
            parse(true,
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite unspecified dual lane (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, NONE),
            parse(true,
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:oneway" to "no"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, NONE),
            parse(false,
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, ADVISORY_LANE),
            parse(false,
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, ADVISORY_LANE),
            parse(true,
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite advisory lane (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, NONE),
            parse(true,
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "advisory"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, NONE),
            parse(false,
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, EXCLUSIVE_LANE),
            parse(false,
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, EXCLUSIVE_LANE),
            parse(true,
                "cycleway" to "opposite_lane",
                "oneway" to "yes",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive lane (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, NONE),
            parse(true,
                "cycleway" to "opposite_lane",
                "oneway" to "-1",
                "cycleway:lane" to "exclusive"
            )
        )
    }

    @Test fun `cycleway opposite exclusive dual lane`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, NONE),
            parse(false,
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
            parse(false,
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
            parse(true,
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
            parse(true,
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
            parse(false, "cycleway:left" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on left side`() {
        assertEquals(
            CyclewaySides(SIDEWALK_EXPLICIT, null),
            parse(false,
                "cycleway:left" to "track",
                "cycleway:left:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on left side`() {
        assertEquals(
            CyclewaySides(DUAL_TRACK, null),
            parse(false,
                "cycleway:left" to "track",
                "cycleway:left:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on left side`() {
        assertEquals(
            CyclewaySides(LANE_UNSPECIFIED, null),
            parse(false, "cycleway:left" to "lane")
        )
    }

    @Test fun `unspecified dual lane on left side`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on left side`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms on left side`() {
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            CyclewaySides(EXCLUSIVE_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane on left side`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive",
                "cycleway:left:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on left side`() {
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "exclusive_lane",
                "cycleway:left:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(DUAL_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "mandatory",
                "cycleway:left:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on left side`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms on left side`() {
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(ADVISORY_LANE, null),
            parse(false,
                "cycleway:left" to "lane",
                "cycleway:left:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane on left side`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, null),
            parse(false,
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms on left side`() {
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, null),
            parse(false,
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, null),
            parse(false,
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(SUGGESTION_LANE, null),
            parse(false,
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "dashed"
            )
        )
    }

    @Test fun `pictograms on left side`() {
        assertEquals(
            CyclewaySides(PICTOGRAMS, null),
            parse(false,
                "cycleway:left" to "shared_lane",
                "cycleway:left:lane" to "pictogram"
            )
        )
    }

    @Test fun `sidewalk on left side`() {
        assertEquals(
            CyclewaySides(SIDEWALK_OK, null),
            parse(false,
                "cycleway:left" to "no",
                "sidewalk:left:bicycle" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(SIDEWALK_OK, null),
            parse(false,
                "cycleway:left" to "none",
                "sidewalk:left:bicycle" to "yes"
            )
        )
    }

    @Test fun `none on left side`() {
        assertEquals(
            CyclewaySides(NONE, null),
            parse(false,"cycleway:left" to "no")
        )
        assertEquals(
            CyclewaySides(NONE, null),
            parse(false, "cycleway:left" to "none")
        )
    }

    @Test fun `busway on left side`() {
        assertEquals(
            CyclewaySides(BUSWAY, null),
            parse(false, "cycleway:left" to "share_busway")
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, null),
            parse(false,
                "cycleway:left" to "no",
                "oneway" to "yes",
                "oneway:left:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, null),
            parse(false,
                "cycleway:left" to "no",
                "oneway" to "-1",
                "oneway:left:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, null),
            parse(true,
                "cycleway:left" to "no",
                "oneway" to "yes",
                "oneway:left:bicycle" to "no"
            )
        )
    }

    @Test fun `none on left side but oneway that isn't a oneway for cyclists (reversed + left hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, null),
            parse(true,
                "cycleway:left" to "no",
                "oneway" to "-1",
                "oneway:left:bicycle" to "no"
            )
        )
    }

    /* -------------------------------------- cycleway:right  ----------------------------------- */

    @Test fun `track right`() {
        assertEquals(
            CyclewaySides(null, TRACK),
            parse(false, "cycleway:right" to "track")
        )
    }

    @Test fun `explicitly on sidewalk on right side`() {
        assertEquals(
            CyclewaySides(null, SIDEWALK_EXPLICIT),
            parse(false,
                "cycleway:right" to "track",
                "cycleway:right:segregated" to "no"
            )
        )
    }

    @Test fun `dual track on right side`() {
        assertEquals(
            CyclewaySides(null, DUAL_TRACK),
            parse(false,
                "cycleway:right" to "track",
                "cycleway:right:oneway" to "no"
            )
        )
    }

    @Test fun `unspecified lane on right side`() {
        assertEquals(
            CyclewaySides(null, LANE_UNSPECIFIED),
            parse(false, "cycleway:right" to "lane")
        )
    }

    @Test fun `unspecified dual lane on right side`() {
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive lane on right side`() {
        assertEquals(
            CyclewaySides(null, EXCLUSIVE_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive"
            )
        )
    }

    @Test fun `exclusive lane synonyms on right side`() {
        assertEquals(
            CyclewaySides(null, EXCLUSIVE_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, EXCLUSIVE_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "mandatory"
            )
        )
    }

    @Test fun `exclusive dual lane on right side`() {
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive",
                "cycleway:right:oneway" to "no"
            )
        )
    }

    @Test fun `exclusive dual lane synonyms on right side`() {
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive_lane",
                "cycleway:right:oneway" to "no"
            )
        )
        assertEquals(
            CyclewaySides(null, DUAL_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "mandatory",
                "cycleway:right:oneway" to "no"
            )
        )
    }

    @Test fun `advisory lane on right side`() {
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "advisory"
            )
        )
    }

    @Test fun `advisory lane synonyms on right side`() {
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, ADVISORY_LANE),
            parse(false,
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "dashed"
            )
        )
    }

    @Test fun `suggestion lane on right side`() {
        assertEquals(
            CyclewaySides(null, SUGGESTION_LANE),
            parse(false,
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "advisory"
            )
        )
    }

    @Test fun `suggestion lane synonyms on right side`() {
        assertEquals(
            CyclewaySides(null, SUGGESTION_LANE),
            parse(false,
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "advisory_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, SUGGESTION_LANE),
            parse(false,
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "soft_lane"
            )
        )
        assertEquals(
            CyclewaySides(null, SUGGESTION_LANE),
            parse(false,
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "dashed"
            )
        )
    }

    @Test fun `pictograms on right side`() {
        assertEquals(
            CyclewaySides(null, PICTOGRAMS),
            parse(false,
                "cycleway:right" to "shared_lane",
                "cycleway:right:lane" to "pictogram"
            )
        )
    }

    @Test fun `sidewalk on right side`() {
        assertEquals(
            CyclewaySides(null, SIDEWALK_OK),
            parse(false,
                "cycleway:right" to "no",
                "sidewalk:right:bicycle" to "yes"
            )
        )
        assertEquals(
            CyclewaySides(null, SIDEWALK_OK),
            parse(false,
                "cycleway:right" to "none",
                "sidewalk:right:bicycle" to "yes"
            )
        )
    }

    @Test fun `none on right side`() {
        assertEquals(
            CyclewaySides(null, NONE),
            parse(false,"cycleway:right" to "no")
        )
        assertEquals(
            CyclewaySides(null, NONE),
            parse(false, "cycleway:right" to "none")
        )
    }

    @Test fun `busway on right side`() {
        assertEquals(
            CyclewaySides(null, BUSWAY),
            parse(false, "cycleway:right" to "share_busway")
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, null),
            parse(false,
                "cycleway:right" to "no",
                "oneway" to "yes",
                "oneway:right:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (reversed)`() {
        assertEquals(
            CyclewaySides(NONE, null),
            parse(false,
                "cycleway:right" to "no",
                "oneway" to "-1",
                "oneway:right:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (right hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE, null),
            parse(true,
                "cycleway:right" to "no",
                "oneway" to "yes",
                "oneway:right:bicycle" to "no"
            )
        )
    }

    @Test fun `none on right side but oneway that isn't a oneway for cyclists (reversed + right hand traffic)`() {
        assertEquals(
            CyclewaySides(NONE_NO_ONEWAY, null),
            parse(true,
                "cycleway:right" to "no",
                "oneway" to "-1",
                "oneway:right:bicycle" to "no"
            )
        )
    }
    
    // TODO test cycleway:both
    // TODO opposite tagging for "cycleway:left/right/both"

    // TODO test invalid tags: lane/shared lane without correct subtag
}


private fun parse(isLeftHandTraffic: Boolean, vararg pairs: Pair<String, String>) =
    createCyclewaySides(mapOf(*pairs), isLeftHandTraffic)
