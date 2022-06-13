package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.isSurfaceAndTracktypeMismatching
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item

class AddRoadSurfaceForm : AImageListQuestAnswerFragment<Surface, SurfaceAnswer>() {
    override val items: List<Item<Surface>>
        get() = (PAVED_SURFACES + UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_ROAD_SURFACES).toItems()

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<Surface>) {
        val value = selectedItems.single()
        if (osmElement!!.tags.containsKey("tracktype")) {
            if (isSurfaceAndTracktypeMismatching(value.osmValue, osmElement!!.tags["tracktype"]!!)) {
                confirmTracktypeMismatch { collectSurfaceDescriptionIfNeededAndApplyAnswer(value, true) }
            }
        } else {
            collectSurfaceDescriptionIfNeededAndApplyAnswer(value, false)
        }
    }

    private fun confirmTracktypeMismatch(callback: () -> (Unit)) {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_surface_tractypeMismatchInput_confirmation_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }

    private fun collectSurfaceDescriptionIfNeededAndApplyAnswer(value: Surface, removeTracktype: Boolean) {
        if (value.shouldBeDescribed) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    DescribeGenericSurfaceDialog(requireContext()) { description ->
                        applyAnswer(SurfaceAnswer(value, description, removeTracktype))
                    }.show()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return
        }
        applyAnswer(SurfaceAnswer(value, replacesTracktype = removeTracktype))
    }
}
