package de.westnordost.streetcomplete.quests.building_entrance

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddEntranceForm : AListQuestForm<EntranceAnswer, EntranceAnswer>() {

    override val items = EntranceType.entries + EntranceAnswer.IsDeadEnd

    @Composable
    override fun BoxScope.ItemContent(item: EntranceAnswer) {
        Text(stringResource(item.text))
    }
}
