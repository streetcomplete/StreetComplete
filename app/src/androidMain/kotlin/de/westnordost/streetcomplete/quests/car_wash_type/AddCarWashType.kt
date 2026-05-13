package de.westnordost.streetcomplete.quests.car_wash_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.AUTOMATED
import de.westnordost.streetcomplete.quests.car_wash_type.CarWashType.SELF_SERVICE
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCarWashType : OsmFilterQuestType<Set<CarWashType>>() {

    override val elementFilter = "nodes, ways with amenity = car_wash and !automated and !self_service"
    override val changesetComment = "Specify car wash types"
    override val wikiLink = "Tag:amenity=car_wash"
    override val icon = R.drawable.quest_car_wash
    override val title = Res.string.quest_carWashType_title
    override val achievements = listOf(CAR)

    @Composable
    override fun Form(onAnswer: (Set<CarWashType>) -> Unit) {
        ItemsSelectQuestForm(
            items = CarWashType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: Set<CarWashType>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val isAutomated = answer.contains(AUTOMATED)
        tags["automated"] = isAutomated.toYesNo()

        val hasSelfService = answer.contains(SELF_SERVICE)
        val selfService = when {
            hasSelfService && answer.size == 1 -> "only"
            hasSelfService -> "yes"
            else -> "no"
        }
        tags["self_service"] = selfService
    }
}
