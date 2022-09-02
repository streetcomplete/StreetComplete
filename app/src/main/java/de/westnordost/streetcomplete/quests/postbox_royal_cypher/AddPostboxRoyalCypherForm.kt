package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddPostboxRoyalCypherForm : AImageListQuestForm<PostboxRoyalCypher, PostboxRoyalCypher>() {

    override val items = PostboxRoyalCypher.values().mapNotNull { it.asItem() }
    override val itemsPerRow = 3

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_postboxRoyalCypher_type_none) { confirmNoCypher() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
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
