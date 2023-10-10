package de.westnordost.streetcomplete.quests.max_weight

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.databinding.QuestMaxweightBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.numberOrNull
import de.westnordost.streetcomplete.util.ktx.showKeyboard
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.inputfilter.acceptDecimalDigits

class AddMaxWeightForm : AbstractOsmQuestForm<MaxWeightAnswer>() {

    override val contentLayoutResId = R.layout.quest_maxweight
    private val binding by contentViewBinding(QuestMaxweightBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxweight_answer_other_sign) { onUnsupportedSign() },
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    private var sign: MaxWeightSign? = null

    private val maxWeightInput: EditText? get() = binding.inputSignContainer.findViewById(R.id.maxWeightInput)
    private val weightUnitSelect: Spinner? get() = binding.inputSignContainer.findViewById(R.id.weightUnitSelect)

    private val weightLimitUnits get() = countryInfo.weightLimitUnits

    override fun isFormComplete() = getWeightFromInput() != null

    override fun isRejectingClose() = sign != null || getWeightFromInput() != null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectSignButton.setOnClickListener { showSignSelectionDialog() }

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        }

        binding.selectSignButton.isVisible = sign == null
        if (sign == null) binding.inputSignContainer.removeAllViews()
        initMaxWeightInput()
    }

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        sign = savedInstanceState.getString(MAX_WEIGHT_SIGN)?.let { MaxWeightSign.valueOf(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        sign?.let { outState.putString(MAX_WEIGHT_SIGN, it.name) }
    }

    private fun initMaxWeightInput() {
        val maxWeightInput = maxWeightInput ?: return

        maxWeightInput.doAfterTextChanged { checkIsFormComplete() }
        maxWeightInput.filters = arrayOf(acceptDecimalDigits(6, 2))
        binding.inputSignContainer.setOnClickListener { focusMaxWeightInput() }

        val units = weightLimitUnits.map { it.displayString }
        weightUnitSelect?.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_centered, units)
        weightUnitSelect?.setSelection(0)
    }

    private fun focusMaxWeightInput() {
        val maxWeightInput = maxWeightInput ?: return

        maxWeightInput.requestFocus()
        maxWeightInput.showKeyboard()
    }

    private fun showSignSelectionDialog() {
        val ctx = context ?: return
        val items = MaxWeightSign.values().map {
            it.asItem(layoutInflater, countryInfo.countryCode)
        }
        ImageListPickerDialog(ctx, items, R.layout.cell_labeled_icon_select, 2) { selected ->
            selected.value?.let { setMaxWeightSign(it) }
            checkIsFormComplete()
        }.show()
    }

    private fun setMaxWeightSign(sign: MaxWeightSign) {
        this.sign = sign

        binding.selectSignButton.isInvisible = true
        binding.inputSignContainer.removeAllViews()

        val layoutResourceId = sign.getLayoutResourceId(countryInfo.countryCode)
        layoutInflater.inflate(layoutResourceId, binding.inputSignContainer)
        initMaxWeightInput()
        focusMaxWeightInput()

        binding.inputSignContainer.scaleX = 3f
        binding.inputSignContainer.scaleY = 3f
        binding.inputSignContainer.animate().scaleX(1f).scaleY(1f)
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
        applyAnswer(MaxWeight(sign!!, getWeightFromInput()!!))
    }

    private fun getWeightFromInput(): Weight? {
        val input = maxWeightInput?.numberOrNull ?: return null
        val unit = weightLimitUnits[weightUnitSelect?.selectedItemPosition ?: 0]
        return when (unit) {
            WeightMeasurementUnit.SHORT_TON  -> ShortTons(input)
            WeightMeasurementUnit.POUND      -> ImperialPounds(input.toInt())
            WeightMeasurementUnit.METRIC_TON -> MetricTons(input)
        }
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
