package de.westnordost.streetcomplete.quests.max_weight

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable

class AddMaxWeightForm : AbstractOsmQuestForm<MaxWeightAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxweight_answer_other_sign) { onUnsupportedSign() },
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    private lateinit var type: MutableState<MaxWeightType?>
    private lateinit var weight: MutableState<Weight?>

    private val weightLimitUnits get() = countryInfo.weightLimitUnits

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            type = rememberSerializable { mutableStateOf(null) }
            weight = rememberSerializable { mutableStateOf(null) }

            MaxWeightForm(
                type = type.value,
                onSelectType = {
                    type.value = it
                    checkIsFormComplete()
                },
                weight = weight.value,
                onChangeWeight = {
                    weight.value = it
                    checkIsFormComplete()
                },
                countryCode = countryInfo.countryCode,
                selectableUnits = weightLimitUnits,
            )
        } }
    }

    override fun onClickOk() {
        if (userSelectedUnrealisticWeight()) {
            confirmUnusualInput { applyMaxWeightFormAnswer() }
        } else {
            applyMaxWeightFormAnswer()
        }
    }

    private fun userSelectedUnrealisticWeight(): Boolean {
        val weight = weight.value ?: return false
        val w = weight.toMetricTons()
        return w > 30 || w < 2
    }

    private fun applyMaxWeightFormAnswer() {
        applyAnswer(MaxWeight(type.value!!, weight.value!!))
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

    override fun isFormComplete() = type.value != null && weight.value != null

    override fun isRejectingClose() = type.value != null || weight.value != null
}
