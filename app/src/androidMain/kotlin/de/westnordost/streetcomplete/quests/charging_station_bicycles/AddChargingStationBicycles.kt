package de.westnordost.streetcomplete.quests.charging_station_bicycles

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags

class AddChargingStationBicycles : OsmFilterQuestType<ChargingStationBicycles>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
          amenity = charging_station
          and !bicycle
          and access !~ private|no
    """
    override val changesetComment = "Specify whether bicycles can be charged at charging stations"
    override val wikiLink = "Tag:amenity=charging_station"
    override val icon = R.drawable.quest_bicycle_charger
    override val isDeleteElementEnabled = true
    override val achievements = listOf(BICYCLIST)
    override val hint = R.string.quest_charging_station_bicycles_hint

    override fun getTitle(tags: Map<String, String>) = R.string.quest_charging_station_bicycles_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = charging_station")

    override fun createForm() = AddChargingStationBicyclesForm()

    override fun applyAnswerTo(answer: ChargingStationBicycles, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            ChargingStationBicycles.YES -> {
                tags["bicycle"] = "yes"
            }
            ChargingStationBicycles.NO -> {
                tags["bicycle"] = "no"
            }
            ChargingStationBicycles.ONLY -> {
                tags["bicycle"] = "yes"
                tags["motorcar"] = "no"
            }
        }
    }
}
