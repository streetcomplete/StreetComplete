package de.westnordost.streetcomplete.quests.bike_shop

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddSecondHandBicycleAvailabilityForm : AListQuestForm<SecondHandBicycleAvailability, SecondHandBicycleAvailability>() {

    override val items = SecondHandBicycleAvailability.entries

    @Composable override fun BoxScope.ItemContent(item: SecondHandBicycleAvailability) {
        Text(stringResource(item.text))
    }
}
