package de.westnordost.streetcomplete.quests.leaf_detail

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddTreeLeafTypeForm(
    onAnswer: (TreeLeafTypeAnswer) -> Unit
) {
    ItemSelectQuestForm(
        items = TreeLeafType.entries,
        itemsPerRow = 2,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        onClickOk = onAnswer,
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_leafType_tree_is_just_a_stump)) {
                onAnswer(NotTreeButStump)
            },
        )
    )
}
