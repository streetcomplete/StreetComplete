package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.cycleway.Direction.*
import de.westnordost.streetcomplete.osm.isForwardOneway
import de.westnordost.streetcomplete.osm.isInContraflowOfOneway
import de.westnordost.streetcomplete.osm.isNotOnewayForCyclists
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.osm.isReversedOneway
import kotlinx.serialization.Serializable

data class LeftAndRightCycleway(val left: CyclewayAndDirection?, val right: CyclewayAndDirection?)

fun LeftAndRightCycleway.any(block: (cycleway: CyclewayAndDirection) -> Boolean): Boolean =
    left?.let(block) == true || right?.let(block) == true

fun LeftAndRightCycleway.selectableOrNullValues(countryInfo: CountryInfo): LeftAndRightCycleway {
    val leftIsSelectable = left?.isSelectable(countryInfo) != false
    val rightIsSelectable = right?.isSelectable(countryInfo) != false
    if (leftIsSelectable && rightIsSelectable) return this
    return LeftAndRightCycleway(
        if (leftIsSelectable) left else null,
        if (rightIsSelectable) right else null
    )
}

fun LeftAndRightCycleway.wasNoOnewayForCyclistsButNowItIs(tags: Map<String, String>, isLeftHandTraffic: Boolean): Boolean =
    isOneway(tags)
    && isNotOnewayForCyclists(tags, isLeftHandTraffic)
    && !isNotOnewayForCyclistsNow(tags, isLeftHandTraffic)

fun LeftAndRightCycleway.isNotOnewayForCyclistsNow(tags: Map<String, String>, isLeftHandTraffic: Boolean): Boolean {
    val onewayDir = when  {
        isForwardOneway(tags) -> FORWARD
        isReversedOneway(tags) -> BACKWARD
        else -> return true
    }
    val previous = createCyclewaySides(tags, isLeftHandTraffic)
    val l = (left ?: previous?.left)
    val r = (right ?: previous?.right)
    /* "no cycleway" has no direction and should be ignored
       "separate" should also be ignored because if the cycleway is mapped separately, the existence
       of a separate way that enables cyclists to go in contra-flow-direction doesn't mean that they
       can do the same on the main way for the road too (see #4715) */
    val leftDir = l?.direction?.takeIf { l.cycleway != NONE && l.cycleway != SEPARATE }
    val rightDir = r?.direction?.takeIf { r.cycleway != NONE && r.cycleway != SEPARATE }

    return leftDir == BOTH || rightDir == BOTH ||
        leftDir != null && leftDir != onewayDir ||
        rightDir != null && rightDir != onewayDir
}

@Serializable
data class CyclewayAndDirection(val cycleway: Cycleway, val direction: Direction)

fun CyclewayAndDirection.isSelectable(countryInfo: CountryInfo): Boolean =
    cycleway.isSelectable(countryInfo) &&
    // only allow dual track, dual lanes and "dual" sidewalk (not dual pictograms or something)
    (direction != BOTH || cycleway in listOf(TRACK, UNSPECIFIED_LANE, EXCLUSIVE_LANE, SIDEWALK_EXPLICIT))

@Serializable
enum class Direction {
    FORWARD, BACKWARD, BOTH;

    fun reverse(): Direction = when (this) {
        FORWARD -> BACKWARD
        BACKWARD -> FORWARD
        BOTH -> BOTH
    }
}

@Serializable
enum class Cycleway {
    /** a.k.a. cycle lane with continuous markings, dedicated lane or simply (proper) lane. Usually
     *  exclusive access for cyclists */
    EXCLUSIVE_LANE,
    /** a.k.a. cycle lane with dashed markings, protective lane, multipurpose lane, soft lane,
     *  recommended lane or cycle lanes on 2-1 roads. Usually priority access for cyclists, cars
     *  may use if necessary */
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

    /** no cycle track or lane, but cyclists use shoudler */
    SHOULDER,

    /** unknown cycleway tag set */
    UNKNOWN,

    /** definitely wrong cycleway tag (because wrong scheme, or ambiguous) set */
    INVALID
;

    val isOnSidewalk get() = this == SIDEWALK_EXPLICIT

    /** is a lane (cycleway=lane or cycleway=shared_lane), shared on busway doesn't count as a lane
     *  in that sense because it is not a subtag of the mentioned tags */
    val isLane get() = when (this) {
        EXCLUSIVE_LANE, ADVISORY_LANE, UNSPECIFIED_LANE, UNKNOWN_LANE,
        SUGGESTION_LANE, PICTOGRAMS, UNSPECIFIED_SHARED_LANE, UNKNOWN_SHARED_LANE -> true
        else -> false
    }

    val isUnknown get() = when (this) {
        UNKNOWN, UNKNOWN_LANE, UNKNOWN_SHARED_LANE -> true
        else -> false
    }

    val isReversible get() = when (this) {
        NONE, NONE_NO_ONEWAY, SEPARATE, SHOULDER -> false
        else -> true
    }

    val isInvalid get() = this == INVALID
}

private fun Cycleway.isSelectable(countryInfo: CountryInfo): Boolean =
    !isInvalid && !isUnknown && !isAmbiguous(countryInfo)

fun Cycleway.isAmbiguous(countryInfo: CountryInfo) = when (this) {
    UNSPECIFIED_SHARED_LANE ->
        true
    UNSPECIFIED_LANE ->
        countryInfo.hasAdvisoryCycleLane && countryInfo.countryCode !in listOf("BE", "NL")
    else ->
        false
}

fun getSelectableCycleways(
    countryInfo: CountryInfo,
    roadTags: Map<String, String>,
    isRightSide: Boolean,
    isLeftHandTraffic: Boolean,
    direction: Direction?
): List<CyclewayAndDirection> {
    val dir = direction?.takeUnless { it == BOTH } ?: Direction.getDefault(isRightSide, isLeftHandTraffic)
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
        SHOULDER
    )
    val dualCycleways = listOf(
        CyclewayAndDirection(if (countryInfo.hasAdvisoryCycleLane) EXCLUSIVE_LANE else UNSPECIFIED_LANE, BOTH),
        CyclewayAndDirection(TRACK, BOTH),
        CyclewayAndDirection(SIDEWALK_EXPLICIT, BOTH)
    )

    // no need to distinguish between advisory and exclusive lane where the concept of exclusive
    // lanes does not exist
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
    // different wording for a contraflow lane that is marked like a "shared" lane (just bicycle pictogram)
    if (isInContraflowOfOneway(roadTags, dir)) {
        cycleways.add(cycleways.indexOf(NONE) + 1, NONE_NO_ONEWAY)
    }
    return cycleways.map { CyclewayAndDirection(it, dir) } + dualCycleways
}

/** Return the default direction of a cycleway if nothing is specified */
fun Direction.Companion.getDefault(isRightSide: Boolean, isLeftHandTraffic: Boolean): Direction =
    if (isRightSide xor isLeftHandTraffic) FORWARD else BACKWARD

val CyclewayAndDirection.estimatedWidth: Float get() = when (cycleway) {
    EXCLUSIVE_LANE -> 1.5f
    ADVISORY_LANE -> 1f
    UNSPECIFIED_LANE -> 1f
    UNKNOWN_LANE -> 1f
    SUGGESTION_LANE -> 0.75f
    TRACK -> 1.5f
    else -> 0f
} * if (direction == BOTH) 2 else 1
