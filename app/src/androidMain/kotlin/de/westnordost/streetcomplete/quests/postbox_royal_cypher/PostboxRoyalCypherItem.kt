package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.CHARLES_III
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.EDWARD_VII
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.EDWARD_VIII
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.ELIZABETH_II
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.GEORGE_V
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.GEORGE_VI
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.NONE
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.SCOTTISH_CROWN
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.VICTORIA
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun PostboxRoyalCypher.asItem(): DisplayItem<PostboxRoyalCypher>? {
    val iconResId = iconResId ?: return null
    val titleResId = titleResId ?: return null
    return Item(this, iconResId, titleResId)
}

private val PostboxRoyalCypher.titleResId: Int? get() = when (this) {
    ELIZABETH_II ->   R.string.quest_postboxRoyalCypher_type_eiir
    GEORGE_V ->       R.string.quest_postboxRoyalCypher_type_gr
    GEORGE_VI ->      R.string.quest_postboxRoyalCypher_type_gvir
    VICTORIA ->       R.string.quest_postboxRoyalCypher_type_vr
    EDWARD_VII ->     R.string.quest_postboxRoyalCypher_type_eviir
    SCOTTISH_CROWN -> R.string.quest_postboxRoyalCypher_type_scottish_crown
    EDWARD_VIII ->    R.string.quest_postboxRoyalCypher_type_eviiir
    CHARLES_III ->    R.string.quest_postboxRoyalCypher_type_ciiir
    NONE ->           null
}

private val PostboxRoyalCypher.iconResId: Int? get() = when (this) {
    ELIZABETH_II ->   R.drawable.ic_postbox_royal_cypher_eiir
    GEORGE_V ->       R.drawable.ic_postbox_royal_cypher_gr
    GEORGE_VI ->      R.drawable.ic_postbox_royal_cypher_gvir
    VICTORIA ->       R.drawable.ic_postbox_royal_cypher_vr
    EDWARD_VII ->     R.drawable.ic_postbox_royal_cypher_eviir
    SCOTTISH_CROWN -> R.drawable.ic_postbox_royal_cypher_scottish_crown
    EDWARD_VIII ->    R.drawable.ic_postbox_royal_cypher_eviiir
    CHARLES_III ->    R.drawable.ic_postbox_royal_cypher_ciiir
    NONE ->           null
}
