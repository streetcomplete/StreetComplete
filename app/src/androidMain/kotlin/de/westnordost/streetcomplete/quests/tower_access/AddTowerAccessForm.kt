package de.westnordost.streetcomplete.quests.tower_access

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddTowerAccessForm : ARadioGroupQuestForm<TowerAccess, TowerAccess>() {

    override val items = TowerAccess.entries

    @Composable override fun BoxScope.ItemContent(item: TowerAccess) {
        Text(stringResource(item.text))
    }
}
