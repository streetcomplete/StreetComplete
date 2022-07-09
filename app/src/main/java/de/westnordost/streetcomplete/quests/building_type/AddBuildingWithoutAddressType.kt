package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddBuildingWithAddressType : OsmElementQuestType<BuildingType> {

    override fun isApplicableTo(element: Element): Boolean? =
        if (!buildingFilter.matches(element)) false else null

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val buildings = mapData.filter { buildingFilter.matches(it) }
        val buildingsWithoutAddress = getBuildingsWithoutAddress(buildings, mapData)
        return buildings.filterNot { it in buildingsWithoutAddress }
    }

    override val changesetComment = "Add building types"
    override val wikiLink = "Key:building"
    override val icon = R.drawable.ic_quest_building_has_address

    override fun getTitle(tags: Map<String, String>) = R.string.quest_buildingType_has_address_title

    override fun createForm() = AddBuildingTypeForm()

    override fun applyAnswerTo(answer: BuildingType, tags: Tags, timestampEdited: Long) {
        applyBuildingAnswer(answer, tags, timestampEdited)
    }
}
