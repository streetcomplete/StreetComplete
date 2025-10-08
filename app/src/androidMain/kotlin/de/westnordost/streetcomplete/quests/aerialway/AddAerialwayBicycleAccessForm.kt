package de.westnordost.streetcomplete.quests.aerialway

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AListQuestForm
import org.jetbrains.compose.resources.stringResource

class AddAerialwayBicycleAccessForm : AListQuestForm<AerialwayBicycleAccessAnswer, AerialwayBicycleAccessAnswer>() {

    override val items = AerialwayBicycleAccessAnswer.entries

    @Composable
    override fun BoxScope.ItemContent(item: AerialwayBicycleAccessAnswer) {
        Text(stringResource(item.text))
    }
}
