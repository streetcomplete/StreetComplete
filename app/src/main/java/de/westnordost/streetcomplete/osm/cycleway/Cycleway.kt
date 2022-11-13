package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*

enum class Cycleway {
    /** a.k.a. cycle lane with continuous markings, dedicated lane or simply (proper) lane. Usually
     *  exclusive access for cyclists */
    EXCLUSIVE_LANE,
    /** [EXCLUSIVE_LANE] in both directions */
    DUAL_LANE,
    /** a.k.a. cycle lane with dashed markings, protective lane, multipurpose lane, soft lane or
     *  recommended lane. Usually priority access for cyclists, cars may use if necessary */
    ADVISORY_LANE,
    /** some kind of cycle lane, not specified whether exclusive or advisory */
    UNSPECIFIED_LANE,
    /** unknown lane: lane tag set, but unknown subtag */
    UNKNOWN_LANE,

    /** slight difference to advisory lane only made in NL, BE. Basically a very slim multi-purpose
     *  shoulder */
    SUGGESTION_LANE,
    /** a.k.a. sharrows, shared lane with pictograms. Just some bicycle pictograms drawn on the
     *  roadway to signalize that there are also cyclists here (and are allowed to use the road) */
    PICTOGRAMS,
    /** shared lane tag set, but no subtag */
    UNSPECIFIED_SHARED_LANE,
    /** shared lane tag set, but unknown subtag */
    UNKNOWN_SHARED_LANE,

    /** cycle track, i.e. beyond curb or other barrier */
    TRACK,
    /** [TRACK] in both directions */
    DUAL_TRACK,

    /** cyclists share space with bus lane */
    BUSWAY,

    /** cyclists explicitly ought to share the sidewalk with pedestrians, i.e. the cycle track is
     *  not segregated from the sidewalk */
    SIDEWALK_EXPLICIT,
    // the following not anymore, see #2276
    // no cycleway, but cyclists are allowed on sidewalk
    // SIDEWALK_OK,

    /** no cycle track or lane */
    NONE,
    /** none, but oneway road is not oneway for cyclists (sometimes has pictograms, which is why it
     *  should not be confused with [PICTOGRAMS]) */
    NONE_NO_ONEWAY,

    /** cycleway is mapped as a separate way */
    SEPARATE,

    /** unknown cycleway tag set */
    UNKNOWN,

    /** definitely wrong cycleway tag (because wrong scheme, or ambiguous) set */
    INVALID
;

    val isOnSidewalk get() = this == SIDEWALK_EXPLICIT

    /** is a lane (cycleway=lane or cycleway=shared_lane), shared on busway doesn't count as a lane
     *  in that sense because it is not a subtag of the mentioned tags */
    val isLane get() = when (this) {
        EXCLUSIVE_LANE, DUAL_LANE, ADVISORY_LANE, UNSPECIFIED_LANE, UNKNOWN_LANE,
        SUGGESTION_LANE, PICTOGRAMS, UNSPECIFIED_SHARED_LANE, UNKNOWN_SHARED_LANE -> true
        else -> false
    }

    val isUnknown get() = when (this) {
        UNKNOWN, UNKNOWN_LANE, UNKNOWN_SHARED_LANE -> true
        else -> false
    }

    val isInvalid get() = this == INVALID

    val isOneway get() = this != DUAL_LANE && this != DUAL_TRACK
}

fun Cycleway.isAmbiguous(countryInfo: CountryInfo) = when (this) {
    UNSPECIFIED_SHARED_LANE ->
        true
    UNSPECIFIED_LANE ->
        countryInfo.hasAdvisoryCycleLane && countryInfo.countryCode !in listOf("BE", "NL")
    else ->
        false
}

fun getSelectableCyclewaysInCountry(countryInfo: CountryInfo): List<Cycleway> {
    val cycleways = mutableListOf(
        NONE,
        TRACK,
        EXCLUSIVE_LANE,
        ADVISORY_LANE,
        UNSPECIFIED_LANE,
        SUGGESTION_LANE,
        SEPARATE,
        PICTOGRAMS,
        BUSWAY,
        SIDEWALK_EXPLICIT,
        DUAL_LANE,
        DUAL_TRACK
    )
    if (countryInfo.hasAdvisoryCycleLane) {
        cycleways.remove(UNSPECIFIED_LANE)
    } else {
        cycleways.remove(EXCLUSIVE_LANE)
        cycleways.remove(ADVISORY_LANE)
    }
    // different tagging for NL / BE
    if (countryInfo.countryCode in listOf("BE", "NL")) {
        cycleways.remove(ADVISORY_LANE)
    } else {
        cycleways.remove(SUGGESTION_LANE)
    }
    return cycleways
}

val Cycleway.estimatedWidth: Float get() = when (this) {
    EXCLUSIVE_LANE -> 1.5f
    DUAL_LANE -> 3f
    ADVISORY_LANE -> 1f
    UNSPECIFIED_LANE -> 1f
    UNKNOWN_LANE -> 1f
    SUGGESTION_LANE -> 0.75f
    TRACK -> 1.5f
    DUAL_TRACK -> 3f
    else -> 0f
}
