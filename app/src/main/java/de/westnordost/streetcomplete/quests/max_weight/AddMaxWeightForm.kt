package de.westnordost.streetcomplete.quests.max_weight

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.allowOnlyNumbers
import de.westnordost.streetcomplete.ktx.numberOrNull
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher

import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit.*

class AddMaxWeightForm : AbstractQuestFormAnswerFragment<MaxWeightAnswer>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_maxweight_answer_other_sign) { onUnsupportedSign() },
        OtherAnswer(R.string.quest_maxweight_answer_noSign) { confirmNoSign() }
    )

    private var tonInput: EditText? = null
    private var poundInput: EditText? = null
    private var weightUnitSelect: Spinner? = null
    private var tonInputSign: View? = null
    private var poundInputSign: View? = null

    private val weightLimitUnits get() = countryInfo.weightLimitUnits.map { it.toWeightMeasurementUnit() }

    override fun isFormComplete() = getWeightFromInput() != null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        setMaxWeightSignLayout(R.layout.quest_maxweight, weightLimitUnits.first())
        return view
    }

    private fun setMaxWeightSignLayout(resourceId: Int, unit: WeightMeasurementUnit) {
        val contentView = setContentView(resourceId)

        tonInput = contentView.findViewById(R.id.tonInput)
        poundInput = contentView.findViewById(R.id.poundInput)

        val onTextChangedListener = TextChangedWatcher { checkIsFormComplete() }
        tonInput?.addTextChangedListener(onTextChangedListener)
        poundInput?.addTextChangedListener(onTextChangedListener)

        tonInputSign = contentView.findViewById(R.id.tonInputSign)
        poundInputSign = contentView.findViewById(R.id.poundInputSign)

        weightUnitSelect = contentView.findViewById(R.id.weightUnitSelect)
        weightUnitSelect?.visibility = if (weightLimitUnits.size == 1) View.GONE else View.VISIBLE
        weightUnitSelect?.adapter = ArrayAdapter(context!!, R.layout.spinner_item_centered, weightLimitUnits)
        weightUnitSelect?.setSelection(0)
        weightUnitSelect?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                switchLayout(weightUnitSelect?.selectedItem as WeightMeasurementUnit)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }
        tonInput?.allowOnlyNumbers()
        switchLayout(unit)
    }

    private fun switchLayout(unit: WeightMeasurementUnit) {
        val isTon = unit == TON || unit == SHORT_TON
        val isPound = unit == POUND

        tonInputSign?.visibility = if (isTon) View.VISIBLE else View.GONE
        poundInputSign?.visibility = if (isPound) View.VISIBLE else View.GONE

        if (isTon) tonInput?.requestFocus()
        if (isPound) poundInput?.requestFocus()
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
        return w > 25 || w < 2
    }

    private fun applyMaxWeightFormAnswer() {
        applyAnswer(MaxWeight(getWeightFromInput()!!))
    }

    private fun getWeightFromInput(): WeightMeasure? {
        when(weightUnitSelect?.selectedItem as WeightMeasurementUnit? ?: weightLimitUnits.first()) {
            TON ->       tonInput?.numberOrNull?.let { return MetricTons(it) }
            SHORT_TON -> tonInput?.numberOrNull?.let { return ShortTons(it) }
            POUND ->     poundInput?.numberOrNull?.let { return ImperialPounds(it.toInt()) }
        }
        return null
    }

    private fun onUnsupportedSign() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_maxweight_unsupported_sign_request_photo)
            .setPositiveButton(android.R.string.ok) { _, _ -> composeNote() }
            .setNegativeButton(R.string.quest_leave_new_note_no) { _, _ -> skipQuest() }
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
}
