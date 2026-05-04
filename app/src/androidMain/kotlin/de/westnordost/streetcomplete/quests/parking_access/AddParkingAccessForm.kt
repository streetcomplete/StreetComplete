package de.westnordost.streetcomplete.quests.parking_access

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddParkingAccessForm : AbstractOsmQuestForm<ParkingAccess>() {

    @Composable
    override fun Content() {
        RadioGroupQuestForm(
            items = ParkingAccess.entries,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = { applyAnswer(it) }
        )
    }
}
