package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddServiceBuildingOperator : OsmFilterQuestType<ServiceBuildingOperatorAnswer>() {

    override val elementFilter = """
        ways, relations with
          building ~ service|transformer_tower
          and !operator
          and !name
          and !brand
          and disused != yes and abandoned != yes and !construction
    """
    override val changesetComment = "Add service building operator"
    override val wikiLink = "Tag:building=service"
    override val icon = R.drawable.ic_quest_service_building
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_service_building_operator_title

    override fun createForm() = AddServiceBuildingOperatorForm()

    override fun applyAnswerTo(answer: ServiceBuildingOperatorAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is ServiceBuildingOperator -> {
                tags["operator"] = answer.name
            }
            is DisusedServiceBuilding -> {
                tags["disused"] = "yes"
                tags.keys.toList().filter { it.matches(Regex("^(power|service|man_made|substation|pipeline|utility|railway)$")) }
                    .forEach {
                        tags["disused:" + it] = tags[it] ?: "yes"
                        tags.remove(it)
                    }
            }
        }
    }
}
