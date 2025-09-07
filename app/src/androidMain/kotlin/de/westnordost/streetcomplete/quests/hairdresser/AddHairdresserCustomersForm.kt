package de.westnordost.streetcomplete.quests.hairdresser

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddHairdresserCustomersForm : AListQuestForm<HairdresserCustomers, HairdresserCustomers>() {

    override val items = HairdresserCustomers.entries

    @Composable override fun BoxScope.ItemContent(item: HairdresserCustomers) {
        Text(stringResource(item.text))
    }
}
