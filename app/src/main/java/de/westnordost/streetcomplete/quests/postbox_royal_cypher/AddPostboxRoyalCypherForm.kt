package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.image_select.Item

class AddPostboxRoyalCypherForm : AImageListQuestForm<PostboxRoyalCypher, PostboxRoyalCypher>() {

    override val items = listOf(
        Item(PostboxRoyalCypher.ELIZABETH_II,   R.drawable.ic_postbox_royal_cypher_eiir,           R.string.quest_postboxRoyalCypher_type_eiir),
        Item(PostboxRoyalCypher.GEORGE_V,       R.drawable.ic_postbox_royal_cypher_gr,             R.string.quest_postboxRoyalCypher_type_gr),
        Item(PostboxRoyalCypher.GEORGE_VI,      R.drawable.ic_postbox_royal_cypher_gvir,           R.string.quest_postboxRoyalCypher_type_gvir),
        Item(PostboxRoyalCypher.VICTORIA,       R.drawable.ic_postbox_royal_cypher_vr,             R.string.quest_postboxRoyalCypher_type_vr),
        Item(PostboxRoyalCypher.EDWARD_VII,     R.drawable.ic_postbox_royal_cypher_eviir,          R.string.quest_postboxRoyalCypher_type_eviir),
        Item(PostboxRoyalCypher.SCOTTISH_CROWN, R.drawable.ic_postbox_royal_cypher_scottish_crown, R.string.quest_postboxRoyalCypher_type_scottish_crown),
        Item(PostboxRoyalCypher.EDWARD_VIII,    R.drawable.ic_postbox_royal_cypher_eviiir,         R.string.quest_postboxRoyalCypher_type_eviiir),
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_postboxRoyalCypher_type_none) { confirmNoCypher() }
    )

    override val itemsPerRow = 3

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
