package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.postbox_royal_cypher.PostboxRoyalCypher.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val PostboxRoyalCypher.title: String @Composable get() = when (this) {
    ELIZABETH_II ->   "E II R"
    GEORGE_V ->       "G R"
    GEORGE_VI ->      "G VI R"
    VICTORIA ->       "V R"
    EDWARD_VII ->     "E VII R"
    SCOTTISH_CROWN -> stringResource(Res.string.quest_postboxRoyalCypher_type_scottish_crown)
    EDWARD_VIII ->    "E VIII R"
    CHARLES_III ->    "C III R"
    NONE ->           stringResource(Res.string.quest_postboxRoyalCypher_type_none)
}

val PostboxRoyalCypher.icon: DrawableResource get() = when (this) {
    ELIZABETH_II ->   Res.drawable.postbox_royal_cypher_eiir
    GEORGE_V ->       Res.drawable.postbox_royal_cypher_gr
    GEORGE_VI ->      Res.drawable.postbox_royal_cypher_gvir
    VICTORIA ->       Res.drawable.postbox_royal_cypher_vr
    EDWARD_VII ->     Res.drawable.postbox_royal_cypher_eviir
    SCOTTISH_CROWN -> Res.drawable.postbox_royal_cypher_scottish_crown
    EDWARD_VIII ->    Res.drawable.postbox_royal_cypher_eviiir
    CHARLES_III ->    Res.drawable.postbox_royal_cypher_ciiir
    NONE ->           Res.drawable.empty_128
}
