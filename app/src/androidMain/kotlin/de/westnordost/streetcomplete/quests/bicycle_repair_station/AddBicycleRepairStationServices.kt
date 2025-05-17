package de.westnordost.streetcomplete.quests.bicycle_repair_station

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBicycleRepairStationServices : OsmFilterQuestType<List<BicycleRepairStationService>>() {

    override val elementFilter = """
        nodes, ways with
        amenity = bicycle_repair_station
        and (
            !service:bicycle:pump
            or !service:bicycle:stand
            or !service:bicycle:tools
            or !service:bicycle:chain_tool
        )
        and access !~ private|no
    """

    override val changesetComment = "Specify features of bicycle repair stations"
    override val wikiLink = "Tag:amenity=bicycle_repair_station"
    override val icon = R.drawable.ic_quest_bicycle_repair_amenity
    override val isDeleteElementEnabled = true
    override val achievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_repair_station_services_title

    override fun createForm() = AddBicycleRepairStationServicesForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            nodes, ways with
            amenity ~ bicycle_repair_station|compressed_air
        """)

    override fun applyAnswerTo(answer: List<BicycleRepairStationService>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        for (entry in BicycleRepairStationService.entries) {
            tags["service:bicycle:${entry.value}"] = (entry in answer).toYesNo()
        }
    }
}
