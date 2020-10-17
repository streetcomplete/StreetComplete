package de.westnordost.streetcomplete.quests.surface

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddPathSurfaceForm : AImageListQuestAnswerFragment<String, DetailSurfaceAnswer>() {

    /*
    TODO - lost functionality for now
    is it going to work out of box?
    override val topItems get() =
        when (val pathType = determinePathType(osmElement!!.tags)) {
            "bridleway" -> listOf(
                DIRT, GRASS, SAND,
                PEBBLES, FINE_GRAVEL, COMPACTED
            )
            "path" -> listOf(
                DIRT, PEBBLES, COMPACTED,
                ASPHALT, FINE_GRAVEL, PAVING_STONES
            )
            "footway" -> listOf(
                PAVING_STONES, ASPHALT, CONCRETE,
                COMPACTED, FINE_GRAVEL, DIRT
            )
            "cycleway" -> listOf(
                PAVING_STONES, ASPHALT, CONCRETE,
                COMPACTED, WOOD, METAL
            )
            "steps" -> listOf(
                PAVING_STONES, ASPHALT, CONCRETE,
                WOOD, SETT, UNHEWN_COBBLESTONE
            )
            else -> throw IllegalStateException("Unexpected path type $pathType")
        }.toItems()
     */

    override val items: List<Item<String>> get() =
        //if (osmElement!!.tags["surface"] == "paved") - TODO: reimplement or remove
        (PAVED_SURFACES + UNPAVED_SURFACES + GROUND_SURFACES).toItems() +
            // TODO: have proper images for path (crop from panorama images)
            Item("paved", R.drawable.path_surface_paved, R.string.quest_surface_value_paved, null, listOf()) +
            Item("unpaved", R.drawable.path_surface_unpaved, R.string.quest_surface_value_unpaved, null, listOf()) +
            Item("ground", R.drawable.surface_ground, R.string.quest_surface_value_ground, null, listOf())

    // TODO: everything below duplicates AddRoadSurfaceForm...
    // TODO: DRY it?

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        // this calls comes from onClickOk() in this class,
        // through onClickOk() AImageListQuestAnswerFragment
        // that calls onClickOk with parameters - that is
        // overloaded here

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

    class DescribeGenericSurfaceDialog(
        context: Context,
        onSurfaceDescribed: (txt:String) -> Unit
    ) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {
        init {
            val view = LayoutInflater.from(context).inflate(R.layout.quest_surface_detailed_answer_impossible, null)
            val explanationInput = view.findViewById<EditText>(R.id.explanationInput)
            // TODO enable/disable ok button based on whether commentInput is empty (with TextWatcher?)

            setTitle(context.resources.getString(R.string.quest_surface_detailed_answer_impossible_title))

            setButton(
                DialogInterface.BUTTON_POSITIVE,
                context.getString(android.R.string.yes)
            ) { _, _ ->
                val txt = explanationInput.text.toString().trim()
                if (!txt.isEmpty()) {
                    onSurfaceDescribed(txt)
                }
            }

            setButton(
                DialogInterface.BUTTON_NEGATIVE,
                context.getString(android.R.string.cancel)
            ) { _, _ ->
                setView(view)
            }
            setView(view)
        }
    }

}
