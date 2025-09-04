package de.westnordost.streetcomplete.quests.foot

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddProhibitedForPedestriansForm : AListQuestForm<ProhibitedForPedestriansAnswer, ProhibitedForPedestriansAnswer>() {

    override val items = ProhibitedForPedestriansAnswer.entries

    @Composable override fun BoxScope.ItemContent(item: ProhibitedForPedestriansAnswer) {
        Text(stringResource(item.text))
    }
}
