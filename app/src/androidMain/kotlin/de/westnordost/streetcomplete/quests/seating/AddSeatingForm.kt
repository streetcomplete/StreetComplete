package de.westnordost.streetcomplete.quests.seating

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddSeatingForm : ARadioGroupQuestForm<Seating, Seating>() {
    override val items = Seating.entries

    @Composable override fun BoxScope.ItemContent(item: Seating) {
        Text(stringResource(item.text))
    }
}
