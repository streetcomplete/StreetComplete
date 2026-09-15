package de.westnordost.streetcomplete.quests.socket

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_charger_socket
import de.westnordost.streetcomplete.resources.quest_charging_station_socket_title
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddChargingStationSocket : OsmElementQuestType<Map<ChargingStationSocket, Int>> {

    private val filter by lazy { """
        nodes, ways with
          amenity = charging_station
          and motorcar != no
          and motor_vehicle != no
          and lockable != yes
          and (
            !~"socket:(${ChargingStationSocket.entries.joinToString("|") { it.osmId }})"
            or ~"socket:(${ChargingStationSocket.entries.joinToString("|") { it.osmId }})" ~ "yes"
            or ~"socket:(${INVALID_CHARGING_STATION_SOCKETS.joinToString("|")})"
            or socket older today -2 years
          )
          and access !~ no|private
    """.toElementFilterExpression() }

    override val changesetComment = "Specify charging station sockets"
    override val wikiLink = "Key:socket"
    override val icon = Res.drawable.quest_charger_socket
    override val title = Res.string.quest_charging_station_socket_title
    override val achievements = listOf(CAR)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // Individual charge points are nodes per OSM wiki; not quested yet (follow-up).
        val chargePointCenters = mapData
            .filter("nodes with man_made = charge_point")
            .mapNotNull { (it as? Node)?.position }
            .toList()

        return mapData
            .filter(filter)
            .filter { !it.containsMappedChargePoints(mapData, chargePointCenters)
        }.toList()
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        return when (element) {
            is Node -> true
            // ways: need geometry for country + contained charge_point check
            else -> null
        }
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = charging_station or man_made = charge_point")

    @Composable
    override fun Form(
        on: (QuestAction<Map<ChargingStationSocket, Int>>) -> Unit,
        element: Element,
        geometry: ElementGeometry,
        countryInfo: CountryInfo
    ) {
        AddChargingStationSocketForm(on, element, countryInfo)
    }

    override fun applyAnswerTo(
        answer: Map<ChargingStationSocket, Int>,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        // remove deprecated/ambiguous keys
        for (socket in INVALID_CHARGING_STATION_SOCKETS) {
            tags.remove("socket:$socket")
        }
        // update sockets chosen and delete those not chosen
        for (socket in ChargingStationSocket.entries) {
            val key = "socket:${socket.osmId}"
            if (socket in answer) {
                val count = answer[socket] ?: 0
                // count > 0 -> numeric; count = 0 -> explicit "no" (not unknown).
                tags[key] = if (count > 0) count.toString() else "no"
            } else {
                // however, do not delete an explicit "no" or "0"
                if (tags[key] != "no" && tags[key] != "0") {
                    tags.remove(key)
                }
            }
        }

        // Every completed survey refreshes check_date:socket so the quest stays suppressed
        tags.updateCheckDateForKey("socket")
    }
}

private fun Element.containsMappedChargePoints(
    mapData: MapDataWithGeometry,
    chargePointCenters: List<LatLon>
): Boolean {
    if (this !is Way || chargePointCenters.isEmpty()) return false
    val geometry = mapData.getGeometry(type, id) as? ElementPolygonsGeometry ?: return false
    return chargePointCenters.any { pos ->
        geometry.bounds.contains(pos) && pos.isInMultipolygon(geometry.polygons)
    }
}
