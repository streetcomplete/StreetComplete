package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddServiceBuildingType : OsmFilterQuestType<String>() {

    override val elementFilter = """
        ways, relations with
          building = service
          and !power
          and !service
          and !man_made
          and !substation
          and !pipeline
    """
    override val changesetComment = "Add service building type"
    override val wikiLink = "Tag:building=service"
    override val icon = R.drawable.ic_quest_service_building
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_service_building_type_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> {
        val title = tags["operator"]?.let { " ($it)" } ?: ""
        return arrayOf(title)
    }

    override fun createForm() = AddServiceBuildingTypeForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            "substation" -> {
                tags["power"] = "substation"
                tags["substation"] = "minor_distribution"
            }
            "gas" -> {
                tags["pipeline"] = "substation"
                tags["substation"] = "distribution"
                tags["substance"] = "gas"
            }
            "water_well", "reservoir_covered", "pumping_station" -> {
                tags["man_made"] = answer
            }
        }
    }
}
