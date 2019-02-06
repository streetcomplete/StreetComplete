package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher

import de.westnordost.streetcomplete.quests.max_height.Measurement.*

private enum class Measurement { METRIC, IMPERIAL }

class AddMaxHeightForm : AbstractQuestFormAnswerFragment<MaxHeightAnswer>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_maxheight_answer_noSign) { confirmNoSign() }
    )

    private var meterInput: EditText? = null
    private var feetInput: EditText? = null
    private var inchInput: EditText? = null
    private var heightUnitSelect: Spinner? = null

    private var meterInputSign: View? = null
    private var feetInputSign: View? = null

    override fun isFormComplete() = getHeightFromInput() != null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val unit = if (countryInfo.measurementSystem[0] == "metric") METRIC else IMPERIAL
        setMaxHeightSignLayout(R.layout.quest_maxheight, unit)

        return view
    }

    private fun setMaxHeightSignLayout(resourceId: Int, unit: Measurement) {
        val contentView = setContentView(resourceId)

        meterInput = contentView.findViewById(R.id.meterInput)
        feetInput = contentView.findViewById(R.id.feetInput)
        inchInput = contentView.findViewById(R.id.inchInput)

        val onTextChangedListener = TextChangedWatcher { checkIsFormComplete() }
        meterInput?.addTextChangedListener(onTextChangedListener)
        feetInput?.addTextChangedListener(onTextChangedListener)
        inchInput?.addTextChangedListener(onTextChangedListener)

        meterInputSign = contentView.findViewById(R.id.meterInputSign)
        feetInputSign = contentView.findViewById(R.id.feetInputSign)

        heightUnitSelect = contentView.findViewById(R.id.heightUnitSelect)
        val measurementUnits = countryInfo.measurementSystem
        heightUnitSelect?.visibility = if (measurementUnits.size == 1) View.GONE else View.VISIBLE
        heightUnitSelect?.adapter = ArrayAdapter(context!!, R.layout.spinner_item_centered, getSpinnerItems(measurementUnits))
        heightUnitSelect?.setSelection(0)
        heightUnitSelect?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                val heightUnit = if (heightUnitSelect?.selectedItem == "m") METRIC else IMPERIAL
                switchLayout(heightUnit)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        inchInput?.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val destStr = dest.toString()
            val input = destStr.substring(0, dstart) + source.toString() + destStr.substring(dend, destStr.length)
            if (input.toInt() <= 12) null else ""
        })
        /* Workaround for an Android bug that it assumes the decimal separator to always be the "."
           for EditTexts with inputType "numberDecimal", independent of Locale. See
           https://issuetracker.google.com/issues/36907764 .

           Affected Android versions are all versions till (exclusive) Android Oreo. */

        /* actually, let's not care about which separator the user uses, he might be confused
           whether he should use the one as displayed on the sign or in his phone's locale */
        //char separator = DecimalFormatSymbols.getInstance(getCountryInfo().getLocale()).getDecimalSeparator();
        meterInput?.keyListener = DigitsKeyListener.getInstance("0123456789,.")

        switchLayout(unit)
    }

    private fun switchLayout(unit: Measurement) {
        val isMetric = unit == METRIC
        val isImperial = unit == IMPERIAL

        meterInputSign?.visibility = if (isMetric) View.VISIBLE else View.GONE
        feetInputSign?.visibility = if (isImperial) View.VISIBLE else View.GONE

        if (isMetric) meterInput?.requestFocus()
        if (isImperial) feetInput?.requestFocus()
    }

    private fun getSpinnerItems(units: List<String>) = units.mapNotNull {
        when(it) {
            "metric" -> "m"
            "imperial" -> "ft"
            else -> null
        }
    }

    private fun confirmNoSign() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_maxheight_answer_noSign_question)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ ->  applyAnswer(NoMaxHeightSign(true)) }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(NoMaxHeightSign(false)) }
            .show()
        }
    }

    override fun onClickOk() {
        if (userSelectedUnrealisticHeight()) {
            confirmUnusualInput { applyMaxHeightFormAnswer() }
        } else {
            applyMaxHeightFormAnswer()
        }
    }

    private fun userSelectedUnrealisticHeight(): Boolean {
        val height = getHeightFromInput() ?: return false
        val m = height.toMeters()
        return m > 6 || m < 1.9
    }

    private fun applyMaxHeightFormAnswer() {
        applyAnswer(MaxHeight(getHeightFromInput()!!))
    }

    private fun getHeightFromInput(): Measure? {
        if (isMetric()) {
            val input = meterInput!!.text.toString().replace(",", ".")
            if (input.isNotEmpty()) return MetricMeasure(input.toDouble())
        } else {
            val feetString = feetInput!!.text.toString()
            val inchString = inchInput!!.text.toString()

            if (feetString.isNotEmpty() && inchString.isNotEmpty()) {
                return ImperialMeasure(feetString.toInt(), inchString.toInt())
            }
        }
        return null
    }

    private fun isMetric() =
        heightUnitSelect?.let { it.selectedItem == "m" }
            ?: (countryInfo.measurementSystem[0] == "metric")

    private fun confirmUnusualInput(callback: () -> (Unit)) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.quest_generic_confirmation_title)
                .setMessage(R.string.quest_maxheight_unusualInput_confirmation_description)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }
}
