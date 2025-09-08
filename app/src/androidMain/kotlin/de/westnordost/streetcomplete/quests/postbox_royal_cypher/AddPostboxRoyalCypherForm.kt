package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconItem

class AddPostboxRoyalCypherForm : AImageListQuestComposeForm<PostboxRoyalCypher, PostboxRoyalCypher>() {

    override val items = PostboxRoyalCypher.entries.mapNotNull { it.asItem() }
    override val itemContent =
        @androidx.compose.runtime.Composable { item: ImageListItem<PostboxRoyalCypher>, index: Int, onClick: () -> Unit, role: Role ->
            key(item.item) {
                SelectableIconItem(
                    item = item.item,
                    isSelected = item.checked,
                    onClick = onClick,
                    role = role
                )
            }
        }
    override val itemsPerRow = 3

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_postboxRoyalCypher_type_none) { confirmNoCypher() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onClickOk(selectedItems: List<PostboxRoyalCypher>) {
        applyAnswer(selectedItems.single())
    }

    private fun confirmNoCypher() {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(PostboxRoyalCypher.NONE) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
