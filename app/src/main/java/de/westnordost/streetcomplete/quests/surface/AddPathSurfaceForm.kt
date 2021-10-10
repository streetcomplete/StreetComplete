package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.ktx.isArea
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.image_select.Item

class AddPathSurfaceForm : AImageListQuestAnswerFragment<Surface, SurfaceOrIsStepsAnswer>() {
    override val items: List<Item<Surface>>
        get() = (PAVED_SURFACES + UNPAVED_SURFACES + Surface.WOODCHIPS + GROUND_SURFACES + GENERIC_SURFACES).toItems()

    override val otherAnswers get() = listOfNotNull(
        createConvertToStepsAnswer(),
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<Surface>) {
        val value = selectedItems.single()
        if (value.shouldBeDescribed) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    DescribeGenericSurfaceDialog(requireContext()) { description ->
                        applyAnswer(GenericSurfaceAnswer(value, description))
                    }.show()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return
        }
        applyAnswer(SpecificSurfaceAnswer(value))
    }

    private fun createConvertToStepsAnswer(): AnswerItem? {
        val way = osmElement as? Way ?: return null
        if (way.isArea() || way.tags["highway"] == "steps") return null

        return AnswerItem(R.string.quest_generic_answer_is_actually_steps) {
            applyAnswer(IsActuallyStepsAnswer)
        }
    }
}
