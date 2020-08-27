package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*

enum class Cycleway {
    // some kind of cycle lane, not specified if with continuous or dashed lane markings
    LANE_UNSPECIFIED,
    // a.k.a. exclusive lane, dedicated lane or simply (proper) lane
    EXCLUSIVE_LANE,
    // a.k.a. protective lane, multipurpose lane, soft lane or recommended lane
    ADVISORY_LANE,
    // slight difference to dashed lane only made in NL, BE
    SUGGESTION_LANE,
    // cycle track
    TRACK,
    // no cycleway
    NONE,
    // none, but oneway road is not oneway for cyclists (sometimes has pictograms)
    NONE_NO_ONEWAY,
    // shared lane with pictograms
    PICTOGRAMS,
    // no cycleway, but cyclists are to explicitly share the sidewalk
    SIDEWALK_EXPLICIT,
    // no cycleway, but cyclists are allowed on sidewalk
    SIDEWALK_OK,
    // lane in both directions
    DUAL_LANE,
    // track in both directions
    DUAL_TRACK,
    // shared with bus lane
    BUSWAY;

    val isOnSidewalk get() = this == SIDEWALK_EXPLICIT || this == SIDEWALK_OK

    /** is a lane (cycleway=lane or cycleway=shared_lane), shared on busway doesn't count as a lane
     *  in that sense because it is not a subtag of the mentioned tags */
    val isLane get() =
        this == LANE_UNSPECIFIED ||
        this == EXCLUSIVE_LANE ||
        this == ADVISORY_LANE ||
        this == SUGGESTION_LANE ||
        this == PICTOGRAMS ||
        this == DUAL_LANE

    val isOneway get() = this != DUAL_LANE && this != DUAL_TRACK
}
