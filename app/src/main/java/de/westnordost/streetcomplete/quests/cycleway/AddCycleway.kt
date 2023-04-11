package de.westnordost.streetcomplete.quests.cycleway

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.getByLocation
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNSPECIFIED_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNSPECIFIED_SHARED_LANE
import de.westnordost.streetcomplete.osm.cycleway.LeftAndRightCycleway
import de.westnordost.streetcomplete.osm.cycleway.any
import de.westnordost.streetcomplete.osm.cycleway.applyTo
import de.westnordost.streetcomplete.osm.cycleway.createCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.isAmbiguous
import de.westnordost.streetcomplete.osm.estimateParkingOffRoadWidth
import de.westnordost.streetcomplete.osm.estimateRoadwayWidth
import de.westnordost.streetcomplete.osm.guessRoadwayWidth
import de.westnordost.streetcomplete.osm.surface.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.util.math.isNearAndAligned
import java.util.concurrent.FutureTask

class AddCycleway(
    private val countryInfos: CountryInfos,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>,
) : OsmElementQuestType<LeftAndRightCycleway> {

    override val changesetComment = "Specify whether there are cycleways"
    override val wikiLink = "Key:cycleway"
    override val icon = R.drawable.ic_quest_bicycleway
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_overlay

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

    override fun applyAnswerTo(answer: LeftAndRightCycleway, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val countryInfo = countryInfos.getByLocation(
            countryBoundariesFuture.get(),
            geometry.center.longitude,
            geometry.center.latitude
        )
        answer.applyTo(tags, countryInfo.isLeftHandTraffic)
    }
}

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
      and expressway != yes
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
        or highway = residential and (maxspeed > 33 or $notInZone30OrLess)
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
        or $notInZone30OrLess
      )
      and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
""".toElementFilterExpression() }

private val maybeSeparatelyMappedCyclewaysFilter by lazy { """
    ways with highway ~ path|footway|cycleway|construction
""".toElementFilterExpression() }
// highway=construction included, as situation often changes during and after construction

private val notInZone30OrLess =
    "~\"(${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")})\"" +
    " ~ \".*(urban|rural|trunk|motorway|nsl_single|nsl_dual)\""

private val olderThan4Years = TagOlderThan("cycleway", RelativeDate(-(365 * 4).toFloat()))

private fun Element.hasOldInvalidOrAmbiguousCyclewayTags(countryInfo: CountryInfo?): Boolean? {
    val sides = createCyclewaySides(tags, false)
    // has no cycleway tagging
    if (sides == null) return false
    // any cycleway tagging is not known: don't mess with that
    if (sides.any { it.cycleway.isUnknown }) return false
    // has any invalid cycleway tags
    if (sides.any { it.cycleway.isInvalid }) return true
    // or it is older than x years
    if (olderThan4Years.matches(this)) return true
    // has any ambiguous cycleway tags
    if (countryInfo != null) {
        if (sides.any { it.cycleway.isAmbiguous(countryInfo) }) return true
    } else {
        if (sides.any { it.cycleway == UNSPECIFIED_SHARED_LANE }) return true
        // for this, a countryCode is necessary, thus return null if no country code is available
        if (sides.any { it.cycleway == UNSPECIFIED_LANE }) return null
    }
    return false
}
