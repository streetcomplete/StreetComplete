package de.westnordost.streetcomplete.quests.sanitary_dump_station_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBikeParkingCover : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
         amenity = sanitary_dump_station
         and !fee
    """
    override val changesetComment = "Specify if sanitary dump station requires a fee"
    override val wikiLink = "Tag:amenity=sanitary_dump_station"
    override val icon = R.drawable.ic_quest_fuel_self_service
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sanitary_dump_station_fee_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = sanitary_dump_station")

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["fee"] = answer.toYesNo()
    }
}
