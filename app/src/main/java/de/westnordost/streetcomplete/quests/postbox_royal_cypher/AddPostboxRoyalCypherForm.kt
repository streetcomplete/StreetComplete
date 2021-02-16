package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher
import de.westnordost.streetcomplete.view.image_select.Item

class AddPostboxRoyalCypherForm : AImageListQuestAnswerFragment<PostboxRoyalCypher, PostboxRoyalCypher>() {

    override val items = listOf(
        Item(PostboxRoyalCypher.ELIZABETH_II,   R.drawable.postbox_royal_cypher_eiir),
        Item(PostboxRoyalCypher.GEORGE_VI,      R.drawable.postbox_royal_cypher_gvir),
        Item(PostboxRoyalCypher.EDWARD_VIII,    R.drawable.postbox_royal_cypher_eviiir),
        Item(PostboxRoyalCypher.GEORGE_V,       R.drawable.postbox_royal_cypher_gr),
        Item(PostboxRoyalCypher.EDWARD_VII,     R.drawable.postbox_royal_cypher_eviir),
        Item(PostboxRoyalCypher.VICTORIA,       R.drawable.postbox_royal_cypher_vr),
        Item(PostboxRoyalCypher.SCOTTISH_CROWN, R.drawable.postbox_royal_cypher_scottish_crown, R.string.quest_postboxRoyalCypher_type_scottish_crown)
    )

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_postboxRoyalCypher_type_none) { applyAnswer(PostboxRoyalCypher.NONE) }
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<PostboxRoyalCypher>) {
        applyAnswer(selectedItems.single())
    }
}
