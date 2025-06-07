package de.westnordost.streetcomplete.quests.steps_ramp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.BICYCLE
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.STROLLER
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.WHEELCHAIR
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableImageCell

class AddStepsRampForm : AImageListQuestForm<StepsRamp, StepsRampAnswer>() {

    override val items = StepsRamp.entries.map { it.asItem() }
    override val itemsPerRow = 2
    override val maxSelectableItems = -1
    override val moveFavoritesToFront = false

    override val itemContent = @Composable { item: ImageListItem<StepsRamp>, index: Int, onClick: () -> Unit, role: Role ->
        key(item.item) {
            SelectableImageCell(
                item = item.item,
                isSelected = item.checked,
                onClick = {
                    if (item.item.value == StepsRamp.NONE) {
                        currentItems.value = currentItems.value.map { if (it.item.value != StepsRamp.NONE) ImageListItem(it.item, false) else it }
                    } else {
                        currentItems.value = currentItems.value.map { if (it.item.value == StepsRamp.NONE) ImageListItem(it.item, false) else it }
                    }
                    onClick()
                },
                modifier = Modifier.fillMaxSize(),
                role = role
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onClickOk(selectedItems: List<StepsRamp>) {
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
