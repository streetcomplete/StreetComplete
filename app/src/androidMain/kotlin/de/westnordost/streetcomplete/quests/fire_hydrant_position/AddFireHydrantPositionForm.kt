package de.westnordost.streetcomplete.quests.fire_hydrant_position

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddFireHydrantPositionForm : AImageListQuestForm<FireHydrantPosition, FireHydrantPosition>() {

    override val items = FireHydrantPosition.entries
    override val itemsPerRow = 2

    @Composable override fun BoxScope.ItemContent(item: FireHydrantPosition) {
        val isPillar = element.tags["fire_hydrant:type"] == "pillar"
        ImageWithLabel(painterResource(item.getIcon(isPillar)), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<FireHydrantPosition>) {
        applyAnswer(selectedItems.single())
    }
}
