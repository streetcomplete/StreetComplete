package de.westnordost.streetcomplete.quests.parking_access

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddParkingAccessForm : AListQuestForm<ParkingAccess, ParkingAccess>() {

    override val items = ParkingAccess.entries

    @Composable override fun BoxScope.ItemContent(item: ParkingAccess) {
        Text(stringResource(item.text))
    }
}
