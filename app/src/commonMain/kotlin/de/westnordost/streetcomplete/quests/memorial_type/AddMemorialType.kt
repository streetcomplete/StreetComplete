package de.westnordost.streetcomplete.quests.memorial_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddMemorialType : OsmFilterQuestType<MemorialType>() {

    override val elementFilter = """
        nodes, ways, relations with
          historic = memorial
          and (!memorial or memorial = yes)
          and !memorial:type
    """
    override val changesetComment = "Specify memorial types"
    override val wikiLink = "Key:memorial"
    override val icon = Res.drawable.quest_memorial
    override val title = Res.string.quest_memorialType_title
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(on: (QuestAction<MemorialType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = MemorialType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            on = on,
        )
    }

    override fun applyAnswerTo(answer: MemorialType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
