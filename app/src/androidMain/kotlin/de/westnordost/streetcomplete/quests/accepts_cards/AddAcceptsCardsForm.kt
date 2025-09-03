package de.westnordost.streetcomplete.quests.accepts_cards

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance.CREDIT_CARDS_ONLY
import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance.DEBIT_AND_CREDIT
import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance.DEBIT_CARDS_ONLY
import de.westnordost.streetcomplete.quests.accepts_cards.CardAcceptance.NEITHER_DEBIT_NOR_CREDIT
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_accepts_cards_credit_only
import de.westnordost.streetcomplete.resources.quest_accepts_cards_debit_and_credit
import de.westnordost.streetcomplete.resources.quest_accepts_cards_dedit_only
import de.westnordost.streetcomplete.resources.quest_accepts_cards_unavailable
import de.westnordost.streetcomplete.ui.common.TextItem

class AddAcceptsCardsForm : AListQuestForm<CardAcceptance>() {

    override val items = listOf(
        TextItem(DEBIT_AND_CREDIT, Res.string.quest_accepts_cards_debit_and_credit),
        TextItem(CREDIT_CARDS_ONLY, Res.string.quest_accepts_cards_credit_only),
        TextItem(DEBIT_CARDS_ONLY, Res.string.quest_accepts_cards_dedit_only),
        TextItem(NEITHER_DEBIT_NOR_CREDIT, Res.string.quest_accepts_cards_unavailable),
    )
}
