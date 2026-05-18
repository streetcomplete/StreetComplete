package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.BUILDINGS_WITH_LEVELS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddBuildingLevels : OsmFilterQuestType<BuildingLevels>() {

    override val elementFilter = """
        ways, relations with
           building ~ ${BUILDINGS_WITH_LEVELS.joinToString("|")}
           and (
             !building:levels
             or !roof:levels and !roof:height and roof:shape and roof:shape != flat
           )
           and !(height and roof:height)
           and !building:min_level
           and !man_made
           and location != underground
           and ruins != yes
    """
    override val changesetComment = "Specify building and roof levels"
    override val wikiLink = "Key:building:levels"
    override val icon = R.drawable.quest_building_levels
    override val title = Res.string.quest_buildingLevels_title2
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_difficult_and_time_consuming
    override val hint = Res.string.quest_buildingLevels_hint

    @Composable
    override fun Form(onAnswer: (BuildingLevels) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddBuildingLevelsForm(onAnswer, element, countryInfo)
    }

    override fun applyAnswerTo(answer: BuildingLevels, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["building:levels"] = answer.levels.toString()
        answer.roofLevels?.let { tags["roof:levels"] = it.toString() }
    }
}
