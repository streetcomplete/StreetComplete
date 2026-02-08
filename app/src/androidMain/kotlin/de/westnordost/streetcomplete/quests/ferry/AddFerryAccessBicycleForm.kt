package de.westnordost.streetcomplete.quests.ferry

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm

class AddFerryAccessBicycleForm :
    ARadioGroupQuestForm<FerryBicycleAccess, FerryBicycleAccess>() {

    override val items = FerryBicycleAccess.entries

    @Composable
    override fun BoxScope.ItemContent(item: FerryBicycleAccess) {
        Text(stringResource(item.text))
    }

}
