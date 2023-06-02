package de.westnordost.streetcomplete.quests.roof_colour

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags

class AddRoofColour : OsmFilterQuestType<RoofColour>() {

    override val elementFilter = """
        ways, relations with
          roof:shape
          and roof:shape != flat
          and !roof:colour
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """
    override val changesetComment = "Specify roof colour"
    override val wikiLink = "Key:roof:colour"
    override val icon = R.drawable.ic_quest_roof_colour
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_roofColour

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofColour_title

    override fun createForm() = AddRoofColourForm()

    override fun applyAnswerTo(
        answer: RoofColour,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["roof:colour"] = answer.osmValue
    }
}
