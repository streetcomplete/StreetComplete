package de.westnordost.streetcomplete.quests.steps_ramp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.BICYCLE
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.NONE
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.STROLLER
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.WHEELCHAIR
import de.westnordost.streetcomplete.view.image_select.ImageSelectAdapter

class AddStepsRampForm : AImageListQuestForm<StepsRamp, StepsRampAnswer>() {

    override val items = StepsRamp.entries.map { it.asItem() }
    override val itemsPerRow = 2
    override val maxSelectableItems = -1
    override val moveFavoritesToFront = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // NONE is exclusive with the other options
        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                val noneIndex = imageSelector.indexOf(NONE)
                if (index == noneIndex) {
                    for (selectedIndex in imageSelector.selectedIndices) {
                        if (selectedIndex != index) imageSelector.deselect(selectedIndex)
                    }
                } else {
                    imageSelector.deselect(noneIndex)
                }
            }

            override fun onIndexDeselected(index: Int) {}
        })
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
