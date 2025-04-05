package de.westnordost.streetcomplete.quests.building_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddBuildingMaterial : OsmFilterQuestType<BuildingMaterial>() {

    override val elementFilter = """
        ways, relations with
          ((building and building !~ no|construction|roof|carport)
          or (building:part and building:part !~ no|construction|roof|carport))
          and !building:material
          and indoor != no
          and wall != no
    """
    override val changesetComment = "Specify building material"
    override val wikiLink = "Key:building:material"
    override val icon = R.drawable.ic_quest_building_material

    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = when {
        tags.containsKey("building:part") -> R.string.quest_buildingPartMaterial_title
        else -> R.string.quest_buildingMaterial_title
    }

    override fun createForm() = AddBuildingMaterialForm()

    override fun applyAnswerTo(
        answer: BuildingMaterial,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["building:material"] = answer.osmValue
    }
}
