package de.westnordost.streetcomplete.quests.smoothness

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.view.image_select.Item

class AddSmoothnessForm : AImageListQuestAnswerFragment<SmoothnessAnswer, SmoothnessAnswer>() {

    override val otherAnswers = listOf(
        // add other answer "none is applicable" that asks whether the surface is correct
        //  remove surface on incorrect, ask for note if correct
        //  work on this later
        //OtherAnswer(R.string.quest_smoothness_wrong_surface) { surfaceWrong() },
        OtherAnswer(R.string.quest_smoothness_obstacle) { showObstacleHint() }
    )

    val surface get() = osmElement!!.tags["surface"]

    val way get() = osmElement!!.tags["highway"]

    // show only answers that make sense for the tagged surface
    // 0-7 are excellent - impassible, ordered like SmoothnessAnswer
    override val items get() = when {
        listOf("asphalt", "paving_stones", "concrete", "metal").contains(surface) -> getAnswers(0,4)
        listOf("sett", "unhewn_cobblestone", "compacted", "grass_paver").contains(surface) -> getAnswers(2,6)
        listOf("gravel", "fine_gravel", "pebbles").contains(surface) -> getAnswers(3,6)
        listOf("dirt", "grass").contains(surface) -> getAnswers(3,7)
        else -> getAnswers(0,7)
    }

    override val itemsPerRow = 2

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<SmoothnessAnswer>) {
        applyAnswer(selectedItems.single())
    }

    private fun showObstacleHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_smoothness_obstacle_hint)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    private fun getAnswers(minSmoothness: Int, maxSmoothness: Int): List<Item<SmoothnessAnswer>> {
        val answers = mutableListOf<Item<SmoothnessAnswer>>()
        // this is assuming array is sorted in the order of SmoothnessAnswer (and works, at least on my phone)
        for (smoothness in SmoothnessAnswer.values().sliceArray(minSmoothness..maxSmoothness))
            answers.add(Item(smoothness, getImage(smoothness), getDescription(smoothness)))
        return answers
    }

    private fun getDescription(smoothness: SmoothnessAnswer) = when (smoothness) {
        SmoothnessAnswer.EXCELLENT -> R.string.quest_smoothness_excellent
        SmoothnessAnswer.GOOD -> when (way) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_good
            else -> R.string.quest_smoothness_good
        }
        SmoothnessAnswer.INTERMEDIATE -> when (way) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_intermediate_path
            else -> R.string.quest_smoothness_intermediate_road
        }
        SmoothnessAnswer.BAD -> when (way) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_bad_path
            else -> R.string.quest_smoothness_bad_road
        }
        SmoothnessAnswer.VERY_BAD -> when (way) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_very_bad_path
            else -> R.string.quest_smoothness_very_bad_road
        }
        SmoothnessAnswer.HORRIBLE -> when (way) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_horrible
            else -> R.string.quest_smoothness_horrible
        }
        SmoothnessAnswer.VERY_HORRIBLE -> when (way) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_very_horrible
            else -> R.string.quest_smoothness_very_horrible
        }
        SmoothnessAnswer.IMPASSABLE -> when (way) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_impassable
            else -> R.string.quest_smoothness_impassable
        }
    }

    private fun getImage(smoothness: SmoothnessAnswer) = when (smoothness) {
        SmoothnessAnswer.EXCELLENT -> when (surface) {
            "asphalt" -> R.drawable.surface_asphalt_excellent
            "paving_stones" -> R.drawable.surface_paving_stones_excellent
            else -> R.drawable.surface_paved_area
        }
        SmoothnessAnswer.GOOD -> when (surface) {
            "asphalt", "concrete" -> R.drawable.surface_asphalt
            "paving_stones" -> R.drawable.surface_paving_stones
            else -> R.drawable.surface_paved_area
        }
        SmoothnessAnswer.INTERMEDIATE -> when (surface) {
            "asphalt", "concrete" -> R.drawable.surface_asphalt
            "paving_stones" -> R.drawable.surface_paving_stones
            "sett" -> R.drawable.surface_sett_intermediate
            else -> R.drawable.surface_paved_area
        }
        SmoothnessAnswer.BAD -> when (surface) {
            "asphalt", "concrete" -> R.drawable.surface_asphalt
            "paving_stones" -> R.drawable.surface_paving_stones
            "sett" -> R.drawable.surface_sett
            else -> R.drawable.surface_paved_area
        }
        SmoothnessAnswer.VERY_BAD -> when (surface) {
            "asphalt", "concrete" -> R.drawable.surface_asphalt
            "paving_stones" -> R.drawable.surface_paving_stones
            else -> R.drawable.surface_paved_area
        }
        SmoothnessAnswer.HORRIBLE -> when (surface) {
            "asphalt", "concrete" -> R.drawable.surface_asphalt
            "paving_stones" -> R.drawable.surface_paving_stones
            else -> R.drawable.surface_paved_area
        }
        SmoothnessAnswer.VERY_HORRIBLE -> when (surface) {
            "asphalt", "concrete" -> R.drawable.surface_asphalt
            "paving_stones" -> R.drawable.surface_paving_stones
            else -> R.drawable.surface_paved_area
        }
        SmoothnessAnswer.IMPASSABLE -> when (surface) {
            "asphalt", "concrete" -> R.drawable.surface_asphalt
            "paving_stones" -> R.drawable.surface_paving_stones
            else -> R.drawable.surface_paved_area
        }
    }

    // low priority, add later
