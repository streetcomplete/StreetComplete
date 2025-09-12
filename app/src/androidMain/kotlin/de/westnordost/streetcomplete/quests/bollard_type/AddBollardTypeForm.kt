package de.westnordost.streetcomplete.quests.bollard_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBollardTypeForm : AImageListQuestForm<BollardType, BollardTypeAnswer>() {

    override val items = BollardType.entries
    override val itemsPerRow = 3

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_bollard_type_not_bollard) {
            applyAnswer(BarrierTypeIsNotBollard)
        },
    )

    @Composable override fun BoxScope.ItemContent(item: BollardType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<BollardType>) {
        applyAnswer(selectedItems.single())
    }
}
