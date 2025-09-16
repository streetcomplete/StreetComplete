package de.westnordost.streetcomplete.quests.steps_ramp

import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemsSelectQuestForm
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.BICYCLE
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.STROLLER
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.WHEELCHAIR
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddStepsRampForm : AItemsSelectQuestForm<StepsRamp, StepsRampAnswer>() {

    override val items = StepsRamp.entries
    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    @Composable override fun ItemContent(item: StepsRamp) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: Set<StepsRamp>) {
        if (selectedItems.contains(WHEELCHAIR)) {
            confirmWheelchairRampIsSeparate { isSeparate ->
                val wheelchairRampStatus =
                    if (isSeparate) {
                        WheelchairRampStatus.SEPARATE
                    } else {
                        WheelchairRampStatus.YES
                    }

                applyAnswer(
                    StepsRampAnswer(
                        bicycleRamp = selectedItems.contains(BICYCLE),
                        strollerRamp = selectedItems.contains(STROLLER),
                        wheelchairRamp = wheelchairRampStatus
                    )
                )
            }
        } else {
            applyAnswer(
                StepsRampAnswer(
                    bicycleRamp = selectedItems.contains(BICYCLE),
                    strollerRamp = selectedItems.contains(STROLLER),
                    wheelchairRamp = WheelchairRampStatus.NO
                )
            )
        }
    }

    private fun confirmWheelchairRampIsSeparate(onAnswer: (Boolean) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.quest_steps_ramp_separate_wheelchair)
            .setPositiveButton(R.string.quest_steps_ramp_separate_wheelchair_confirm) { _, _ ->
                onAnswer(true)
            }
            .setNegativeButton(R.string.quest_steps_ramp_separate_wheelchair_decline) { _, _ ->
                onAnswer(false)
            }
            .setNeutralButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
