package de.westnordost.streetcomplete.quests.accepts_cards

import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

enum class CardAcceptance(val debit: Boolean, val credit: Boolean) {
    DEBIT_AND_CREDIT(true, true),
    CREDIT_CARDS_ONLY(false, true),
    DEBIT_CARDS_ONLY(true, false),
    NEITHER_DEBIT_NOR_CREDIT(false, false),
}

val CardAcceptance.text: StringResource
    get() = when (this) {
    CardAcceptance.DEBIT_AND_CREDIT -> Res.string.quest_accepts_cards_debit_and_credit
    CardAcceptance.CREDIT_CARDS_ONLY -> Res.string.quest_accepts_cards_credit_only
    CardAcceptance.DEBIT_CARDS_ONLY -> Res.string.quest_accepts_cards_dedit_only
    CardAcceptance.NEITHER_DEBIT_NOR_CREDIT -> Res.string.quest_accepts_cards_unavailable
}
