package de.westnordost.streetcomplete.quests.smoothness

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.surface.Surface
import de.westnordost.streetcomplete.quests.surface.asItem
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class AddSmoothnessForm : AImageListQuestAnswerFragment<Smoothness, SmoothnessAnswer>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_smoothness_wrong_surface) { surfaceWrong() },
        OtherAnswer(R.string.quest_smoothness_obstacle) { showObstacleHint() }
    )

    private val surfaceTag get() = osmElement!!.tags["surface"]

    private val highwayTag get() = osmElement!!.tags["highway"]

    // show only answers that make sense for the tagged surface
    // numbers 0-7 are excellent - impassible, ordered like Smoothness class
    // TODO: leave unused surfaces in here for future extension of the quest? or remove?
    override val items get() = when {
        // paving stones on roads will not be "excellent" as they get easily damaged by cars
        surfaceTag == "paving_stones" && !ALL_PATHS_EXCEPT_STEPS.contains(highwayTag) -> getAnswers(1,4)
        listOf("asphalt", "paving_stones", "concrete", "metal").contains(surfaceTag) -> getAnswers(0,4)
        listOf("sett", "unhewn_cobblestone", "compacted", "grass_paver").contains(surfaceTag) -> getAnswers(2,6)
        listOf("gravel", "fine_gravel", "pebbles").contains(surfaceTag) -> getAnswers(3,6)
        listOf("dirt", "grass").contains(surfaceTag) -> getAnswers(3,7)
        else -> getAnswers(0,7)
    }

    override val itemsPerRow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_smoothness
    }

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<Smoothness>) {
        applyAnswer(SmoothnessValueAnswer(selectedItems.single().osmValue))
    }

    private fun showObstacleHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_smoothness_obstacle_hint)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    // this requires Smoothness class to be sorted from best to worst!
    private fun getAnswers(minSmoothness: Int, maxSmoothness: Int): List<Item<Smoothness>> {
        val answers = mutableListOf<Item<Smoothness>>()
        for (smoothness in Smoothness.values().sliceArray(minSmoothness..maxSmoothness))
            answers.add(Item(smoothness, getImage(smoothness), smoothness.title, getDescription(smoothness)))
        return answers
    }

    private fun getDescription(smoothness: Smoothness) = when (smoothness) {
        Smoothness.EXCELLENT -> when (surfaceTag) {
            "paving_stones" -> R.string.quest_smoothness_description_excellent_paving_stones
            else -> R.string.quest_smoothness_description_excellent
        }
        Smoothness.GOOD -> when (surfaceTag) {
            "paving_stones" -> R.string.quest_smoothness_description_good_paving_stones
            else -> R.string.quest_smoothness_description_good
        }
        Smoothness.INTERMEDIATE -> when (surfaceTag) {
            "paving_stones", "sett" -> R.string.quest_smoothness_description_intermediate_paving_stones
            "compacted" -> R.string.quest_smoothness_description_intermediate_compacted
            else -> when (highwayTag) {
                in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_intermediate_path
                else -> R.string.quest_smoothness_description_intermediate
            }
        }
        Smoothness.BAD -> when (surfaceTag) {
            "sett" -> R.string.quest_smoothness_description_bad_sett
            "paving_stones" -> R.string.quest_smoothness_description_bad_paving_stones
            else -> when (highwayTag) {
                in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_bad_path
                else -> R.string.quest_smoothness_description_bad_road
            }
        }
        Smoothness.VERY_BAD -> when (highwayTag) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_very_bad_path
            else -> R.string.quest_smoothness_description_very_bad_road
        }
        Smoothness.HORRIBLE -> when (highwayTag) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_horrible
            else -> R.string.quest_smoothness_description_horrible
        }
        Smoothness.VERY_HORRIBLE -> when (highwayTag) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_very_horrible
            else -> R.string.quest_smoothness_description_very_horrible
        }
        Smoothness.IMPASSABLE -> when (highwayTag) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_impassable
            else -> R.string.quest_smoothness_description_impassable
        }
    }

    private fun getImage(smoothness: Smoothness) = when (smoothness) {
        Smoothness.EXCELLENT -> when (surfaceTag) {
            "paving_stones" -> R.drawable.surface_paving_stones_excellent
            else -> R.drawable.surface_asphalt_excellent
        }
        Smoothness.GOOD -> when (surfaceTag) {
            "paving_stones" -> R.drawable.surface_paving_stones_good
            else -> R.drawable.surface_asphalt_good
        }
        Smoothness.INTERMEDIATE -> when (surfaceTag) {
            "paving_stones" -> R.drawable.surface_paving_stones_intermediate
            "sett" -> R.drawable.surface_sett_intermediate
            "compacted" -> R.drawable.surface_compacted_intermediate
            else -> R.drawable.surface_asphalt_intermediate
        }
        Smoothness.BAD -> when (surfaceTag) {
            "paving_stones" -> R.drawable.surface_paving_stones_bad
            "sett" -> R.drawable.surface_sett_bad
            "gravel" -> R.drawable.surface_gravel_bad
            "compacted" -> R.drawable.surface_compacted_bad
            else -> R.drawable.surface_asphalt_bad
        }
        Smoothness.VERY_BAD -> when (surfaceTag) {
            "paving_stones" -> R.drawable.surface_paving_stones_very_bad
            "sett" -> R.drawable.surface_sett_very_bad
            else -> R.drawable.surface_asphalt_very_bad
        }
        // TODO: need images for the values below (and replace the ones above)
        Smoothness.HORRIBLE  -> R.drawable.surface_paved_area
        Smoothness.VERY_HORRIBLE -> R.drawable.surface_paved_area
        Smoothness.IMPASSABLE -> R.drawable.surface_paved_area
    }

    private fun surfaceWrong() {
        val surfaceType = Surface.values().find { it.osmValue == surfaceTag }

        if (surfaceType != null) {
            showWrongSurfaceDialog(surfaceType)
        } else {
            // this should not happen: smoothness quest is not asked for unknown surfaces
            composeNote()
        }
    }

    private fun showWrongSurfaceDialog(surface: Surface) {
        val inflater = LayoutInflater.from(requireContext())
        val inner = inflater.inflate(R.layout.dialog_quest_smoothness_wrong_surface, null, false)
        ItemViewHolder(inner.findViewById(R.id.item_view)).bind(surface.asItem())

        AlertDialog.Builder(requireContext())
            .setView(inner)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> composeNote() } // TODO: maybe do something else?
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(WrongSurfaceAnswer) }
            .show()
    }

}
