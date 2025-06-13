package de.westnordost.streetcomplete.quests.max_weight

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.max_weight.signs.GeneralMaxWeightSign
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class AddMaxWeightForm : AbstractOsmQuestForm<MaxWeightAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxweight_answer_other_sign) { onUnsupportedSign() },
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    private var sign: MutableState<MaxWeightSign?> = mutableStateOf(null)
    private val weightLimitUnits get() = countryInfo.weightLimitUnits

    override fun isFormComplete() = getWeightFromInput() != null

    override fun isRejectingClose() = sign.value != null || getWeightFromInput() != null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content {
            sign = remember { mutableStateOf(null) }
            Surface {
                if (sign.value == null) {
                    Button(onClick = { showSignSelectionDialog() }) {
                        Text(stringResource(R.string.quest_maxweight_select_sign))
                    }
                } else {
                    GeneralMaxWeightSign(
                        maxWeightSign = sign.value!!,
                        signType = sign.value!!.getSignType(countryInfo.countryCode)
                    )
                }
            }
        }
        initMaxWeightInput()
    }

    private fun initMaxWeightInput() {
        // val maxWeightInput = maxWeightInput ?: return
        //
        // maxWeightInput.doAfterTextChanged { checkIsFormComplete() }
        // maxWeightInput.filters = arrayOf(acceptDecimalDigits(6, 2))
        // binding.inputSignContainer.setOnClickListener { focusMaxWeightInput() }
        //
        // val units = weightLimitUnits.map { it.displayString }
        // weightUnitSelect?.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_centered, units)
        // weightUnitSelect?.setSelection(0)
    }

    private fun focusMaxWeightInput() {
        // val maxWeightInput = maxWeightInput ?: return
        //
        // maxWeightInput.requestFocus()
        // maxWeightInput.showKeyboard()
    }

    private fun showSignSelectionDialog() {
        val ctx = context ?: return
        val items = MaxWeightSign.entries.map {
            it.asItem(layoutInflater, countryInfo.countryCode)
        }
        ImageListPickerDialog(ctx, items, R.layout.cell_labeled_icon_select, 2) { selected ->
            selected.value?.let { setMaxWeightSign(it) }
            checkIsFormComplete()
        }.show()
    }

    private fun setMaxWeightSign(sign: MaxWeightSign) {
        this.sign.value = sign

        // binding.selectSignButton.isInvisible = true
        // binding.inputSignContainer.removeAllViews()

        // val layoutResourceId = sign.getLayoutResourceId(countryInfo.countryCode)
        // layoutInflater.inflate(layoutResourceId, binding.inputSignContainer)
        // initMaxWeightInput()
        // focusMaxWeightInput()
        //
        // binding.inputSignContainer.scaleX = 3f
        // binding.inputSignContainer.scaleY = 3f
        // binding.inputSignContainer.animate().scaleX(1f).scaleY(1f)
    }

    override fun onClickOk() {
        if (userSelectedUnrealisticWeight()) {
            confirmUnusualInput { applyMaxWeightFormAnswer() }
        } else {
            applyMaxWeightFormAnswer()
        }
    }

    private fun userSelectedUnrealisticWeight(): Boolean {
        val weight = getWeightFromInput() ?: return false
        val w = weight.toMetricTons()
        return w > 30 || w < 2
    }

    private fun applyMaxWeightFormAnswer() {
        applyAnswer(MaxWeight(sign.value!!, getWeightFromInput()!!))
    }

    private fun getWeightFromInput(): Weight? {
        // val input = maxWeightInput?.numberOrNull ?: return null
        // val unit = weightLimitUnits[weightUnitSelect?.selectedItemPosition ?: 0]
        // return when (unit) {
        //     WeightMeasurementUnit.SHORT_TON  -> ShortTons(input)
        //     WeightMeasurementUnit.POUND      -> ImperialPounds(input.toInt())
        //     WeightMeasurementUnit.METRIC_TON -> MetricTons(input)
        // }
        return ShortTons(10.0)
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
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoMaxWeightSign) }
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

    companion object {
        private const val MAX_WEIGHT_SIGN = "max_weight_sign"
    }
}
