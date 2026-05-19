package de.westnordost.streetcomplete.quests.traffic_calming_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddTrafficCalmingType : OsmFilterQuestType<TrafficCalmingType>() {

    override val elementFilter = "nodes with traffic_calming = yes"
    override val changesetComment = "Specify traffic calming types"
    override val wikiLink = "Key:traffic_calming"
    override val icon = R.drawable.quest_car_bumpy
    override val title = Res.string.quest_traffic_calming_type_title
    override val achievements = listOf(PEDESTRIAN, CAR)

    @Composable
    override fun Form(onAnswer: (TrafficCalmingType) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = TrafficCalmingType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: TrafficCalmingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["traffic_calming"] = answer.osmValue
    }
}
