package de.westnordost.streetcomplete.quests.powerpoles_material

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddPowerPolesMaterialForm : AImageListQuestForm<PowerPolesMaterial, PowerPolesMaterial>() {

    override val items = PowerPolesMaterial.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_powerPolesMaterial_is_terminal) { applyAnswer(PowerPolesMaterial.TERMINAL) }
    )

    override fun onClickOk(selectedItems: List<PowerPolesMaterial>) {
        applyAnswer(selectedItems.single())
    }

}
