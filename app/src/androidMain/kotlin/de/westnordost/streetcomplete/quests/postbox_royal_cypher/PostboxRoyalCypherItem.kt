package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.postbox_royal_cypher_ciiir
import de.westnordost.streetcomplete.resources.postbox_royal_cypher_eiir
import de.westnordost.streetcomplete.resources.postbox_royal_cypher_eviiir
import de.westnordost.streetcomplete.resources.postbox_royal_cypher_eviir
import de.westnordost.streetcomplete.resources.postbox_royal_cypher_gr
import de.westnordost.streetcomplete.resources.postbox_royal_cypher_gvir
import de.westnordost.streetcomplete.resources.postbox_royal_cypher_scottish_crown
import de.westnordost.streetcomplete.resources.postbox_royal_cypher_vr
import de.westnordost.streetcomplete.resources.quest_postboxRoyalCypher_type_ciiir
import de.westnordost.streetcomplete.resources.quest_postboxRoyalCypher_type_eiir
import de.westnordost.streetcomplete.resources.quest_postboxRoyalCypher_type_eviiir
import de.westnordost.streetcomplete.resources.quest_postboxRoyalCypher_type_eviir
import de.westnordost.streetcomplete.resources.quest_postboxRoyalCypher_type_gr
import de.westnordost.streetcomplete.resources.quest_postboxRoyalCypher_type_gvir
import de.westnordost.streetcomplete.resources.quest_postboxRoyalCypher_type_scottish_crown
import de.westnordost.streetcomplete.resources.quest_postboxRoyalCypher_type_vr
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val PostboxRoyalCypher.title: StringResource? get() = when (this) {
    ELIZABETH_II ->   Res.string.quest_postboxRoyalCypher_type_eiir
    GEORGE_V ->       Res.string.quest_postboxRoyalCypher_type_gr
    GEORGE_VI ->      Res.string.quest_postboxRoyalCypher_type_gvir
    VICTORIA ->       Res.string.quest_postboxRoyalCypher_type_vr
    EDWARD_VII ->     Res.string.quest_postboxRoyalCypher_type_eviir
    SCOTTISH_CROWN -> Res.string.quest_postboxRoyalCypher_type_scottish_crown
    EDWARD_VIII ->    Res.string.quest_postboxRoyalCypher_type_eviiir
    CHARLES_III ->    Res.string.quest_postboxRoyalCypher_type_ciiir
    NONE ->           null
}

val PostboxRoyalCypher.icon: DrawableResource? get() = when (this) {
    ELIZABETH_II ->   Res.drawable.postbox_royal_cypher_eiir
    GEORGE_V ->       Res.drawable.postbox_royal_cypher_gr
    GEORGE_VI ->      Res.drawable.postbox_royal_cypher_gvir
    VICTORIA ->       Res.drawable.postbox_royal_cypher_vr
    EDWARD_VII ->     Res.drawable.postbox_royal_cypher_eviir
    SCOTTISH_CROWN -> Res.drawable.postbox_royal_cypher_scottish_crown
    EDWARD_VIII ->    Res.drawable.postbox_royal_cypher_eviiir
    CHARLES_III ->    Res.drawable.postbox_royal_cypher_ciiir
    NONE ->           null
}
