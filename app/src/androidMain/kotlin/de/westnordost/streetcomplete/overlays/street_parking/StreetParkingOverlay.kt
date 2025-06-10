package de.westnordost.streetcomplete.overlays.street_parking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.AndroidOverlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.maxspeed.MAX_SPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.OFF_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STAGGERED_HALF_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STAGGERED_ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.parseStreetParkingSides
import de.westnordost.streetcomplete.osm.traffic_calming.LaneNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.traffic_calming.parseNarrowingTrafficCalming
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm

class StreetParkingOverlay : Overlay, AndroidOverlay {

    override val title = R.string.overlay_street_parking
    override val icon = R.drawable.ic_quest_parking_lane
    override val changesetComment = "Specify whether there is street parking and what kind"
    override val wikiLink: String = "Key:parking:lane"
    override val achievements = listOf(CAR)
    override val isCreateNodeEnabled = true

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, OverlayStyle>> =
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
      or tunnel = yes
      or junction = roundabout
      or ~"${MAX_SPEED_TYPE_KEYS.joinToString("|")}" ~ ".*:(rural|nsl_single|nsl_dual)"
      or maxspeed >= 70
""".toElementFilterExpression() }

private val parkingLotAreaStyle = OverlayStyle.Polygon(OverlayColor.Blue)
private val parkingLotPointStyle = OverlayStyle.Point(R.drawable.preset_temaki_car_parked)
private val chicaneStyle = OverlayStyle.Point(R.drawable.preset_temaki_chicane_arrow)
private val trafficCalmingStyle = OverlayStyle.Point(R.drawable.preset_temaki_diamond)

private fun getNarrowingTrafficCalmingStyle(element: Element): OverlayStyle? =
    when (parseNarrowingTrafficCalming(element.tags)) {
        LaneNarrowingTrafficCalming.CHICANE -> chicaneStyle
        null -> null
        else -> trafficCalmingStyle
    }

private fun getStreetParkingStyle(element: Element): OverlayStyle {
    val parking = parseStreetParkingSides(element.tags)
    // not set but private or not expected to have a sidewalk -> do not highlight as missing
    if (parking == null) {
        if (isPrivateOnFoot(element) || streetParkingTaggingNotExpected.matches(element)) {
            return OverlayStyle.Polyline(OverlayStyle.Stroke(OverlayColor.Invisible))
        }
    }

    return OverlayStyle.Polyline(
        stroke = null,
        strokeLeft = parking?.left.style,
        strokeRight = parking?.right.style
    )
}

private val ParkingPosition.isDashed: Boolean get() = when (this) {
    STREET_SIDE, PAINTED_AREA_ONLY, STAGGERED_ON_STREET, STAGGERED_HALF_ON_STREET -> true
    else -> false
}

private val ParkingPosition.color get() = when (this) {
    ON_STREET, PAINTED_AREA_ONLY, STAGGERED_ON_STREET ->
        OverlayColor.Gold
    HALF_ON_STREET, STAGGERED_HALF_ON_STREET ->
        OverlayColor.Aquamarine
    OFF_STREET, STREET_SIDE ->
        OverlayColor.Blue
}

private val StreetParking?.style: OverlayStyle.Stroke get() = when (this) {
    is StreetParking.PositionAndOrientation ->
                                OverlayStyle.Stroke(position.color, position.isDashed)

    StreetParking.None ->       OverlayStyle.Stroke(OverlayColor.Black)

    StreetParking.Separate ->   OverlayStyle.Stroke(OverlayColor.Invisible)

    StreetParking.Unknown,
    StreetParking.Incomplete,
    null ->                     OverlayStyle.Stroke(OverlayColor.Red)
}
