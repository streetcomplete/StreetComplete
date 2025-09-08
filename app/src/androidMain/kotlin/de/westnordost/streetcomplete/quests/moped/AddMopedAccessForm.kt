package de.westnordost.streetcomplete.quests.moped

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddMopedAccessForm : AListQuestForm<MopedAccessAnswer, MopedAccessAnswer>() {

    override val items = MopedAccessAnswer.entries

    @Composable override fun BoxScope.ItemContent(item: MopedAccessAnswer) {
        Text(stringResource(item.text))
    }
}
