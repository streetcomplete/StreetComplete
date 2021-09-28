package de.westnordost.streetcomplete.quests.cycleway

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.*
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST

import de.westnordost.streetcomplete.quests.cycleway.Cycleway.*
import de.westnordost.streetcomplete.util.isNearAndAligned

class AddCycleway(private val countryInfos: CountryInfos) : OsmElementQuestType<CyclewayAnswer> {

    override val commitMessage = "Add whether there are cycleways"
    override val wikiLink = "Key:cycleway"
    override val icon = R.drawable.ic_quest_bicycleway

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

    override val isSplitWayEnabled = true

    override val questTypeAchievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) : Int =
        if (createCyclewaySides(tags, false) != null)
            R.string.quest_cycleway_resurvey_title
        else
            R.string.quest_cycleway_title2

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
                    val minDistToWays = estimatedWidth(road.tags) / 2.0 + 6
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
            val countryCode = mapData.getWayGeometry(way.id)?.center?.let { p ->
                countryInfos.get(p.longitude, p.latitude).countryCode
            }
            way.hasOldInvalidOrAmbiguousCyclewayTags(countryCode) == true
        }

        return roadsWithMissingCycleway + oldRoadsWithKnownCycleways
    }

    private fun estimatedWidth(tags: Map<String, String>): Float {
        val width = tags["width"]?.toFloatOrNull()
        if (width != null) return width
        val lanes = tags["lanes"]?.toIntOrNull()
        if (lanes != null) return lanes * 3f
        return 12f
    }

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

    override fun applyAnswerTo(answer: CyclewayAnswer, changes: StringMapChangesBuilder) {

        if (answer.left == answer.right) {
            answer.left?.let { applyCyclewayAnswerTo(it.cycleway, Side.BOTH, 0, changes) }
            deleteCyclewayAnswerIfExists(Side.LEFT, changes)
            deleteCyclewayAnswerIfExists(Side.RIGHT, changes)
        } else {
            answer.left?.let { applyCyclewayAnswerTo(it.cycleway, Side.LEFT, it.dirInOneway, changes) }
            answer.right?.let { applyCyclewayAnswerTo(it.cycleway, Side.RIGHT, it.dirInOneway, changes) }
            deleteCyclewayAnswerIfExists(Side.BOTH, changes)
        }
        deleteCyclewayAnswerIfExists(null, changes)

        applySidewalkAnswerTo(answer.left?.cycleway, answer.right?.cycleway, changes)

        if (answer.isOnewayNotForCyclists) {
            changes.addOrModify("oneway:bicycle", "no")
        } else {
            changes.deleteIfPreviously("oneway:bicycle", "no")
        }

        // only set the check date if nothing was changed
        val isNotActuallyChangingAnything = changes.getChanges().all { change ->
            change is StringMapEntryModify && change.value == change.valueBefore
        }
        if (isNotActuallyChangingAnything || changes.hasCheckDateForKey("cycleway")) {
            changes.updateCheckDateForKey("cycleway")
        }
    }

    /** Just add a sidewalk if we implicitly know from the answer that there is one */
    private fun applySidewalkAnswerTo(
        cyclewayLeft: Cycleway?, cyclewayRight: Cycleway?, changes: StringMapChangesBuilder) {

        /* only tag if we know the sidewalk value for both sides (because it is not possible in
           OSM to specify the sidewalk value only for one side. sidewalk:right/left=yes is not
           well established. */
        if (cyclewayLeft?.isOnSidewalk == true && cyclewayRight?.isOnSidewalk == true) {
            changes.addOrModify("sidewalk", "both")
        }
    }

    private enum class Side(val value: String) {
        LEFT("left"), RIGHT("right"), BOTH("both")
    }

    private fun applyCyclewayAnswerTo(cycleway: Cycleway, side: Side, dir: Int,
                                      changes: StringMapChangesBuilder) {
        val directionValue = when {
            dir > 0 -> "yes"
            dir < 0 -> "-1"
            else -> null
        }

        val cyclewayKey = "cycleway:" + side.value
        when (cycleway) {
            NONE, NONE_NO_ONEWAY -> {
                changes.addOrModify(cyclewayKey, "no")
            }
            EXCLUSIVE_LANE, ADVISORY_LANE, UNSPECIFIED_LANE -> {
                changes.addOrModify(cyclewayKey, "lane")
                if (directionValue != null) {
                    changes.addOrModify("$cyclewayKey:oneway", directionValue)
                }
                if (cycleway == EXCLUSIVE_LANE)
                    changes.addOrModify("$cyclewayKey:lane", "exclusive")
                else if (cycleway == ADVISORY_LANE)
                    changes.addOrModify("$cyclewayKey:lane","advisory")
            }
            TRACK -> {
                changes.addOrModify(cyclewayKey, "track")
                if (directionValue != null) {
                    changes.addOrModify("$cyclewayKey:oneway", directionValue)
                }
                if (changes.getPreviousValue("$cyclewayKey:segregated") == "no") {
                    changes.modify("$cyclewayKey:segregated", "yes")
                }
            }
            DUAL_TRACK -> {
                changes.addOrModify(cyclewayKey, "track")
                changes.addOrModify("$cyclewayKey:oneway", "no")
            }
            DUAL_LANE -> {
                changes.addOrModify(cyclewayKey, "lane")
                changes.addOrModify("$cyclewayKey:oneway", "no")
                changes.addOrModify("$cyclewayKey:lane", "exclusive")
            }
            SIDEWALK_EXPLICIT -> {
                // https://wiki.openstreetmap.org/wiki/File:Z240GemeinsamerGehundRadweg.jpeg
                changes.addOrModify(cyclewayKey, "track")
                changes.addOrModify("$cyclewayKey:segregated", "no")
            }
            PICTOGRAMS -> {
                changes.addOrModify(cyclewayKey, "shared_lane")
                changes.addOrModify("$cyclewayKey:lane", "pictogram")
            }
            SUGGESTION_LANE -> {
                changes.addOrModify(cyclewayKey, "shared_lane")
                changes.addOrModify("$cyclewayKey:lane", "advisory")
            }
            BUSWAY -> {
                changes.addOrModify(cyclewayKey, "share_busway")
            }
            SEPARATE -> {
                changes.addOrModify(cyclewayKey, "separate")
            }
            else -> {
                throw IllegalArgumentException("Invalid cycleway")
            }
        }

        // clear previous cycleway:lane value
        if (!cycleway.isLane) {
            changes.deleteIfExists("$cyclewayKey:lane")
        }
        // clear previous cycleway:oneway=no value (if not about to set a new value)
        if (cycleway.isOneway && directionValue == null) {
            changes.deleteIfPreviously("$cyclewayKey:oneway", "no")
        }
        // clear previous cycleway:segregated=no value
        if (cycleway != SIDEWALK_EXPLICIT && cycleway != TRACK) {
            changes.deleteIfPreviously("$cyclewayKey:segregated", "no")
        }
    }

    /** clear previous answers for the given side */
    private fun deleteCyclewayAnswerIfExists(side: Side?, changes: StringMapChangesBuilder) {
        val sideVal = if (side == null) "" else ":" + side.value
        val cyclewayKey = "cycleway$sideVal"

        // only things are cleared that are set by this quest
        // for example cycleway:surface should only be cleared by a cycleway surface quest etc.
        changes.deleteIfExists(cyclewayKey)
        changes.deleteIfExists("$cyclewayKey:lane")
        changes.deleteIfExists("$cyclewayKey:oneway")
        changes.deleteIfExists("$cyclewayKey:segregated")
        changes.deleteIfExists("sidewalk$sideVal:bicycle")
    }

    companion object {

        /* Excluded is
          - anything explicitly tagged as no bicycles or having to use separately mapped sidepath
          - if not already tagged with a cycleway: streets with low speed or that are not paved, as
            they are very unlikely to have cycleway infrastructure
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
                or highway = residential and (
                  maxspeed > 30
                  or (maxspeed ~ ".*mph" and maxspeed !~ "([1-9]|1[0-9]|20) mph")
                  or $notIn30ZoneOrLess
                )
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
                or (maxspeed ~ ".*mph" and maxspeed !~ "([1-9]|1[0-2]) mph")
                or $notIn30ZoneOrLess
              )
              and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
        """.toElementFilterExpression() }

        private val maybeSeparatelyMappedCyclewaysFilter by lazy { """
            ways with highway ~ path|footway|cycleway
        """.toElementFilterExpression() }

        private val notIn30ZoneOrLess = MAXSPEED_TYPE_KEYS.joinToString(" or ") {
            """$it and $it !~ ".*zone:?([1-9]|[1-2][0-9]|30)""""
        }

        private val olderThan4Years = TagOlderThan("cycleway", RelativeDate(-(365 * 4).toFloat()))

        private fun Element.hasOldInvalidOrAmbiguousCyclewayTags(countryCode: String?): Boolean? {
            val sides = createCyclewaySides(tags, false)
            // has no cycleway tagging
            if (sides == null) return false
            // any cycleway tagging is not known: don't mess with that
            if (sides.any { it.isUnknown }) return false
            // has any invalid cycleway tags
            if (sides.any { it == INVALID }) return true
            // or it is older than x years
            if (olderThan4Years.matches(this)) return true
            // has any ambiguous cycleway tags
            if (countryCode != null) {
                if (sides.any { it.isAmbiguous(countryCode) }) return true
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
