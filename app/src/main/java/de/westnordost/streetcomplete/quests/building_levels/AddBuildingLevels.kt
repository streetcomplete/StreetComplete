package de.westnordost.streetcomplete.quests.building_levels

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.BUILDINGS_WITH_LEVELS
import de.westnordost.streetcomplete.osm.Tags

class AddBuildingLevels : OsmFilterQuestType<BuildingLevelsAnswer>() {

    override val elementFilter = """
        ways, relations with
           building ~ ${BUILDINGS_WITH_LEVELS.joinToString("|")}
           and (
               !building:levels
               or !roof:levels and roof:shape and roof:shape != flat
           )
           and !man_made
           and location != underground
           and ruins != yes
    """
    override val changesetComment = "Specify building and roof levels"
    override val wikiLink = "Key:building:levels"
    override val icon = R.drawable.ic_quest_building_levels
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = when {
        tags.containsKey("building:part") -> R.string.quest_buildingLevels_title_buildingPart2
        else -> R.string.quest_buildingLevels_title2
    }

    override fun createForm() = AddBuildingLevelsForm()

    override fun applyAnswerTo(answer: BuildingLevelsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["building:levels"] = answer.levels.toString()
        answer.roofLevels?.let { tags["roof:levels"] = it.toString() }
    }
}
