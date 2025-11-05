package de.westnordost.streetcomplete.quests.max_weight

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_maxweight_add_sign
import de.westnordost.streetcomplete.resources.quest_maxweight_select_sign
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.util.content
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
                var showSelectionDialog by remember { mutableStateOf(false) }

            Column {
                types.forEachIndexed { i, _ ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            MaxWeightForm(
                                type = types[i].value,
                                weight = weights[i].value,
                                onChangeWeight = {
                                    weights[i].value = it
                                    checkIsFormComplete()
                                },
                                countryCode = countryInfo.countryCode,
                                selectableUnits = weightLimitUnits
                            )
                        }
                        IconButton(
                            onClick = {
                                types.removeAt(i)
                                weights.removeAt(i)
                                checkIsFormComplete() },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                        ) {
                            Icon(painterResource(Res.drawable.ic_delete_24), null)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                if (types.size < maxSupportedSigns(countryInfo.countryCode)) {
                    Button2(
                        onClick = {
                                  showSelectionDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (types.isEmpty())
                            Text(stringResource(Res.string.quest_maxweight_select_sign))
                        else
                            Text(stringResource(Res.string.quest_maxweight_add_sign))
                    }
                }

                if (showSelectionDialog) {
                    SimpleItemSelectDialog(
                        onDismissRequest = { showSelectionDialog = false },
                        columns = SimpleGridCells.Fixed(2),
                        items = MaxWeightType.entries.filter { it !in types.map { it.value } as List<MaxWeightType> && it.getIcon(countryInfo.countryCode) != null },
                        onSelected = {
                            types.add(mutableStateOf(it))
                            weights.add(mutableStateOf(null))
                            checkIsFormComplete()
                            showSelectionDialog = false
                        },
                        itemContent = {
                            val icon = it.getIcon(countryInfo.countryCode)
                            if (icon != null) Image(painterResource(icon), null)
                        }
                    )
                }
            }
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

    private fun maxSupportedSigns(countryCode: String): Int = when (countryCode) {
            "AU", "CA", "US", "DE" -> 5
            else -> 4
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
