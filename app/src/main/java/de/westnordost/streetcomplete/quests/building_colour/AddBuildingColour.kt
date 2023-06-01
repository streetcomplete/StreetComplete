package de.westnordost.streetcomplete.quests.building_colour

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddBuildingColour : OsmFilterQuestType<BuildingColour>() {

    override val elementFilter = """
        ways, relations with
          ((building and building !~ no|construction)
          or (building:part and building:part !~ no|construction))
          and !building:colour
    """
    override val changesetComment = "Add building colour"
    override val wikiLink = "Key:building:colour"
    override val icon = R.drawable.ic_quest_building_colour
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = when {
        tags.containsKey("building:part") -> R.string.quest_buildingPartColour_title
        else -> R.string.quest_buildingColour_title
    }

    override fun createForm() = AddBuildingColourForm()

    override fun applyAnswerTo(
        answer: BuildingColour,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["building:colour"] = answer.osmValue
    }
}
