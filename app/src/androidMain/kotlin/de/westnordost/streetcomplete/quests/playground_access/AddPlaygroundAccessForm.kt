package de.westnordost.streetcomplete.quests.playground_access

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddPlaygroundAccessForm : AListQuestForm<PlaygroundAccess, PlaygroundAccess>() {

    override val items = PlaygroundAccess.entries

    @Composable override fun BoxScope.ItemContent(item: PlaygroundAccess) {
        Text(stringResource(item.text))
    }
}
