package de.westnordost.streetcomplete.quests.board_name

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.localized_name.applyTo
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.LocalizedNameQuestForm

class AddBoardName : OsmFilterQuestType<List<LocalizedName>>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
          tourism = information and information = board
          and board_type !~ notice|public_transport
          and !board:title
        )
        and !name and noname != yes and name:signed != no
    """

    override val changesetComment = "Determine information board names"
    override val wikiLink = "Tag:information=board"
    override val icon = Res.drawable.quest_label_thing
    override val title = Res.string.quest_board_name_title
    override val achievements = listOf(OUTDOORS)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways, relations with tourism = information and information = board")

    @Composable
    override fun Form(on: (QuestAction<List<LocalizedName>>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        LocalizedNameQuestForm(
            on = on,
            countryInfo = countryInfo,
            initialLocalizedNames = null,
        )
    }

    override fun applyAnswerTo(answer: List<LocalizedName>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer.isEmpty()) {
            tags["noname"] = "yes"
        } else {
            answer.applyTo(tags)
        }
    }
}
