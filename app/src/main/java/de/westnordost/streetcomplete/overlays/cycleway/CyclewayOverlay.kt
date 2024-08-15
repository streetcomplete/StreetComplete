package de.westnordost.streetcomplete.overlays.cycleway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_boulevard.parseBicycleBoulevard
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.cycleway.isAmbiguous
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.parseSeparateCycleway
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.quests.cycleway.AddCycleway

class CyclewayOverlay(
    private val getCountryInfoByLocation: (location: LatLon) -> CountryInfo,
) : Overlay {

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
    PolylineStyle(StrokeStyle(parseSeparateCycleway(element.tags).getColor()))

private fun SeparateCycleway?.getColor() = when (this) {
    SeparateCycleway.NOT_ALLOWED,
    SeparateCycleway.NON_DESIGNATED_ON_FOOTWAY,
    SeparateCycleway.PATH ->
        Color.BLACK

    SeparateCycleway.NON_SEGREGATED ->
        Color.CYAN

    SeparateCycleway.SEGREGATED,
    SeparateCycleway.EXCLUSIVE,
    SeparateCycleway.EXCLUSIVE_WITH_SIDEWALK ->
        Color.BLUE
    SeparateCycleway.ALLOWED_ON_FOOTWAY ->
        Color.AQUAMARINE

    null ->
        Color.INVISIBLE
}

private fun getStreetCyclewayStyle(element: Element, countryInfo: CountryInfo): PolylineStyle {
    val cycleways = parseCyclewaySides(element.tags, countryInfo.isLeftHandTraffic)
    val isBicycleBoulevard = parseBicycleBoulevard(element.tags) == BicycleBoulevard.YES
    val isNoCyclewayExpected = lazy { cyclewayTaggingNotExpected(element) || isPrivateOnFoot(element) }

    return PolylineStyle(
        stroke = if (isBicycleBoulevard) StrokeStyle(Color.GOLD, dashed = true) else null,
        strokeLeft = cycleways?.left?.cycleway.getStyle(countryInfo, isNoCyclewayExpected),
        strokeRight = cycleways?.right?.cycleway.getStyle(countryInfo, isNoCyclewayExpected)
    )
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
      or ~"${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")}" ~ ".*:(zone)?:?([1-9]|[1-2][0-9]|30)"
""".toElementFilterExpression() }

private fun cyclewayTaggingNotExpected(element: Element) =
    cyclewayTaggingNotExpectedFilter.matches(element)

private fun Cycleway?.getStyle(
    countryInfo: CountryInfo,
    isNoCyclewayExpected: Lazy<Boolean>,
): StrokeStyle = when (this) {
    TRACK ->
        StrokeStyle(Color.BLUE)

    EXCLUSIVE_LANE, UNSPECIFIED_LANE ->
        if (isAmbiguous(countryInfo)) {
            StrokeStyle(Color.DATA_REQUESTED)
        } else {
            StrokeStyle(Color.GOLD)
        }

    ADVISORY_LANE, SUGGESTION_LANE, UNSPECIFIED_SHARED_LANE ->
        if (isAmbiguous(countryInfo)) {
            StrokeStyle(Color.DATA_REQUESTED)
        } else {
            StrokeStyle(Color.ORANGE)
        }

    PICTOGRAMS ->
        StrokeStyle(Color.ORANGE, dashed = true)

    BUSWAY ->
        StrokeStyle(Color.LIME, dashed = true)

    SIDEWALK_EXPLICIT ->
        StrokeStyle(Color.CYAN, dashed = false)

    NONE ->
        StrokeStyle(Color.BLACK)

    SHOULDER, NONE_NO_ONEWAY ->
        StrokeStyle(Color.BLACK, dashed = true)

    SEPARATE ->
        StrokeStyle(Color.INVISIBLE)

    SIDEWALK_OK ->
        StrokeStyle(Color.CYAN, dashed = true)

    UNKNOWN, INVALID, UNKNOWN_LANE, UNKNOWN_SHARED_LANE ->
        StrokeStyle(Color.DATA_REQUESTED)

    null ->
         StrokeStyle(if (isNoCyclewayExpected.value) Color.INVISIBLE else Color.DATA_REQUESTED)
}
