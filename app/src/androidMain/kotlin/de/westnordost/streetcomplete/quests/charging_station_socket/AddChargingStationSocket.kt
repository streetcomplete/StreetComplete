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

        // Exclude if any valid socket:* with numeric value exists
        val hasValidSocket = element.tags.any {
            it.key.startsWith("socket:") && it.value.toIntOrNull() != null
        }

        return !hasValidSocket
    }

    override fun applyAnswerTo(
        answer: Map<SocketType, Int>,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {

        // Cleanup deprecated keys
        tags.keys
            .filter { it.startsWith("socket:tesla") || it == "socket:css" }
            .forEach { tags.remove(it) }

        // Remove old supported socket keys
        SocketType.selectableValues.forEach {
            tags.remove("socket:${it.osmKey}")
        }

        // Apply new values
        answer.forEach { (type, count) ->
            tags["socket:${type.osmKey}"] = count.toString()
        }

        // type2_cable=no logic
        if (answer.containsKey(SocketType.TYPE2) &&
            !answer.containsKey(SocketType.TYPE2_CABLE)
        ) {
            tags["socket:type2_cable"] = "no"
        }
    }
}
