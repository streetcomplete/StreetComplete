package de.westnordost.streetcomplete.quests.charging_station_socket

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.math.contains

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

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return mapData
            .filter { element -> filter.matches(element) }
            .filter { element -> isApplicableTo(element, mapData) }
            .asIterable()
    }

    override fun isApplicableTo(element: Element): Boolean? {
        // This variant must exist because OsmElementQuestType requires it.
        // But we delegate real logic to the overloaded version.
        return null
    }

    private fun isApplicableTo(
        element: Element,
        mapData: MapDataWithGeometry
    ): Boolean {

        if (!filter.matches(element)) return false

        // Skip charge_point nodes completely
        if (element.tags["man_made"] == "charge_point") return false

        // Skip charging_station areas that contain charge_points
        if (element is Way) {

            val geometry = mapData.getGeometry(element.type, element.id)
                ?: return true

            val bounds = geometry.bounds

            val hasChargePointsInside = mapData
                .filter { it.tags["man_made"] == "charge_point" }
                .any { cp ->
                    val cpGeometry = mapData.getGeometry(cp.type, cp.id)
                        ?: return@any false

                    bounds.contains(cpGeometry.center)
                }

            if (hasChargePointsInside) return false
        }

        val socketTags = element.tags
            .filterKeys { it.startsWith("socket:") }

        if (socketTags.isEmpty()) return true
        if (socketTags.keys.any { isDeprecatedSocketKey(it) }) return true
        if (socketTags.values.any { it == "yes" }) return true

        return false
    }

    override fun applyAnswerTo(
        answer: Map<SocketType, Int>,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {

        // Cleanup deprecated keys
        tags.keys
            .filter { isDeprecatedSocketKey(it) }
            .toList()
            .forEach { tags.remove(it) }

        // Remove old socket:* keys
        tags.keys
            .filter { it.startsWith("socket:") }
            .toList()
            .forEach { tags.remove(it) }

        // Apply new values
        answer.forEach { (type, count) ->
            tags["socket:${type.osmKey}"] = count.toString()
        }

        // type2/type2_cable=no logic
        if (answer.containsKey(SocketType.TYPE2)
            && !answer.containsKey(SocketType.TYPE2_CABLE)
        ) {
            tags["socket:type2_cable"] = "no"
        }
    }

    private fun isDeprecatedSocketKey(key: String): Boolean =
        key.startsWith("socket:tesla") ||
            key == "socket:css" ||
            key == "socket:unknown" ||
            key == "socket:type"
}
