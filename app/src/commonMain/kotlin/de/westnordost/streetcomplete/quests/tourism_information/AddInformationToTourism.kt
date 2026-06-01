package de.westnordost.streetcomplete.quests.tourism_information

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddInformationToTourism : OsmFilterQuestType<TourismInformation>() {

    override val elementFilter = "nodes, ways with tourism = information and !information"
    override val changesetComment = "Specify type of tourist informations"
    override val wikiLink = "Tag:tourism=information"
    override val icon = Res.drawable.quest_information
    override val title = Res.string.quest_tourism_information_title
    override val achievements = listOf(RARE, CITIZEN, OUTDOORS)

    @Composable
    override fun Form(on: (QuestAction<TourismInformation>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = TourismInformation.entries,
            itemsPerRow = 2,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            on = on,
        )
    }

    override fun applyAnswerTo(answer: TourismInformation, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["information"] = answer.osmValue
    }
}
