package de.westnordost.streetcomplete.quests.leaf_detail

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight
import de.westnordost.streetcomplete.quests.kerb_height.icon
import de.westnordost.streetcomplete.quests.kerb_height.title
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddTreeLeafTypeForm : AImageListQuestForm<TreeLeafType, TreeLeafTypeAnswer>() {

    override val items = TreeLeafType.entries
    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<TreeLeafType>) {
        applyAnswer(selectedItems.single())
    }

    @Composable override fun BoxScope.ItemContent(item: TreeLeafType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_leafType_tree_is_just_a_stump) {
            applyAnswer(NotTreeButStump)
        },
    )
}
