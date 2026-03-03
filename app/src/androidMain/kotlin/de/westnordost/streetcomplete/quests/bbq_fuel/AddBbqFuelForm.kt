package de.westnordost.streetcomplete.quests.bbq_fuel

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import org.jetbrains.compose.resources.stringResource

class AddBbqFuelForm : ARadioGroupQuestForm<BbqFuel, BbqFuelAnswer>() {
    override val items = BbqFuel.entries

    @Composable override fun BoxScope.ItemContent(item: BbqFuel) {
        Text(stringResource(item.text))
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_bbq_fuel_not_a_bbq) { confirmNotBbq() },
    )

    private fun confirmNotBbq() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_bbq_fuel_not_a_bbq_confirmation)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(BbqFuelAnswer.IsFirePit) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
