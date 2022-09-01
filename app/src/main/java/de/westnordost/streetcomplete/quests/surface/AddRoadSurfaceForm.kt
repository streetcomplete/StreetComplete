package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.isSurfaceAndTracktypeMismatching
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddRoadSurfaceForm : AImageListQuestForm<Surface, SurfaceAnswer>() {
    override val items get() =
        (PAVED_SURFACES + UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_ROAD_SURFACES).toItems()

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<Surface>) {
        val surface = selectedItems.single()
        confirmPotentialTracktypeMismatch(surface) {
            collectSurfaceDescription(surface) { description ->
                applyAnswer(SurfaceAnswer(surface, description))
            }
        }
    }

    private fun confirmPotentialTracktypeMismatch(
        surface: Surface,
        onTracktypeConfirmed: () -> Unit
    ) {
        val tracktype = element.tags["tracktype"]
        if (tracktype == null) {
            onTracktypeConfirmed()
        } else if (isSurfaceAndTracktypeMismatching(surface.osmValue, tracktype)) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_generic_confirmation_title)
                .setMessage(R.string.quest_surface_tractypeMismatchInput_confirmation_description)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    onTracktypeConfirmed()
                }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        } else {
            onTracktypeConfirmed()
        }
    }

    private fun collectSurfaceDescription(
        surface: Surface,
        onSurfaceDescribed: (description: String?) -> Unit
    ) {
        if (!surface.shouldBeDescribed) {
            onSurfaceDescribed(null)
        } else {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    DescribeGenericSurfaceDialog(requireContext(), onSurfaceDescribed).show()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }
}
