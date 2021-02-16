package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher
import de.westnordost.streetcomplete.view.image_select.Item

class AddPostboxRoyalCypherForm : AImageListQuestAnswerFragment<PostboxRoyalCypher, PostboxRoyalCypher>() {

    override val items = listOf(
        Item(PostboxRoyalCypher.ELIZABETHII,    R.drawable.postbox_royal_cypher_eiir),
        Item(PostboxRoyalCypher.GEORGEVI,       R.drawable.postbox_royal_cypher_gvir),
        Item(PostboxRoyalCypher.EDWARDVIII,     R.drawable.postbox_royal_cypher_eviiir),
        Item(PostboxRoyalCypher.GEORGEV,        R.drawable.postbox_royal_cypher_gr),
        Item(PostboxRoyalCypher.EDWARDVII,      R.drawable.postbox_royal_cypher_eviir),
        Item(PostboxRoyalCypher.VICTORIA,       R.drawable.postbox_royal_cypher_vr),
        Item(PostboxRoyalCypher.SCOTTISHCROWN,  R.drawable.postbox_royal_cypher_scottish_crown, R.string.quest_postboxRoyalCypher_type_crown)
    )

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_postboxRoyalCypher_type_none) { applyAnswer(PostboxRoyalCypher.NONE) }
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<PostboxRoyalCypher>) {
        applyAnswer(selectedItems.single())
    }
}
