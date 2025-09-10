package de.westnordost.streetcomplete.quests.fire_hydrant

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddFireHydrantTypeForm : AImageListQuestForm<FireHydrantType, FireHydrantType>() {

    override val items = FireHydrantType.entries
    override val itemsPerRow = 2

    @Composable override fun BoxScope.ItemContent(item: FireHydrantType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<FireHydrantType>) {
        applyAnswer(selectedItems.single())
    }
}
