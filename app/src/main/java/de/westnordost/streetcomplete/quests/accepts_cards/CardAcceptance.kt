package de.westnordost.streetcomplete.quests.accepts_cards

enum class CardAcceptance(val debit: String, val credit: String) {
    DEBIT_AND_CREDIT("yes", "yes"),
    CREDIT_CARDS_ONLY("no", "yes"),
    DEBIT_CARDS_ONLY("yes", "no"),
    NEITHER_DEBIT_NOR_CREDIT("no", "no"),
}
