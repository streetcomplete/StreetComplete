package de.westnordost.streetcomplete.quests.max_weight

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AddMaxWeightForm : AbstractOsmQuestForm<List<MaxWeight>>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxweight_answer_other_sign) { onUnsupportedSign() },
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    private var signs: SnapshotStateList<MaxWeight> = mutableStateListOf()

    private val weightLimitUnits get() = countryInfo.weightLimitUnits

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snapshotFlow { signs.toList() }
            .onEach { checkIsFormComplete() }
            .launchIn(lifecycleScope)

        binding.composeViewBase.content { Surface {
            MaxWeightForm(
                signs = signs,
                countryCode = countryInfo.countryCode,
                selectableUnits = weightLimitUnits,
                onSignAdded = { maxweight ->
                    signs.add(maxweight)
                },
                onSignRemoved = { index ->
                    signs.removeAt(index)
                },
                onSignChanged = { index, maxweight ->
                    signs[index] = maxweight
                },
            )
        } }
    }

    override fun onClickOk() {
        if (userSelectedUnrealisticWeight()) {
            confirmUnusualInput { applyAnswer(signs) }
        } else {
            applyAnswer(signs)
        }
    }

    private fun userSelectedUnrealisticWeight(): Boolean {
        for (sign in signs) {
            val w = sign.weight?.toMetricTons() ?: continue
            if (w > 30 || w < 2) return true
        }
        return false
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
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(emptyList()) }
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

    override fun isFormComplete(): Boolean =
        signs.isNotEmpty() && signs.all { it.weight != null }

    override fun isRejectingClose(): Boolean =
        signs.isNotEmpty()
}
