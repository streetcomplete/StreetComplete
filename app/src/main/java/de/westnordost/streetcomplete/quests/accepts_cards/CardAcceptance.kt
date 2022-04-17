package de.westnordost.streetcomplete.quests.accepts_cards

enum class CardAcceptance(val debit: Boolean, val credit: Boolean) {
    DEBIT_AND_CREDIT(true, true),
    CREDIT_CARDS_ONLY(false, true),
    DEBIT_CARDS_ONLY(true, false),
    NEITHER_DEBIT_NOR_CREDIT(false, false),
}
