package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher
import de.westnordost.streetcomplete.view.image_select.Item

class AddPostboxRoyalCypherForm : AImageListQuestAnswerFragment<PostboxRoyalCypher, PostboxRoyalCypher>() {

    override val items = listOf(
        Item(PostboxRoyalCypher.ELIZABETHII,    R.drawable.postbox_royal_cypher_eiir,           R.string.quest_postboxRoyalCypher_type_elizabethii),
        Item(PostboxRoyalCypher.GEORGEVI,       R.drawable.postbox_royal_cypher_gvir,           R.string.quest_postboxRoyalCypher_type_georgevi),
        Item(PostboxRoyalCypher.EDWARDVIII,     R.drawable.postbox_royal_cypher_eviiir,         R.string.quest_postboxRoyalCypher_type_edwardviii),
        Item(PostboxRoyalCypher.GEORGEV,        R.drawable.postbox_royal_cypher_gr,             R.string.quest_postboxRoyalCypher_type_georgev),
        Item(PostboxRoyalCypher.EDWARDVII,      R.drawable.postbox_royal_cypher_eviir,          R.string.quest_postboxRoyalCypher_type_edwardvii),
        Item(PostboxRoyalCypher.VICTORIA,       R.drawable.postbox_royal_cypher_vr,             R.string.quest_postboxRoyalCypher_type_victoria),
        Item(PostboxRoyalCypher.SCOTTISHCROWN,  R.drawable.postbox_royal_cypher_scottish_crown, R.string.quest_postboxRoyalCypher_type_scottish_crown),
        Item(PostboxRoyalCypher.NONE,           R.drawable.postbox_royal_cypher_none,           R.string.quest_postboxRoyalCypher_type_none)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<PostboxRoyalCypher>) {
        applyAnswer(selectedItems.single())
    }
}
