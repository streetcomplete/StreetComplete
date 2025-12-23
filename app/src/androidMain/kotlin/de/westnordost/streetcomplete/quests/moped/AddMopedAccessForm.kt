package de.westnordost.streetcomplete.quests.moped

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddMopedAccessForm : ARadioGroupQuestForm<MopedAccessAnswer, MopedAccessAnswer>() {

    override val items = MopedAccessAnswer.entries

    @Composable override fun BoxScope.ItemContent(item: MopedAccessAnswer) {
        Text(stringResource(item.text))
    }
}
