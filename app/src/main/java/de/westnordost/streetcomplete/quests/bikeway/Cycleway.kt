package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*

enum class Cycleway {
    // a.k.a. exclusive lane, dedicated lane or simply (proper) lane
    EXCLUSIVE_LANE,
    // lane in both directions
    DUAL_LANE,
    // a.k.a. protective lane, multipurpose lane, soft lane or recommended lane
    ADVISORY_LANE,
    // some kind of cycle lane, not specified if with continuous or dashed lane markings
    UNSPECIFIED_LANE,
    // unknown lane: lane tag set, but unknown subtag
    UNKNOWN_LANE,

    // slight difference to dashed lane only made in NL, BE
    SUGGESTION_LANE,
    // cycle track
    PICTOGRAMS,
    // unspecified shared lane: shared lane tag set, but no subtag
    UNSPECIFIED_SHARED_LANE,
    // unknown shared lane: shared lane tag set, but unknown subtag
    UNKNOWN_SHARED_LANE,

    // no cycleway, but cyclists are to explicitly share the sidewalk
    TRACK,
    // track in both directions
    DUAL_TRACK,

    // shared with bus lane
    BUSWAY,

    // shared lane with pictograms
    SIDEWALK_EXPLICIT,
    // no cycleway, but cyclists are allowed on sidewalk
    SIDEWALK_OK,

    // no cycleway
    NONE,
    // none, but oneway road is not oneway for cyclists (sometimes has pictograms)
    NONE_NO_ONEWAY,

    // unknown cycleway tag set
    UNKNOWN
;

    val isOnSidewalk get() = this == SIDEWALK_EXPLICIT || this == SIDEWALK_OK

    /** is a lane (cycleway=lane or cycleway=shared_lane), shared on busway doesn't count as a lane
     *  in that sense because it is not a subtag of the mentioned tags */
    val isLane get() = when(this) {
        EXCLUSIVE_LANE, DUAL_LANE, ADVISORY_LANE, UNSPECIFIED_LANE, UNKNOWN_LANE,
        SUGGESTION_LANE, PICTOGRAMS, UNSPECIFIED_SHARED_LANE, UNKNOWN_SHARED_LANE -> true
        else -> false
    }

    val isOneway get() = this != DUAL_LANE && this != DUAL_TRACK
}

val Cycleway.estimatedWidth: Float get() = when(this) {
    EXCLUSIVE_LANE -> 1.5f
    DUAL_LANE -> 3f
    ADVISORY_LANE -> 1f
    UNSPECIFIED_LANE -> 1f
    UNKNOWN_LANE -> 1f
    SUGGESTION_LANE -> 0.75f
    else -> 0f
}
