package de.westnordost.streetcomplete.quests.max_weight

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_maxweight_remove_sign
import de.westnordost.streetcomplete.resources.quest_maxweight_select_sign
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.util.content
import org.jetbrains.compose.resources.stringResource

class AddMaxWeightForm : AbstractOsmQuestForm<MaxWeightAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxweight_answer_other_sign) { onUnsupportedSign() },
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    private lateinit var types: SnapshotStateList<MutableState<MaxWeightType?>> private set
    private lateinit var weights: SnapshotStateList<MutableState<Weight?>> private set

    private val weightLimitUnits get() = countryInfo.weightLimitUnits

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content {
            Surface {
                types = remember { SnapshotStateList() }
                weights = remember { SnapshotStateList() }
            Column {
                types.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEachIndexed { colIndex, _ ->
                            val i = rowIndex * 2 + colIndex
                            MaxWeightForm(
                                type = types[i].value,
                                onSelectType = {
                                    types[i].value = it
                                    checkIsFormComplete()
                                },
                                weight = weights[i].value,
                                onChangeWeight = {
                                    weights[i].value = it
                                    checkIsFormComplete()
                                },
                                countryCode = countryInfo.countryCode,
                                selectableUnits = weightLimitUnits,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Button2(
                    onClick = {
                        types.add(mutableStateOf(null))
                        weights.add(mutableStateOf(null))
                              },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.quest_maxweight_select_sign))
                }
            }
        } }
    }

    override fun onClickOk() {
        if (anyUnrealisticWeight()) {
            confirmUnusualInput { applyAnswers() }
        } else {
            applyAnswers()
        }
    }

    private fun anyUnrealisticWeight(): Boolean {
        for (weight in weights) {
            val w = weight.value?.toMetricTons()
            if (w != null && (w > 30 || w < 2)) return true
        }
        return false
    }

    private fun applyAnswers() {
        /*val maxWeights = weights.mapIndexed { i, weight ->
            MaxWeight(types.value!!, weight.value!!)
        }
        applyAnswer(MaxWeights(maxWeights))*/
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
