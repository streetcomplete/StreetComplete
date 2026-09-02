package de.westnordost.streetcomplete.quests.general_access

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddGeneralAccess : OsmFilterQuestType<GeneralAccess>() {

    override val elementFilter = """
        nodes, ways, relations with
          (
            amenity ~ bbq|bicycle_wash|compressed_air|dog_toilet|kitchen|sanitary_dump_station|shower|toilets|toy_library|water_point
            or leisure ~ bird_hide|wildlife_hide
            or shelter_type = basic_hut
            or waterway ~ fuel|water_point
          )
          and (!access or access = unknown)
          and !access:conditional
    """
    override val changesetComment = "Specify access to places"
    override val wikiLink = "Key:access"
    override val icon = Res.drawable.quest_playground // TODO
    override val title = Res.string.quest_generalAccess_title
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(on: (QuestAction<GeneralAccess>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        RadioGroupQuestForm(
            on = on,
            title = stringResource(
                if (element.tags["amenity"] != null || element.tags["waterway"] != null) {
                    // Things you "use"
                    Res.string.quest_generalFee_title
                } else if (element.tags["shelter_type"] == "basic_hut") {
                    // Places you "stay at"
                    Res.string.quest_generalFee_title3
                } else {
                    // Places you "enter"
                    Res.string.quest_generalFee_title2
                }
            ),
            items = GeneralAccess.entries,
            itemContent = { Text(stringResource(it.text)) },
        )
    }

    override fun applyAnswerTo(answer: GeneralAccess, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["access"] = answer.osmValue
    }
}
