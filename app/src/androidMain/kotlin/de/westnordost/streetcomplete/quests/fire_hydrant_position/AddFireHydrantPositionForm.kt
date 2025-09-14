package de.westnordost.streetcomplete.quests.fire_hydrant_position

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddFireHydrantPositionForm : AItemSelectQuestForm<FireHydrantPosition, FireHydrantPosition>() {

    override val items = FireHydrantPosition.entries
    override val itemsPerRow = 2

    @Composable override fun ItemContent(item: FireHydrantPosition) {
        val isPillar = element.tags["fire_hydrant:type"] == "pillar"
        ImageWithLabel(painterResource(item.getIcon(isPillar)), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: FireHydrantPosition) {
        applyAnswer(selectedItem)
    }
}
