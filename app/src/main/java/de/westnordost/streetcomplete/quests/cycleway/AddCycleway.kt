package de.westnordost.streetcomplete.quests.cycleway

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.ADVISORY_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.BUSWAY
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.DUAL_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.DUAL_TRACK
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.EXCLUSIVE_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.NONE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.NONE_NO_ONEWAY
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.PICTOGRAMS
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.SEPARATE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.SIDEWALK_EXPLICIT
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.SUGGESTION_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.TRACK
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNSPECIFIED_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNSPECIFIED_SHARED_LANE
import de.westnordost.streetcomplete.osm.cycleway.LeftAndRightCycleway
import de.westnordost.streetcomplete.osm.cycleway.createCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.isAmbiguous
import de.westnordost.streetcomplete.osm.estimateParkingOffRoadWidth
import de.westnordost.streetcomplete.osm.estimateRoadwayWidth
import de.westnordost.streetcomplete.osm.guessRoadwayWidth
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.util.math.isNearAndAligned
import java.util.concurrent.FutureTask

class AddCycleway(
    private val countryInfos: CountryInfos,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
) : OsmElementQuestType<CyclewayAnswer> {

    override val changesetComment = "Specify whether there are cycleways"
    override val wikiLink = "Key:cycleway"
    override val icon = R.drawable.ic_quest_bicycleway
    override val achievements = listOf(BICYCLIST)

    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=cycleway/AddCycleway.kt
    // #749. sources:
    // Google Street View (driving around in virtual car)
    // https://en.wikivoyage.org/wiki/Cycling
    // http://peopleforbikes.org/get-local/ (US)
    override val enabledInCountries = NoCountriesExcept(
        // all of Northern and Western Europe, most of Central Europe, some of Southern Europe
        "NO", "SE", "FI", "IS", "DK",
        "GB", "IE", "NL", "BE", "FR", "LU",
        "DE", "PL", "CZ", "HU", "AT", "CH", "LI",
        "ES", "IT", "HR",
        // East Asia
        "JP", "KR", "TW",
        // some of China (East Coast)
        "CN-BJ", "CN-TJ", "CN-SD", "CN-JS", "CN-SH",
        "CN-ZJ", "CN-FJ", "CN-GD", "CN-CQ",
        // Australia etc
        "NZ", "AU",
        // some of Canada
        "CA-BC", "CA-QC", "CA-ON", "CA-NS", "CA-PE",
        // some of the US
        // West Coast, East Coast, Center, South
        "US-WA", "US-OR", "US-CA",
        "US-MA", "US-NJ", "US-NY", "US-DC", "US-CT", "US-FL",
        "US-MN", "US-MI", "US-IL", "US-WI", "US-IN",
        "US-AZ", "US-TX"
    )

    override fun getTitle(tags: Map<String, String>) = when {
        createCyclewaySides(tags, false) != null -> R.string.quest_cycleway_resurvey_title
        else -> R.string.quest_cycleway_title2
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val eligibleRoads = mapData.ways.filter { roadsFilter.matches(it) }

        /* we want to return two sets of roads: Those that do not have any cycleway tags, and those
         * that do but have been checked more than 4 years ago. */

        /* For the first, the roadsWithMissingCycleway filter is not enough. In OSM, cycleways may be
         * mapped as separate ways as well and it is not guaranteed that in this case,
         * cycleway = separate or something is always tagged on the main road then. So, all roads
         * should be excluded whose center is within of ~15 meters of a cycleway, to be on the safe
         * side. */

        val roadsWithMissingCycleway = eligibleRoads.filter { untaggedRoadsFilter.matches(it) }.toMutableList()

        if (roadsWithMissingCycleway.isNotEmpty()) {

            val maybeSeparatelyMappedCyclewayGeometries = mapData.ways
                .filter { maybeSeparatelyMappedCyclewaysFilter.matches(it) }
                .mapNotNull { mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry }

            val minAngleToWays = 25.0

            if (maybeSeparatelyMappedCyclewayGeometries.isNotEmpty()) {
                // filter out roads with missing cycleways that are near footways
                roadsWithMissingCycleway.removeAll { road ->
                    val minDistToWays = getMinDistanceToWays(road.tags).toDouble()
                    val roadGeometry = mapData.getWayGeometry(road.id) as? ElementPolylinesGeometry
                    roadGeometry?.isNearAndAligned(
                        minDistToWays,
                        minAngleToWays,
                        maybeSeparatelyMappedCyclewayGeometries
                    ) ?: true
                }
            }
        }

        /* For the second, nothing special. Filter out ways that have been checked less then 4
        *  years ago or have no known cycleway tags */

        val oldRoadsWithKnownCycleways = eligibleRoads.filter { way ->
            val countryInfo = mapData.getWayGeometry(way.id)?.center?.let { p ->
                countryInfos.getByLocation(
                    countryBoundariesFuture.get(),
                    p.longitude,
                    p.latitude,
                )
            }
            way.hasOldInvalidOrAmbiguousCyclewayTags(countryInfo) == true
        }

        return roadsWithMissingCycleway + oldRoadsWithKnownCycleways
    }

    private fun getMinDistanceToWays(tags: Map<String, String>): Float =
        (
            (estimateRoadwayWidth(tags) ?: guessRoadwayWidth(tags)) +
            (estimateParkingOffRoadWidth(tags) ?: 0f)
        ) / 2f +
        4f // + generous buffer for possible grass verge

    override fun isApplicableTo(element: Element): Boolean? {
        if (!roadsFilter.matches(element)) return false

        /* can't determine for yet untagged roads by the tags alone because we need info about
           surrounding geometry */
        if (untaggedRoadsFilter.matches(element)) return null

        /* but if already tagged and old, we don't need to look at surrounding geometry to see if
           it is applicable or not */
        return element.hasOldInvalidOrAmbiguousCyclewayTags(null)
    }

    override fun createForm() = AddCyclewayForm()

    override fun applyAnswerTo(answer: CyclewayAnswer, tags: Tags, timestampEdited: Long) {
        if (answer.left == answer.right) {
            answer.left?.let { applyCyclewayAnswerTo(it.cycleway, Side.BOTH, 0, tags) }
            deleteCyclewayAnswerIfExists(Side.LEFT, tags)
            deleteCyclewayAnswerIfExists(Side.RIGHT, tags)
        } else {
            answer.left?.let { applyCyclewayAnswerTo(it.cycleway, Side.LEFT, it.dirInOneway, tags) }
            answer.right?.let { applyCyclewayAnswerTo(it.cycleway, Side.RIGHT, it.dirInOneway, tags) }
            deleteCyclewayAnswerIfExists(Side.BOTH, tags)
        }
        deleteCyclewayAnswerIfExists(null, tags)

        applySidewalkAnswerTo(answer.left?.cycleway, answer.right?.cycleway, tags)

        if (answer.isOnewayNotForCyclists) {
            tags["oneway:bicycle"] = "no"
        } else {
            if (tags["oneway:bicycle"] == "no") {
                tags.remove("oneway:bicycle")
            }
        }

        // only set the check date if nothing was changed
        if (!tags.hasChanges || tags.hasCheckDateForKey("cycleway")) {
            tags.updateCheckDateForKey("cycleway")
        }
    }

    /** Just add a sidewalk if we implicitly know from the answer that there is one */
    private fun applySidewalkAnswerTo(cyclewayLeft: Cycleway?, cyclewayRight: Cycleway?, tags: Tags) {

        /* only tag if we know the sidewalk value for both sides (because it is not possible in
           OSM to specify the sidewalk value only for one side. sidewalk:right/left=yes is not
           well established. */
        if (cyclewayLeft?.isOnSidewalk == true && cyclewayRight?.isOnSidewalk == true) {
            tags["sidewalk"] = "both"
        }
    }

    private enum class Side(val value: String) {
        LEFT("left"), RIGHT("right"), BOTH("both")
    }

    private fun applyCyclewayAnswerTo(cycleway: Cycleway, side: Side, dir: Int, tags: Tags) {
        val directionValue = when {
            dir > 0 -> "yes"
            dir < 0 -> "-1"
            else -> null
        }

        val cyclewayKey = "cycleway:" + side.value
        when (cycleway) {
            NONE, NONE_NO_ONEWAY -> {
                tags[cyclewayKey] = "no"
            }
            EXCLUSIVE_LANE, ADVISORY_LANE, UNSPECIFIED_LANE -> {
                tags[cyclewayKey] = "lane"
                if (directionValue != null) {
                    tags["$cyclewayKey:oneway"] = directionValue
                }
                if (cycleway == EXCLUSIVE_LANE) {
                    tags["$cyclewayKey:lane"] = "exclusive"
                } else if (cycleway == ADVISORY_LANE) {
                    tags["$cyclewayKey:lane"] = "advisory"
                }
            }
            TRACK -> {
                tags[cyclewayKey] = "track"
                if (directionValue != null) {
                    tags["$cyclewayKey:oneway"] = directionValue
                }
                if (tags.containsKey("$cyclewayKey:segregated")) {
                    tags["$cyclewayKey:segregated"] = "yes"
                }
            }
            DUAL_TRACK -> {
                tags[cyclewayKey] = "track"
                tags["$cyclewayKey:oneway"] = "no"
            }
            DUAL_LANE -> {
                tags[cyclewayKey] = "lane"
                tags["$cyclewayKey:oneway"] = "no"
                tags["$cyclewayKey:lane"] = "exclusive"
            }
            SIDEWALK_EXPLICIT -> {
                // https://wiki.openstreetmap.org/wiki/File:Z240GemeinsamerGehundRadweg.jpeg
                tags[cyclewayKey] = "track"
                tags["$cyclewayKey:segregated"] = "no"
            }
            PICTOGRAMS -> {
                tags[cyclewayKey] = "shared_lane"
                tags["$cyclewayKey:lane"] = "pictogram"
            }
            SUGGESTION_LANE -> {
                tags[cyclewayKey] = "shared_lane"
                tags["$cyclewayKey:lane"] = "advisory"
            }
            BUSWAY -> {
                tags[cyclewayKey] = "share_busway"
            }
            SEPARATE -> {
                tags[cyclewayKey] = "separate"
            }
            else -> {
                throw IllegalArgumentException("Invalid cycleway")
            }
        }

        // clear previous cycleway:lane value
        if (!cycleway.isLane) {
            tags.remove("$cyclewayKey:lane")
        }
        // clear previous cycleway:oneway=no value (if not about to set a new value)
        if (cycleway.isOneway && directionValue == null) {
            if (tags["$cyclewayKey:oneway"] == "no") {
                tags.remove("$cyclewayKey:oneway")
            }
        }
        // clear previous cycleway:segregated=no value
        if (cycleway != SIDEWALK_EXPLICIT && cycleway != TRACK) {
            if (tags["$cyclewayKey:segregated"] == "no") {
                tags.remove("$cyclewayKey:segregated")
            }
        }
    }

    /** clear previous answers for the given side */
    private fun deleteCyclewayAnswerIfExists(side: Side?, tags: Tags) {
        val sideVal = if (side == null) "" else ":" + side.value
        val cyclewayKey = "cycleway$sideVal"

        // only things are cleared that are set by this quest
        // for example cycleway:surface should only be cleared by a cycleway surface quest etc.
        tags.remove(cyclewayKey)
        tags.remove("$cyclewayKey:lane")
        tags.remove("$cyclewayKey:oneway")
        tags.remove("$cyclewayKey:segregated")
        tags.remove("sidewalk$sideVal:bicycle")
    }

    companion object {

        /* Excluded is
          - anything explicitly tagged as no bicycles or having to use separately mapped sidepath
          - if not already tagged with a cycleway: streets with low speed or that are not paved, as
            they are very unlikely to have cycleway infrastructure
                - for highway=residential without speed limit tagged assume low speed
          - if not already tagged, roads that are close (15m) to foot or cycleways (see #718)
          - if already tagged, if not older than 4 years or if the cycleway tag uses some unknown value
        */

        // streets that may have cycleway tagging
        private val roadsFilter by lazy { """
            ways with
              highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service
              and area != yes
              and motorroad != yes
              and bicycle_road != yes
              and cyclestreet != yes
              and bicycle != no
              and bicycle != designated
              and access !~ private|no
              and bicycle != use_sidepath
              and bicycle:backward != use_sidepath
              and bicycle:forward != use_sidepath
              and sidewalk != separate
        """.toElementFilterExpression() }

        // streets that do not have cycleway tagging yet
        private val untaggedRoadsFilter by lazy { """
            ways with (
                highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified
                or highway = residential and (maxspeed > 33 or $notIn30ZoneOrLess)
              )
              and !cycleway
              and !cycleway:left
              and !cycleway:right
              and !cycleway:both
              and !sidewalk:bicycle
              and !sidewalk:left:bicycle
              and !sidewalk:right:bicycle
              and !sidewalk:both:bicycle
              and (
                !maxspeed
                or maxspeed > 20
                or $notIn30ZoneOrLess
              )
              and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
        """.toElementFilterExpression() }

        private val maybeSeparatelyMappedCyclewaysFilter by lazy { """
            ways with highway ~ path|footway|cycleway|construction
        """.toElementFilterExpression() }
        // highway=construction included, as situation often changes during and after construction

        private val notIn30ZoneOrLess = MAXSPEED_TYPE_KEYS.joinToString(" or ") {
            """$it and $it !~ ".*zone:?([1-9]|[1-2][0-9]|30)""""
        }

        private val olderThan4Years = TagOlderThan("cycleway", RelativeDate(-(365 * 4).toFloat()))

        private fun Element.hasOldInvalidOrAmbiguousCyclewayTags(countryInfo: CountryInfo?): Boolean? {
            val sides = createCyclewaySides(tags, false)
            // has no cycleway tagging
            if (sides == null) return false
            // any cycleway tagging is not known: don't mess with that
            if (sides.any { it.isUnknown }) return false
            // has any invalid cycleway tags
            if (sides.any { it.isInvalid }) return true
            // or it is older than x years
            if (olderThan4Years.matches(this)) return true
            // has any ambiguous cycleway tags
            if (countryInfo != null) {
                if (sides.any { it.isAmbiguous(countryInfo) }) return true
            } else {
                if (sides.any { it == UNSPECIFIED_SHARED_LANE }) return true
                // for this, a countryCode is necessary, thus return null if no country code is available
                if (sides.any { it == UNSPECIFIED_LANE }) return null
            }
            return false
        }
    }
}

private fun LeftAndRightCycleway.any(block: (cycleway: Cycleway) -> Boolean): Boolean =
    left?.let(block) == true || right?.let(block) == true
