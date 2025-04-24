package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddServiceBuildingType : OsmFilterQuestType<ServiceBuildingType>() {

    override val elementFilter = """
        ways, relations with
          building ~ service|transformer_tower
          and !power and !disused:power and !abandoned:power and !was:power and !construction:power
          and !service and !disused:service and !abandoned:service and !was:service and !construction:service
          and !man_made and !disused:man_made and !abandoned:man_made and !was:man_made and !construction:man_made
          and !substation and !disused:substation and !abandoned:substation and !was:substation and !construction:substation
          and !pipeline and !disused:pipeline and !abandoned:pipeline and !was:pipeline and !construction:pipeline
          and !utility and !disused:utility and !abandoned:utility and !was:utility and !construction:utility
          and !railway and !disused:railway and !abandoned:railway and !was:railway and !construction:railway
          and disused != yes and abandoned != yes and !construction
    """
    override val changesetComment = "Add service building type"
    override val wikiLink = "Tag:building=service"
    override val icon = R.drawable.ic_quest_service_building
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_service_building_type_title

    override fun createForm() = AddServiceBuildingTypeForm()

    override fun applyAnswerTo(answer: ServiceBuildingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.tags.forEach { tags[it.first] = it.second }
        if (answer == ServiceBuildingType.VENTILATION_SHAFT || answer == ServiceBuildingType.RAILWAY_VENTILATION_SHAFT)
            tags.remove("building") // see https://wiki.openstreetmap.org/wiki/Tag:man_made%3Dventilation_shaft
    }
}
