package de.westnordost.streetcomplete.quests.building_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.building.BuildingStatus
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeAndStatus
import de.westnordost.streetcomplete.osm.building.INVALID_BUILDING_TYPES
import de.westnordost.streetcomplete.osm.building.OTHER_KEYS_POTENTIALLY_DESCRIBING_BUILDING_TYPE
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.osm.building.createBuildingTypeAndStatus
import de.westnordost.streetcomplete.resources.*

class AddBuildingType : OsmFilterQuestType<BuildingStatus>() {

    override val elementFilter = """
        ways, relations with
          building ~ yes|${INVALID_BUILDING_TYPES.joinToString("|")}
          and ${OTHER_KEYS_POTENTIALLY_DESCRIBING_BUILDING_TYPE.joinToString(" and ") { "!$it" }}
          and location != underground
          and disused != yes
          and abandoned != yes
          and ruins != yes
          and !disused:building
          and !abandoned:building
          and !ruins:building
          and !construction:building
    """
    override val changesetComment = "Specify building types"
    override val wikiLink = "Key:building"
    override val icon = Res.drawable.quest_building
    override val title = Res.string.quest_buildingType_title
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_overlay

    @Composable
    override fun Form(on: (QuestAction<BuildingStatus>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddBuildingTypeForm(on)
    }

    override fun applyAnswerTo(answer: BuildingStatus, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        // we tag the status if a status was selected and otherwise the building type
        val buildingTypeAndStatus = when (answer) {
            is BuildingType -> BuildingTypeAndStatus(answer, null)
            // unsupported because unsupported means "change nothing" (while null means "unspecified")
            is BuildingStatus -> BuildingTypeAndStatus(BuildingType.UNSUPPORTED, answer)
        }
        buildingTypeAndStatus.applyTo(tags)
    }
}
