package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.surface.COMMON_SPECIFIC_PAVED_SURFACES
import de.westnordost.streetcomplete.osm.surface.COMMON_SPECIFIC_UNPAVED_SURFACES
import de.westnordost.streetcomplete.osm.surface.GENERIC_ROAD_SURFACES
import de.westnordost.streetcomplete.osm.surface.GROUND_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.shouldBeDescribed
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddPathPartSurfaceForm : AImageListQuestForm<Surface, SurfaceAndNote>() {
    override val items get() =
        (COMMON_SPECIFIC_PAVED_SURFACES + COMMON_SPECIFIC_UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_ROAD_SURFACES).toItems()

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<Surface>) {
        val value = selectedItems.single()
        if (value.shouldBeDescribed) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    DescribeGenericSurfaceDialog(requireContext()) { description ->
                        applyAnswer(SurfaceAndNote(value, description))
                    }.show()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return
        }
        applyAnswer(SurfaceAndNote(value))
    }
}
