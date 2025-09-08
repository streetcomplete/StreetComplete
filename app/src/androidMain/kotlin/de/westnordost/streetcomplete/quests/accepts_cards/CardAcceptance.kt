package de.westnordost.streetcomplete.quests.accepts_cards

import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_accepts_cards_credit_only
import de.westnordost.streetcomplete.resources.quest_accepts_cards_debit_and_credit
import de.westnordost.streetcomplete.resources.quest_accepts_cards_dedit_only
import de.westnordost.streetcomplete.resources.quest_accepts_cards_unavailable
import org.jetbrains.compose.resources.StringResource

enum class CardAcceptance(val debit: Boolean, val credit: Boolean) {
    DEBIT_AND_CREDIT(true, true),
    CREDIT_CARDS_ONLY(false, true),
    DEBIT_CARDS_ONLY(true, false),
    NEITHER_DEBIT_NOR_CREDIT(false, false),
}

val CardAcceptance.text: StringResource get() = when (this) {
    DEBIT_AND_CREDIT -> Res.string.quest_accepts_cards_debit_and_credit
    CREDIT_CARDS_ONLY -> Res.string.quest_accepts_cards_credit_only
    DEBIT_CARDS_ONLY -> Res.string.quest_accepts_cards_dedit_only
    NEITHER_DEBIT_NOR_CREDIT -> Res.string.quest_accepts_cards_unavailable
}
