package de.westnordost.streetcomplete.quests.cycleway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNSPECIFIED_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNSPECIFIED_SHARED_LANE
import de.westnordost.streetcomplete.osm.cycleway.LeftAndRightCycleway
import de.westnordost.streetcomplete.osm.cycleway.any
import de.westnordost.streetcomplete.osm.cycleway.applyTo
import de.westnordost.streetcomplete.osm.cycleway.isAmbiguous
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.osm.isImplicitMaxSpeedButNotSlowZone
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES

class AddCycleway(
    private val getCountryInfoByLocation: (location: LatLon) -> CountryInfo,
) : OsmElementQuestType<LeftAndRightCycleway> {

    override val changesetComment = "Specify whether there are cycleways"
    override val wikiLink = "Key:cycleway"
    override val icon = R.drawable.ic_quest_bicycleway
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_overlay

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            ways with (
                highway ~ cycleway|path
                or highway ~ footway|bridleway and bicycle ~ yes|designated
              )
              and bicycle !~ no|private
              and access !~ no|private
        """)

    // See overview here: https://ent8r.github.io/blacklistr/?streetcomplete=cycleway/AddCycleway.kt
    // #749. sources:
    // Google Street View (driving around in virtual car)
    // https://en.wikivoyage.org/wiki/Cycling
    // http://peopleforbikes.org/get-local/ (US)
    override val enabledInCountries = NoCountriesExcept(
        // all of Northern and Western Europe, most of Central Europe, some of Southern Europe
        "NO", "SE", "FI", "IS", "DK", "SI",
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

    override val hint = R.string.quest_street_side_puzzle_tutorial

    override fun getTitle(tags: Map<String, String>) = when {
        parseCyclewaySides(tags, false) != null -> R.string.quest_cycleway_resurvey_title
        else -> R.string.quest_cycleway_title2
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val eligibleRoads = mapData.ways.filter { roadsFilter.matches(it) }
        val roadsWithMissingCycleway = eligibleRoads.filter { untaggedRoadsFilter.matches(it) }
        val oldRoadsWithKnownCycleways = eligibleRoads.filter { way ->
            val countryInfo = mapData.getWayGeometry(way.id)?.center?.let {
                getCountryInfoByLocation(it)
            }
            way.hasOldInvalidOrAmbiguousCyclewayTags(countryInfo) == true
        }

        return roadsWithMissingCycleway + oldRoadsWithKnownCycleways
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!roadsFilter.matches(element)) return false
        if (untaggedRoadsFilter.matches(element)) return true
        return element.hasOldInvalidOrAmbiguousCyclewayTags(null)
    }

    override fun createForm() = AddCyclewayForm()

    override fun applyAnswerTo(answer: LeftAndRightCycleway, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val countryInfo = getCountryInfoByLocation(geometry.center)
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
      highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service|busway
      and area != yes
      and motorroad != yes
      and expressway != yes
      and bicycle_road != yes
      and cyclestreet != yes
      and bicycle != no
      and bicycle != designated
      and access !~ private|no
""".toElementFilterExpression() }

// streets that do not have cycleway tagging yet
private val untaggedRoadsFilter by lazy { """
    ways with (
        highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified
        or highway = residential and (maxspeed > 33 or $isImplicitMaxSpeedButNotSlowZone)
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
        or $isImplicitMaxSpeedButNotSlowZone
      )
      and surface !~ ${UNPAVED_SURFACES.joinToString("|")}
      and ~bicycle|bicycle:backward|bicycle:forward !~ use_sidepath
      and sidewalk != separate
""".toElementFilterExpression() }

private val olderThan4Years = TagOlderThan("cycleway", RelativeDate(-(365 * 4).toFloat()))

private fun Element.hasOldInvalidOrAmbiguousCyclewayTags(countryInfo: CountryInfo?): Boolean? {
    val sides = parseCyclewaySides(tags, false)
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
