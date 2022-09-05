package de.westnordost.streetcomplete.quests.street_parking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.street_parking.LeftAndRightStreetParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPositionAndOrientation
import de.westnordost.streetcomplete.osm.street_parking.toOsmConditionValue
import de.westnordost.streetcomplete.osm.street_parking.toOsmLaneValue
import de.westnordost.streetcomplete.osm.street_parking.toOsmValue

class AddStreetParking : OsmFilterQuestType<LeftAndRightStreetParking>() {

    override val elementFilter = """
        ways with
          (
            highway ~ residential|living_street
            or (
              highway ~ primary|secondary|tertiary|unclassified
              and (
                sidewalk ~ both|left|right|yes|separate
                or ~${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")} ~ .*urban|.*zone.*
                or maxspeed <= 60
              )
            )
          )
          and !parking:lane and !parking:lane:left and !parking:lane:right and !parking:lane:both
          and !parking:condition and !parking:condition:left and !parking:condition:right and !parking:condition:both
          and area != yes
          and motorroad != yes
          and tunnel != yes
          and bridge != yes
          and priority_road !~ designated|yes
          and overtaking !~ no|forward|backward
          and junction != roundabout
          and !turn:lanes and !turn:lanes:forward and !turn:lanes:backward and !turn:lanes:both_ways
          and (
            access !~ private|no
            or foot and foot !~ private|no
          )
    """
    /* On some roads, usually no-parking rules apply implicitly, so these are filtered out:
       - motorways, trunks (motorroads), pedestrian zones,
       - often priority roads (at least rural ones), roads where overtaking is forbidden
         (continuous center line)
       - roundabouts
       - on sections of the roadway marked with arrows (turn lanes)

       There are some more rules that cannot be filtered due to the lack of tags that are
       set on the road-way:
       - in front of important signs (STOP, saltires, yield etc)
       - at taxi stands, bus stops, ...
       - on and near crossings, level crossings, ... on tram tracks (duh!) etc
       - at narrow points, sharp bends, fire rescue paths and other dangerous points
       - at entries to driveways and other places where there is a dropped kerb
         (but I don't think street parking will/should be mapped at that level of detail)
       - in some country: in front of police stations, post offices, hospitals...
       - etc

       Further, to ask outside of urban areas does not really make sense, so we fuzzily exclude
       roads that are probably outside of settlements (similar idea like for AddWayLit)
      */

    override val changesetComment = "Specify whether and how cars park on roads"
    override val wikiLink = "Key:parking:lane"
    override val icon = R.drawable.ic_quest_parking_lane
    override val achievements = listOf(CAR)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = R.string.quest_street_parking_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("ways with amenity = parking") +
        getMapData().filter("nodes with traffic_calming ~ choker|chicane|island|choked_island|choked_table")

    override fun createForm() = AddStreetParkingForm()

    override fun applyAnswerTo(answer: LeftAndRightStreetParking, tags: Tags, timestampEdited: Long) {
        /* Note: If a resurvey is implemented, old
           parking:lane:*:(parallel|diagonal|perpendicular|...) values must be cleaned up */

        // parking:lane:<left/right/both>
        val laneRight = answer.right!!.toOsmLaneValue() ?: throw IllegalArgumentException()
        val laneLeft = answer.left!!.toOsmLaneValue() ?: throw IllegalArgumentException()

        if (laneLeft == laneRight) {
            tags["parking:lane:both"] = laneLeft
        } else {
            tags["parking:lane:left"] = laneLeft
            tags["parking:lane:right"] = laneRight
        }

        // parking:condition:<left/right/both>
        val conditionRight = answer.right.toOsmConditionValue()
        val conditionLeft = answer.left.toOsmConditionValue()

        if (conditionLeft == conditionRight) {
            conditionLeft?.let { tags["parking:condition:both"] = it }
        } else {
            conditionLeft?.let { tags["parking:condition:left"] = it }
            conditionRight?.let { tags["parking:condition:right"] = it }
        }

        // parking:lane:<left/right/both>:<parallel/diagonal/perpendicular>
        val positionRight = (answer.right as? StreetParkingPositionAndOrientation)?.position?.toOsmValue()
        val positionLeft = (answer.left as? StreetParkingPositionAndOrientation)?.position?.toOsmValue()

        if (laneLeft == laneRight && positionLeft == positionRight) {
            if (positionLeft != null) tags["parking:lane:both:$laneLeft"] = positionLeft
        } else {
            if (positionLeft != null) tags["parking:lane:left:$laneLeft"] = positionLeft
            if (positionRight != null) tags["parking:lane:right:$laneRight"] = positionRight
        }
    }
}
