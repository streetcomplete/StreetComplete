package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.view.Item
import de.westnordost.streetcomplete.quests.surface.Surface.*

class DetailRoadSurfaceForm  : AGroupedImageListQuestAnswerFragment<String, DetailSurfaceAnswer>() {

    override val topItems get() =
        if (osmElement!!.tags["surface"] == "paved")
            listOf(ASPHALT, CONCRETE, SETT, PAVING_STONES, WOOD, GRASS_PAVER).toItems()
        else
            listOf(DIRT, GRASS, PEBBLES, FINE_GRAVEL, SAND, COMPACTED).toItems()

    // note that for unspecific groups null is used as a value, it makes them unselecteable
    override val allItems = listOf(
            Item(null, R.drawable.panorama_surface_paved, R.string.quest_surface_value_paved, null, listOf(
                    ASPHALT, CONCRETE, PAVING_STONES,
                    SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
                    WOOD, METAL
            ).toItems()),
            Item(null, R.drawable.panorama_surface_unpaved, R.string.quest_surface_value_unpaved, null, listOf(
                    COMPACTED, FINE_GRAVEL, GRAVEL,
                    PEBBLES
            ).toItems()),
            Item(null, R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, null, listOf(
                    DIRT, GRASS, SAND
            ).toItems())
    )

    private var isInExplanationMode = false;
    private var explanationInput: EditText? = null

    override val otherAnswers = listOf(
            OtherAnswer(R.string.ic_quest_surface_detailed_answer_impossible) { confirmSwitchToNoDetailedTagPossible() }
    )

    private fun setLayout(layoutResourceId: Int) {
        val view = setContentView(layoutResourceId)

        val onChanged = TextChangedWatcher {
            checkIsFormComplete()
        }
        explanationInput = view.findViewById(R.id.explanationInput)
        explanationInput?.addTextChangedListener(onChanged)
    }

    private val explanation: String get() = explanationInput?.text?.toString().orEmpty().trim()

    override fun isFormComplete(): Boolean {
        if(isInExplanationMode) {
            return explanation.isNotEmpty()
        } else {
            return super.isFormComplete()
        }
    }

    override fun onClickOk() {
        if(isInExplanationMode) {
            applyAnswer(DetailingImpossibleAnswer(explanation))
        } else {
            super.onClickOk();
        }
    }

    override fun onClickOk(value: String) {
        // must not happen in isInExplanationMode
        applyAnswer(SurfaceAnswer(value))
    }

    private fun confirmSwitchToNoDetailedTagPossible() {
        AlertDialog.Builder(activity!!)
                .setMessage(R.string.ic_quest_surface_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) {
                    _, _ -> switchToExplanationLayout()
                }
                .setNegativeButton(R.string.quest_generic_cancel, null)
                .show()

    }

    private fun switchToExplanationLayout(){
        isInExplanationMode = true
        setLayout(R.layout.quest_surface_detailed_answer_impossible)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        isInExplanationMode = savedInstanceState?.getBoolean(IS_IN_EXPLANATION_MODE) ?: false
        setLayout(if (isInExplanationMode) R.layout.quest_surface_detailed_answer_impossible else R.layout.quest_generic_list)

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_IN_EXPLANATION_MODE, isInExplanationMode)
    }

    companion object {
        private const val IS_IN_EXPLANATION_MODE = "is_in_explanation_mode"
    }

}
