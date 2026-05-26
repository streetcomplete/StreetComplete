package de.westnordost.streetcomplete.quests.building_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.INVALID_BUILDING_TYPES
import de.westnordost.streetcomplete.osm.building.OTHER_KEYS_POTENTIALLY_DESCRIBING_BUILDING_TYPE
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.resources.*

class AddBuildingType : OsmFilterQuestType<BuildingType>() {

    override val elementFilter = """
        ways, relations with
        building ~ yes|${INVALID_BUILDING_TYPES.joinToString("|")}
        and ${OTHER_KEYS_POTENTIALLY_DESCRIBING_BUILDING_TYPE.joinToString(" and ") { "!$it" }}
        and location != underground
        and disused != yes
        and abandoned != yes
        and ruins != yes
    """
    override val changesetComment = "Specify building types"
    override val wikiLink = "Key:building"
    override val icon = Res.drawable.quest_building
    override val title = Res.string.quest_buildingType_title
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_overlay

    @Composable
    override fun Form(onAnswer: (BuildingType) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddBuildingTypeForm(onAnswer)
    }

    override fun applyAnswerTo(answer: BuildingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
