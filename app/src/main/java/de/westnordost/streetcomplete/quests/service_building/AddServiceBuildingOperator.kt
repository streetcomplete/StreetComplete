package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddServiceBuildingOperator : OsmFilterQuestType<String>() {

    override val elementFilter = """
        ways, relations with
          building = service
          and !operator
          and !name
          and !brand
    """
    override val changesetComment = "Add service building operator"
    override val wikiLink = "Tag:building=service"
    override val icon = R.drawable.ic_quest_service_building
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_service_building_operator_title

    override fun createForm() = AddServiceBuildingOperatorForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["operator"] = answer
    }
}
