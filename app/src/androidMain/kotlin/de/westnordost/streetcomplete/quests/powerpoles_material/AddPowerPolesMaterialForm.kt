package de.westnordost.streetcomplete.quests.powerpoles_material

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPowerPolesMaterialForm : AItemSelectQuestForm<PowerPolesMaterial, PowerPolesMaterialAnswer>() {

    override val items = PowerPolesMaterial.entries
    override val itemsPerRow = 3

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_powerPolesMaterial_is_terminal) { applyAnswer(PowerLineAnchoredToBuilding) }
    )

    @Composable override fun ItemContent(item: PowerPolesMaterial) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: PowerPolesMaterial) {
        applyAnswer(selectedItem)
    }
}
