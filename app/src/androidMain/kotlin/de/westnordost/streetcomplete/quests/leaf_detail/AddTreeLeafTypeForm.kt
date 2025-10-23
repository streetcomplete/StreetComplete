package de.westnordost.streetcomplete.quests.leaf_detail

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddTreeLeafTypeForm : AItemSelectQuestForm<TreeLeafType, TreeLeafTypeAnswer>() {

    override val items = TreeLeafType.entries
    override val itemsPerRow = 2
    override val serializer = serializer<TreeLeafType>()
    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_leafType_tree_is_just_a_stump) {
            applyAnswer(NotTreeButStump)
        },
    )

    @Composable override fun ItemContent(item: TreeLeafType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: TreeLeafType) {
        applyAnswer(selectedItem)
    }
}
