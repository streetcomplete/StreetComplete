package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.view.Item

class DetailRoadSurfaceForm  : AImageListQuestAnswerFragment<String, DetailSurfaceAnswer>() {

    override val items: List<Item<String>>
        get() = if (osmElement!!.tags["surface"] == "paved")
            (PAVED_SURFACES + UNPAVED_SURFACES + GROUND_SURFACES).toItems()
        else
            (UNPAVED_SURFACES + GROUND_SURFACES + PAVED_SURFACES).toItems()

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        // must not happen in isInExplanationMode
        applyAnswer(SurfaceAnswer(selectedItems.single()))
    }

    private var isInExplanationMode = false;
    private var explanationInput: EditText? = null

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_surface_detailed_answer_impossible) { confirmSwitchToNoDetailedTagPossible() }
    )

    private fun setLayout(layoutResourceId: Int) {
        val view = setContentView(layoutResourceId)

        explanationInput = view.findViewById(R.id.explanationInput)
        explanationInput?.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    private val explanation: String get() = explanationInput?.text?.toString().orEmpty().trim()

    override fun isFormComplete(): Boolean {
        return if(isInExplanationMode) {
            explanation.isNotEmpty()
        } else {
            super.isFormComplete()
        }
    }

    override fun onClickOk() {
        if(isInExplanationMode) {
            applyAnswer(DetailingImpossibleAnswer(explanation))
        } else {
            super.onClickOk();
        }
    }

    private fun confirmSwitchToNoDetailedTagPossible() {
        AlertDialog.Builder(activity!!)
                .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) {
                    _, _ -> switchToExplanationLayout()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

    }

    private fun switchToExplanationLayout() {
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
