package de.westnordost.streetcomplete.quests.incline_direction

import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_steps_incline_up
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleInclineForm : AItemSelectQuestForm<Incline, BicycleInclineAnswer>() {
    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_bicycle_incline_up_and_down) { confirmUpAndDown() }
    )

    override val items = Incline.entries
    override val itemsPerRow = 2

    private fun confirmUpAndDown() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(UpdAndDownHopsAnswer) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    @Composable override fun ItemContent(item: Incline) {
        ImageWithLabel(
            painter = painterResource(item.icon),
            label = stringResource(Res.string.quest_steps_incline_up),
            imageRotation = geometryRotation.floatValue - mapRotation.floatValue
        )
    }

    override fun onClickOk(selectedItem: Incline) {
        applyAnswer(RegularBicycleInclineAnswer(selectedItem))
    }
}
