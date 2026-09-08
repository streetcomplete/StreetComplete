package de.westnordost.streetcomplete.quests.charging_station_socket

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
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddChargingStationSocket : OsmElementQuestType<Map<SocketType, Int>> {

    private val filter by lazy { """
        nodes, ways with
          amenity = charging_station
          and motorcar != no
          and motor_vehicle != no
          and access !~ private|no
    """.toElementFilterExpression() }

    override val changesetComment = "Specify charging station sockets"
    override val wikiLink = "Key:socket"
    override val icon = Res.drawable.quest_charger_socket
    override val title = Res.string.quest_charging_station_socket_title
    override val achievements = listOf(CAR)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val chargePointCenters = mapData
            .filter("nodes, ways with man_made = charge_point")
            .mapNotNull { it.center(mapData) }
            .toList()

        return mapData.filter(filter).filter { element ->
            element.needsSocketSurvey()
                && !element.containsMappedChargePoints(mapData, chargePointCenters)
        }.toList()
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element) || !element.needsSocketSurvey()) return false
        return when (element) {
            // DOMESTIC is always offered, so the quest is never gated on country metadata alone
            is Node -> true
            else -> null
        }
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = charging_station or man_made = charge_point")

    @Composable
    override fun Form(
        on: (QuestAction<Map<SocketType, Int>>) -> Unit,
        element: Element,
        geometry: ElementGeometry,
        countryInfo: CountryInfo
    ) {
        AddChargingStationSocketForm(
            on = on,
            element = element,
            socketTypes = socketTypesForCountry(countryInfo),
        )
    }

    override fun applyAnswerTo(
        answer: Map<SocketType, Int>,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        tags.keys.filter { isDeprecatedSocketKey(it) }.toList().forEach { tags.remove(it) }

        for ((type, count) in answer) {
            val key = type.osmCountKey
            if (count > 0) {
                tags[key] = count.toString()
            } else {
                tags.remove(key)
            }
        }

        val type2Count = answer[SocketType.TYPE2] ?: 0
        val type2CableCount = answer[SocketType.TYPE2_CABLE] ?: 0
        if (type2Count > 0 && SocketType.TYPE2 in answer && type2CableCount <= 0) {
            tags[SocketType.TYPE2_CABLE.osmCountKey] = "no"
        }

        if (!tags.hasChanges || tags.hasCheckDateForKey("socket")) {
            tags.updateCheckDateForKey("socket")
        }
    }
}

private val managedSocketCountKeys = SocketType.entries.map { it.osmCountKey }.toSet()

private fun isDeprecatedSocketKey(key: String): Boolean =
    key.startsWith("socket:tesla") ||
        key == "socket:css" ||
        key == "socket:unknown" ||
        key == "socket:type"

private fun Element.needsSocketSurvey(): Boolean {
    if (tags.keys.any { isDeprecatedSocketKey(it) }) return true
    if (managedSocketCountKeys.any { tags[it] == "yes" }) return true
    if (managedSocketCountKeys.any { tags[it]?.toIntOrNull() != null }) return false
    return true
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

private fun Element.center(mapData: MapDataWithGeometry): LatLon? = when (this) {
    is Node -> position
    else -> mapData.getGeometry(type, id)?.center
}
