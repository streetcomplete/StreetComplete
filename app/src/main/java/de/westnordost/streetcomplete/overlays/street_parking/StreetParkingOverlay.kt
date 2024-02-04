package de.westnordost.streetcomplete.overlays.street_parking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.parseNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.street_parking.IncompleteStreetParking
import de.westnordost.streetcomplete.osm.street_parking.NoStreetParking
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.OFF_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STAGGERED_HALF_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STAGGERED_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPositionAndOrientation
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingSeparate
import de.westnordost.streetcomplete.osm.street_parking.UnknownStreetParking
import de.westnordost.streetcomplete.osm.street_parking.parseStreetParkingSides
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style

class StreetParkingOverlay : Overlay {

    override val title = R.string.overlay_street_parking
    override val icon = R.drawable.ic_quest_parking_lane
    override val changesetComment = "Specify whether there is street parking and what kind"
    override val wikiLink: String = "Key:parking:lane"
    override val achievements = listOf(CAR)
    override val isCreateNodeEnabled = true

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> =
        // roads
        mapData.filter("""
            ways with highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|pedestrian|service
            and area != yes
        """).map { it to getStreetParkingStyle(it) } +
        // separate parking
        mapData.filter("""
            nodes, ways, relations with
            amenity = parking
        """).map { it to if (it is Node) parkingLotPointStyle else parkingLotAreaStyle } +
        // chokers
        mapData.filter("""
            nodes with
            traffic_calming ~ "(choker|chicane|island|choked_.*)"
            or crossing:island = yes
        """).mapNotNull {
            val style = getNarrowingTrafficCalmingStyle(it)
            if (style != null) it to style else null
        }

    override fun createForm(element: Element?): AbstractOverlayForm? =
        if (element != null && element.tags["highway"] in ALL_ROADS && element.tags["area"] != "yes") {
            StreetParkingOverlayForm()
        } else if (element == null || parseNarrowingTrafficCalming(element.tags) != null) {
            LaneNarrowingTrafficCalmingForm()
        } else {
            null
        }
}

private val streetParkingTaggingNotExpected by lazy { """
    ways with
      highway ~ service|pedestrian
      or motorroad = yes
      or expressway = yes
      or junction = roundabout
      or ~"${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")}" ~ ".*:(rural|nsl_single|nsl_dual)"
      or maxspeed >= 70
""".toElementFilterExpression() }

private val parkingLotAreaStyle = PolygonStyle(Color.BLUE)
private val parkingLotPointStyle = PointStyle("ic_preset_temaki_car_parked")
private val chicaneStyle = PointStyle("ic_preset_temaki_chicane_arrow")
private val trafficCalmingStyle = PointStyle("ic_preset_temaki_diamond")

private fun getNarrowingTrafficCalmingStyle(element: Element): Style? =
    when (parseNarrowingTrafficCalming(element.tags)) {
        LaneNarrowingTrafficCalming.CHICANE -> chicaneStyle
        null -> null
        else -> trafficCalmingStyle
    }

private fun getStreetParkingStyle(element: Element): Style {
    val parking = parseStreetParkingSides(element.tags)
    // not set but private or not expected to have a sidewalk -> do not highlight as missing
    if (parking == null) {
        if (isPrivateOnFoot(element) || streetParkingTaggingNotExpected.matches(element)) {
            return PolylineStyle(StrokeStyle(Color.INVISIBLE))
        }
    }

    return PolylineStyle(
        stroke = null,
        strokeLeft = parking?.left.style,
        strokeRight = parking?.right.style
    )
}

private val ParkingPosition.isDashed: Boolean get() = when (this) {
    STREET_SIDE, PAINTED_AREA_ONLY, STAGGERED_ON_STREET, STAGGERED_HALF_ON_STREET -> true
    else -> false
}

private val ParkingPosition.color: String get() = when (this) {
    ON_STREET, PAINTED_AREA_ONLY, STAGGERED_ON_STREET ->
        Color.GOLD
    HALF_ON_STREET, STAGGERED_HALF_ON_STREET ->
        Color.AQUAMARINE
    OFF_STREET, STREET_SIDE ->
        Color.BLUE
}

private val StreetParking?.style: StrokeStyle get() = when (this) {
    is StreetParkingPositionAndOrientation ->
                                StrokeStyle(position.color, position.isDashed)

    NoStreetParking ->          StrokeStyle(Color.BLACK)

    StreetParkingSeparate ->    StrokeStyle(Color.INVISIBLE)

    UnknownStreetParking,
    IncompleteStreetParking,
    null ->                     StrokeStyle(Color.DATA_REQUESTED)
}
