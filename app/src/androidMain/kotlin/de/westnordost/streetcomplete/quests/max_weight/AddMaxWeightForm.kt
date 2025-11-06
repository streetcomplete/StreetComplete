package de.westnordost.streetcomplete.quests.max_weight

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content

class AddMaxWeightForm : AbstractOsmQuestForm<MaxWeightAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxweight_answer_other_sign) { onUnsupportedSign() },
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    private lateinit var types: SnapshotStateList<MutableState<MaxWeightType>> private set
    private lateinit var weights: SnapshotStateList<MutableState<Weight?>> private set

    private val weightLimitUnits get() = countryInfo.weightLimitUnits

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content {
            Surface {
                types = remember { SnapshotStateList() }
                weights = remember { SnapshotStateList() }

                MaxWeightForm(
                    types = types,
                    weights = weights,
                    countryCode = countryInfo.countryCode,
                    selectableUnits = weightLimitUnits,
                    checkIsFormComplete = { checkIsFormComplete() }
                )
        }
    } }

    override fun onClickOk() {
        if (userSelectedUnrealisticWeight()) {
            confirmUnusualInput { applyMaxWeightFormAnswer() }
        } else {
            applyMaxWeightFormAnswer()
        }
    }

    private fun userSelectedUnrealisticWeight(): Boolean {
        for (weight in weights) {
            val w = weight.value?.toMetricTons()
            if (w != null && (w > 30 || w < 2)) return true
        }
        return false
    }

    private fun applyMaxWeightFormAnswer() {
        val typesList = types.map { it.value }
        val weightsList = weights.map { it.value }

        applyAnswer(MaxWeight(typesList as List<MaxWeightType>, weightsList as List<Weight>))
    }

    private fun onUnsupportedSign() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_maxweight_unsupported_sign_request_photo)
            .setPositiveButton(android.R.string.ok) { _, _ -> composeNote() }
            .setNegativeButton(R.string.quest_leave_new_note_no) { _, _ -> hideQuest() }
            .show()
        }
    }

    private fun confirmNoSign() {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(MaxWeightAnswer.NoSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }

    private fun confirmUnusualInput(callback: () -> (Unit)) {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_maxweight_unusualInput_confirmation_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }

    override fun isFormComplete(): Boolean {
        for (i in types.indices) {
            if (types[i].value == null || weights[i].value == null) return false
        }
        return true
    }

    override fun isRejectingClose(): Boolean {
        for (i in types.indices) {
            if (types[i].value != null || weights[i].value != null) return true
        }
        return false
    }
}
