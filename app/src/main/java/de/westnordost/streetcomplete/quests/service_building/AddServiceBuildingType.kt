package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddServiceBuildingType : OsmFilterQuestType<ServiceBuildingType>() {

    override val elementFilter = """
        ways, relations with
          building = service
          and !power
          and !service
          and !man_made
          and !substation
          and !pipeline
          and !utility
          and !railway
    """
    override val changesetComment = "Add service building type"
    override val wikiLink = "Tag:building=service"
    override val icon = R.drawable.ic_quest_service_building
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_service_building_type_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> {
        val operator = tags["operator"]?.let { " ($it)" } ?: ""
        return arrayOf(operator)
    }

    override fun createForm() = AddServiceBuildingTypeForm()

    override fun applyAnswerTo(answer: ServiceBuildingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.tags.forEach { tags[it.first] = it.second }
        if (answer == ServiceBuildingType.VENTILATION_SHAFT || ServiceBuildingType.RAILWAY_VENTILATION_SHAFT)
            tags.remove("building") // see https://wiki.openstreetmap.org/wiki/Tag:man_made%3Dventilation_shaft
    }
}
