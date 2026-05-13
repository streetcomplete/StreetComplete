package de.westnordost.streetcomplete.quests.parking_access

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

@Composable fun AddParkingAccessForm(
    onAnswer: (ParkingAccess) -> Unit
) {
    RadioGroupQuestForm(
        items = ParkingAccess.entries,
        itemContent = { Text(stringResource(it.text)) },
        onClickOk = onAnswer
    )
}