/*    private fun surfaceWrong() {
        // ask whether surface is correct (show current value, like house number quest)
        //  if correct, ask for note
        //  else remove surface

        val surfaceValue = osmElement!!.tags["surface"]!!
        val surfaceType = Surface.values().find { it.osmValue == surfaceValue }

        if (surfaceType != null) {
            showWrongSurfaceDialog(surfaceType)
        } else {
            // should not happen
            onClickCantSay()
        }
    }

    private fun showWrongSurfaceDialog(surface: Surface) {
        val inflater = LayoutInflater.from(requireContext())
        val inner = inflater.inflate(R.layout.dialog_quest_address_no_housenumber, null, false)
        ItemViewHolder(inner.findViewById(R.id.item_view)).bind(surface)

        AlertDialog.Builder(requireContext())
            .setView(inner)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(
                NoHouseNumber
            ) }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(
                WrongBuildingType
            ) }
            .show()
    }*/


/*  // old attempt for answers, remove later
    override val items get() = when {
        listOf("asphalt", "paving_stones", "concrete", "metal").contains(surface) -> answers.subList(0,4)
        listOf("sett", "unhewn_cobblestone", "compacted", "grass_paver").contains(surface) -> answers.subList(2,6)
        listOf("gravel", "fine_gravel", "pebbles").contains(surface) -> answers.subList(3,6)
        listOf("dirt", "grass").contains(surface) -> answers.subList(3,7)
        else -> answers
    }

    private val answers get() = listOf(
/* 0 */    Item(SmoothnessAnswer.EXCELLENT, getImage(0), getDescription(0)),
/* 1 */    Item(SmoothnessAnswer.GOOD, getImage(1), getDescription(1)),
/* 2 */    Item(SmoothnessAnswer.INTERMEDIATE, getImage(2), getDescription(2)),
/* 3 */    Item(SmoothnessAnswer.BAD, getImage(3), getDescription(3)),
/* 4 */    Item(SmoothnessAnswer.VERY_BAD, getImage(4), getDescription(4)),
/* 5 */    Item(SmoothnessAnswer.HORRIBLE, getImage(5), getDescription(5)),
/* 6 */    Item(SmoothnessAnswer.VERY_HORRIBLE, getImage(6), getDescription(6)),
/* 7 */    Item(SmoothnessAnswer.IMPASSABLE, getImage(7), getDescription(7)),
    )
    */

}
