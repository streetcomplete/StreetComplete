package de.westnordost.streetcomplete.quests.charging_station_socket

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags

class AddChargingStationSocket :
    OsmElementQuestType<Map<SocketType, Int>>,
    AndroidQuest {

    private val filter by lazy {
        """
        nodes, ways with
          amenity = charging_station
          and bicycle != yes
          and motorcar != no
        """.toElementFilterExpression()
    }

    override val enabledInCountries = NoCountriesExcept(
        "AT","BE","BG","HR","CY","CZ","DE","DK","EE","ES","FI","FR","GB",
        "GR","HU","IE","IS","IT","LI","LT","LU","LV","MT","NL","NO",
        "PL","PT","RO","SE","SI","SK"
    )

    override val changesetComment = "Specify charging station sockets"
    override val wikiLink = "Key:socket"
    override val icon = R.drawable.quest_charger_socket
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_charging_station_socket_title

    override fun createForm() = AddChargingStationSocketForm()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean {
        if (!filter.matches(element)) return false

        // Only show if NO socket:* exists
        if (element.tags.keys.any { it.startsWith("socket:") }) {
            return false
        }

        return true
    }

    override fun applyAnswerTo(
        answer: Map<SocketType, Int>,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {

        answer.forEach { (type, count) ->
            if (count > 0) {
                tags["socket:${type.osmKey}"] = count.toString()
            }
        }

        // Special rule:
        // If type2 > 0 AND type2_cable == 0 → explicitly tag no
        val type2 = answer[SocketType.TYPE2] ?: 0
        val cable = answer[SocketType.TYPE2_CABLE] ?: 0

        if (type2 > 0 && cable == 0) {
            tags["socket:type2_cable"] = "no"
        }
    }
}
