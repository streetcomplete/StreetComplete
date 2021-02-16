package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher
import de.westnordost.streetcomplete.view.image_select.Item

class AddPostboxRoyalCypherForm : AImageListQuestAnswerFragment<PostboxRoyalCypher, PostboxRoyalCypher>() {

    override val items = listOf(
        Item(PostboxRoyalCypher.ELIZABETHII,    R.drawable.postbox_royal_cypher_eiir_red,           R.string.quest_postboxRoyalCypher_type_elizabethii),
        Item(PostboxRoyalCypher.GEORGEVI,       R.drawable.postbox_royal_cypher_gvir_red,           R.string.quest_postboxRoyalCypher_type_georgevi),
        Item(PostboxRoyalCypher.EDWARDVIII,     R.drawable.postbox_royal_cypher_eviiir_red,         R.string.quest_postboxRoyalCypher_type_edwardviii),
        Item(PostboxRoyalCypher.GEORGEV,        R.drawable.postbox_royal_cypher_gr_red,             R.string.quest_postboxRoyalCypher_type_georgev),
        Item(PostboxRoyalCypher.EDWARDVII,      R.drawable.postbox_royal_cypher_eviir_red,          R.string.quest_postboxRoyalCypher_type_edwardvii),
        Item(PostboxRoyalCypher.VICTORIA,       R.drawable.postbox_royal_cypher_vr_red,             R.string.quest_postboxRoyalCypher_type_victoria),
        Item(PostboxRoyalCypher.SCOTTISHCROWN,  R.drawable.postbox_royal_cypher_scottish_crown_red, R.string.quest_postboxRoyalCypher_type_scottish_crown)
    )

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_postboxRoyalCypher_type_none) { applyAnswer(PostboxRoyalCypher.NONE) }
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<PostboxRoyalCypher>) {
        applyAnswer(selectedItems.single())
    }
}
