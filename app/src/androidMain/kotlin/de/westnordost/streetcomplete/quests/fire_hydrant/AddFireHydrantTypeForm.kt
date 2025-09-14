package de.westnordost.streetcomplete.quests.fire_hydrant

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddFireHydrantTypeForm : AItemSelectQuestForm<FireHydrantType, FireHydrantType>() {

    override val items = FireHydrantType.entries
    override val itemsPerRow = 2

    @Composable override fun ItemContent(item: FireHydrantType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: FireHydrantType) {
        applyAnswer(selectedItem)
    }
}
