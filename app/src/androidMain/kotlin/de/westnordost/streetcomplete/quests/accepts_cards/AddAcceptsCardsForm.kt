package de.westnordost.streetcomplete.quests.accepts_cards

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddAcceptsCardsForm : AListQuestForm<CardAcceptance, CardAcceptance>() {

    override val items = CardAcceptance.entries

    @Composable override fun BoxScope.ItemContent(item: CardAcceptance) {
        Text(stringResource(item.text))
    }
}
