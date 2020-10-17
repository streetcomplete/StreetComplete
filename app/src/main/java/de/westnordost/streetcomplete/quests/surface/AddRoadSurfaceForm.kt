package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.view.image_select.Item

class AddRoadSurfaceForm  : AImageListQuestAnswerFragment<String, DetailSurfaceAnswer>() {
    override val items: List<Item<String>> get() =
        (PAVED_SURFACES + UNPAVED_SURFACES + GROUND_SURFACES).toItems() +
            Item("paved", R.drawable.surface_paved, R.string.quest_surface_value_paved, null, listOf()) +
            Item("unpaved", R.drawable.surface_unpaved, R.string.quest_surface_value_unpaved, null, listOf()) +
            Item("ground", R.drawable.surface_ground, R.string.quest_surface_value_ground, null, listOf())

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        val value = selectedItems.single()
        if(value == "paved" || value == "unpaved" || value == "ground") {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) {
                    _, _ -> run {
                    DescribeGenericSurfaceDialog(requireContext()) { description ->
                        applyAnswer(DetailingWhyOnlyGeneric(value, description))
                    }.show()
                }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return
        }
        applyAnswer(SurfaceAnswer(value))
    }
}
