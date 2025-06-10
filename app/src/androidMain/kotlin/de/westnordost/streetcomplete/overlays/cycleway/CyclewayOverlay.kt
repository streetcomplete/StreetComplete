package de.westnordost.streetcomplete.overlays.cycleway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.AndroidOverlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_boulevard.parseBicycleBoulevard
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.cycleway.isAmbiguous
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.parseSeparateCycleway
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.maxspeed.MAX_SPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.oneway.Direction
import de.westnordost.streetcomplete.osm.oneway.isInContraflowOfOneway
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.quests.cycleway.AddCycleway

class CyclewayOverlay(
    private val getCountryInfoByLocation: (location: LatLon) -> CountryInfo,
) : Overlay, AndroidOverlay {

    override val title = R.string.overlay_cycleway
    override val icon = R.drawable.ic_quest_bicycleway
    override val changesetComment = "Specify whether there are cycleways"
    override val wikiLink: String = "Key:cycleway"
    override val achievements = listOf(EditTypeAchievement.BICYCLIST)
    override val hidesQuestTypes = setOf(AddCycleway::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        // roads
        mapData.filter("""
            ways with
              highway ~ ${ALL_ROADS.joinToString("|")}
              and area != yes
        """).mapNotNull {
            val pos = mapData.getWayGeometry(it.id)?.center ?: return@mapNotNull null
            val countryInfo = getCountryInfoByLocation(pos)
            it to getStreetCyclewayStyle(it, countryInfo)
        } +
        // separately mapped ways
        mapData.filter("""
            ways with
              highway ~ cycleway|path|footway
              and horse != designated
              and area != yes
        """).map { it to getSeparateCyclewayStyle(it) }

    override fun createForm(element: Element?) =
        if (element == null) {
            null
        } else if (element.tags["highway"] in ALL_ROADS) {
            StreetCyclewayOverlayForm()
        } else {
            SeparateCyclewayForm()
        }
}

private fun getSeparateCyclewayStyle(element: Element) =
    OverlayStyle.Polyline(OverlayStyle.Stroke(parseSeparateCycleway(element.tags).getColor()))

private fun SeparateCycleway?.getColor() = when (this) {
    SeparateCycleway.NOT_ALLOWED,
    SeparateCycleway.NON_DESIGNATED_ON_FOOTWAY,
    SeparateCycleway.PATH ->
        OverlayColor.Black

    SeparateCycleway.NON_SEGREGATED ->
        OverlayColor.Cyan

    SeparateCycleway.SEGREGATED,
    SeparateCycleway.EXCLUSIVE,
    SeparateCycleway.EXCLUSIVE_WITH_SIDEWALK ->
        OverlayColor.Blue
    SeparateCycleway.ALLOWED_ON_FOOTWAY ->
        OverlayColor.Aquamarine

    null ->
        OverlayColor.Invisible
}

private fun getStreetCyclewayStyle(element: Element, countryInfo: CountryInfo): OverlayStyle.Polyline {
    val isLeftHandTraffic = countryInfo.isLeftHandTraffic
    val cycleways = parseCyclewaySides(element.tags, isLeftHandTraffic)
    val isNoCyclewayExpectedLeft = { cyclewayTaggingNotExpected(element, false, isLeftHandTraffic) }
    val isNoCyclewayExpectedRight = { cyclewayTaggingNotExpected(element, true, isLeftHandTraffic) }

    return OverlayStyle.Polyline(
        stroke = getStreetStrokeStyle(element.tags),
        strokeLeft = cycleways?.left?.cycleway.getStyle(countryInfo, isNoCyclewayExpectedLeft),
        strokeRight = cycleways?.right?.cycleway.getStyle(countryInfo, isNoCyclewayExpectedRight)
    )
}

private fun getStreetStrokeStyle(tags: Map<String, String>): OverlayStyle.Stroke? {
    val isBicycleBoulevard = parseBicycleBoulevard(tags) == BicycleBoulevard.YES
    val isPedestrian = tags["highway"] == "pedestrian"
    val isBicycleDesignated = tags["bicycle"] == "designated"
    val isBicycleOk = tags["bicycle"] == "yes" && tags["bicycle:signed"] == "yes"

    return when {
        isBicycleBoulevard ->
            OverlayStyle.Stroke(OverlayColor.Gold, dashed = true)
        isPedestrian && isBicycleDesignated ->
            OverlayStyle.Stroke(OverlayColor.Cyan)
        isPedestrian && isBicycleOk ->
            OverlayStyle.Stroke(OverlayColor.Aquamarine)
        isPedestrian ->
            OverlayStyle.Stroke(OverlayColor.Black)
        else ->
            null
    }
}

private val cyclewayTaggingNotExpectedFilter by lazy { """
    ways with
      highway ~ track|living_street|pedestrian|service|motorway_link|motorway|busway
      or motorroad = yes
      or expressway = yes
      or maxspeed <= 20
      or cyclestreet = yes
      or bicycle_road = yes
      or bicycle = no
      or surface ~ ${UNPAVED_SURFACES.joinToString("|")}
      or ~"${MAX_SPEED_TYPE_KEYS.joinToString("|")}" ~ ".*:(zone)?:?([1-9]|[1-2][0-9]|30)"
""".toElementFilterExpression() }

private val cyclewayTaggingInContraflowNotExpectedFilter by lazy { """
    ways with
      dual_carriageway = yes
      or highway ~ primary_link|secondary_link|tertiary_link
      or junction ~ roundabout|circular
""".toElementFilterExpression() }

private fun cyclewayTaggingNotExpected(
    element: Element,
    isRightSide: Boolean,
    isLeftHandTraffic: Boolean
): Boolean =
    cyclewayTaggingNotExpectedFilter.matches(element)
    || isPrivateOnFoot(element)
    || (
        isInContraflowOfOneway(element.tags, Direction.getDefault(isRightSide, isLeftHandTraffic))
        && cyclewayTaggingInContraflowNotExpectedFilter.matches(element)
    )

private fun Cycleway?.getStyle(
    countryInfo: CountryInfo,
    isNoCyclewayExpected: () -> Boolean,
): OverlayStyle.Stroke = when (this) {
    TRACK ->
        OverlayStyle.Stroke(OverlayColor.Blue)

    EXCLUSIVE_LANE, UNSPECIFIED_LANE ->
        if (isAmbiguous(countryInfo)) {
            OverlayStyle.Stroke(OverlayColor.Red)
        } else {
            OverlayStyle.Stroke(OverlayColor.Gold)
        }

    ADVISORY_LANE, SUGGESTION_LANE, UNSPECIFIED_SHARED_LANE ->
        if (isAmbiguous(countryInfo)) {
            OverlayStyle.Stroke(OverlayColor.Red)
        } else {
            OverlayStyle.Stroke(OverlayColor.Orange)
        }

    PICTOGRAMS ->
        OverlayStyle.Stroke(OverlayColor.Orange, dashed = true)

    BUSWAY ->
        OverlayStyle.Stroke(OverlayColor.Lime, dashed = true)

    SIDEWALK_EXPLICIT ->
        OverlayStyle.Stroke(OverlayColor.Cyan, dashed = false)

    NONE ->
        OverlayStyle.Stroke(OverlayColor.Black)

    SHOULDER, NONE_NO_ONEWAY ->
        OverlayStyle.Stroke(OverlayColor.Black, dashed = true)

    SEPARATE ->
        OverlayStyle.Stroke(OverlayColor.Invisible)

    SIDEWALK_OK ->
        OverlayStyle.Stroke(OverlayColor.Aquamarine, dashed = true)

    UNKNOWN, INVALID, UNKNOWN_LANE, UNKNOWN_SHARED_LANE ->
        OverlayStyle.Stroke(OverlayColor.Red)

    null ->
         OverlayStyle.Stroke(if (isNoCyclewayExpected()) OverlayColor.Invisible else OverlayColor.Red)
}
